package com.titozeio.ai;

import com.titozeio.engine.Game;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.TerrainType;
import com.titozeio.skills.FreezeRay;
import com.titozeio.skills.GuidedMissile;
import com.titozeio.skills.JetpackBoost;
import com.titozeio.skills.PlasmaSaberAttack;
import com.titozeio.skills.RepairNanobots;
import com.titozeio.skills.Skill;
import com.titozeio.skills.TurboPropulsion;

/**
 * Evaluador de acciones para la IA.
 * Proporciona una puntuación heurística para cada posible comando, basándose en
 * los pesos definidos en el GDD (marzo 2026).
 * Esta clase es el "cerebro" que determina qué tan útil es una acción
 * inmediata.
 */
public class ActionEvaluator {
    private final AiDifficulty difficulty;

    /**
     * Constructor por defecto (Dificultad Fácil).
     */
    public ActionEvaluator() {
        this(AiDifficulty.EASY);
    }

    /**
     * Constructor con nivel de dificultad específico.
     * 
     * @param difficulty Nivel que afectará a los pesos de la heurística.
     */
    public ActionEvaluator(AiDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Evalúa una acción de movimiento hacia una casilla destino.
     * 
     * @param game   Estado actual del juego.
     * @param actor  Robot que realiza el movimiento.
     * @param target Casilla hexagonal de destino.
     * @return Puntuación que refleja la ventaja táctica de terminar en esa casilla.
     */
    public int scoreMove(Game game, Robot actor, Hexagon target) {
        int score = 10; // Puntuación base por movimiento
        Player enemy = game.getOpponent(actor.getOwner());
        int currentBestDistance = minDistanceToEnemies(game, actor.getPosition(), enemy);
        int newBestDistance = minDistanceToEnemies(game, target, enemy);

        // Bonus por acercarse al enemigo más cercano (+30 por hexágono reducido)
        if (newBestDistance < currentBestDistance) {
            score += (currentBestDistance - newBestDistance) * 30;
        }

        // Bonus por terminar en una casilla desde la que se puede atacar (+80)
        if (canAttackAnyEnemyFrom(game, actor, target)) {
            score += 80;
        }

        // Bonus/Malus por tipo de terreno estratégico
        if (target.getTerrain() == TerrainType.VEGETATION) {
            score += 20; // Ventaja defensiva
        } else if (target.getTerrain() == TerrainType.WATER_SHALLOW) {
            score -= 40; // Exposición innecesaria
        }

        // Bonus por acercarse a la base enemiga para captura (+15)
        Hexagon enemyBase = enemy.getBaseLocation();
        if (enemyBase != null && actor.getPosition() != null) {
            int currentBaseDistance = game.getMap().getDistance(actor.getPosition(), enemyBase);
            int newBaseDistance = game.getMap().getDistance(target, enemyBase);
            if (newBaseDistance < currentBaseDistance) {
                score += 15;
            }
        }

        // MEDICIÓN DE RIESGO (Medio+): Penalización por exposición
        if (difficulty != AiDifficulty.EASY) {
            int threats = countEnemiesCanAttack(game, target, enemy);
            if (threats > 1) {
                score -= 150; // "Terminar muy expuesto" (GDD)
            } else if (threats == 1) {
                score -= 40; // Ligero riesgo
            }

            // Priorización de supervivencia si tiene poca vida
            if (actor.getCurrentHp() <= actor.getMaxHp() * 0.3) {
                if (target.getTerrain() == TerrainType.VEGETATION) {
                    score += 100; // Buscar cobertura desesperadamente
                }
                if (newBestDistance > currentBestDistance) {
                    score += 50; // Huir es buena idea si estamos fatal
                }
            }
        }

        return score;
    }

    /**
     * Evalúa una acción de ataque estándar contra un robot enemigo.
     * 
     * @param game   Estado actual del juego.
     * @param actor  Robot atacante.
     * @param target Robot defensor.
     * @return Puntuación basada en el daño estimado y el valor estratégico del
     *         objetivo.
     */
    public int scoreAttack(Game game, Robot actor, Robot target) {
        int estimatedDamage = estimateBasicAttackDamage(game, actor, target);
        // Base de ataque + peso por daño infligido
        int score = 200 + estimatedDamage * 25;

        // Bonus crítico por destrucción del objetivo (+1000)
        if (target.getCurrentHp() <= estimatedDamage) {
            score += 1000;
        }

        // Bonus por defender la base propia (+150 si el objetivo está en la base)
        if (isThreateningBase(game, target)) {
            score += 150;
        }

        // FOCUS FIRE (Medio+): Bonus por atacar al mismo objetivo que otros aliados
        if (difficulty != AiDifficulty.EASY) {
            for (Robot ally : actor.getOwner().getAliveUnits()) {
                if (ally != actor && !ally.isDestroyed() && canAttackTarget(game, ally, target)) {
                    score += 60; // Concentrar fuego es más eficiente
                }
            }

            // Priorización de objetivos débiles (Rematador)
            if (target.getCurrentHp() <= target.getMaxHp() * 0.25) {
                score += 100;
            }
        }

        return score;
    }

    /**
     * Evalúa el uso de una habilidad especial (Skill).
     * 
     * @param game   Estado actual del juego.
     * @param actor  Robot que usa la skill.
     * @param target Objetivo de la skill (puede ser Robot o Hexagon según el tipo).
     * @return Puntuación basada en el efecto específico de la habilidad.
     */
    public int scoreSkill(Game game, Robot actor, Object target) {
        Skill skill = actor.getSkill();
        if (skill == null) {
            return Integer.MIN_VALUE;
        }

        // Skill ofensiva: Rayo Congelador
        if (skill instanceof FreezeRay freezeRay && target instanceof Robot enemy) {
            int score = 250 + freezeRay.getDamage() * 20;
            if (enemy.getCurrentHp() <= freezeRay.getDamage()) {
                score += 900;
            }
            if (isThreateningBase(game, enemy)) {
                score += 200;
            }
            return score;
        }

        // Skill ofensiva: Misil Teledirigido
        if (skill instanceof GuidedMissile guidedMissile && target instanceof Robot enemy) {
            int score = 260 + guidedMissile.getDamage() * 22;
            if (enemy.getCurrentHp() <= guidedMissile.getDamage()) {
                score += 1000;
            }
            return score;
        }

        // Skill ofensiva: Sable de Plasma (cuerpo a cuerpo pesado)
        if (skill instanceof PlasmaSaberAttack plasmaSaber && target instanceof Robot enemy) {
            int score = 280 + plasmaSaber.getDamage() * 20;
            if (enemy.getCurrentHp() <= plasmaSaber.getDamage()) {
                score += 1000;
            }
            return score;
        }

        // Skill de soporte: Nanobots Reparadores
        if (skill instanceof RepairNanobots repairNanobots && target instanceof Robot ally) {
            int missingHp = ally.getMaxHp() - ally.getCurrentHp();
            if (missingHp <= 0) {
                return 0; // No curar si ya está al máximo
            }
            return 180 + Math.min(missingHp, repairNanobots.getHealAmount()) * 30;
        }

        // Skills de movilidad: Jetpack o Turbo
        if ((skill instanceof JetpackBoost || skill instanceof TurboPropulsion) && target instanceof Hexagon hex) {
            return 120 + scoreMove(game, actor, hex);
        }

        return 50; // Puntuación por defecto para casos no contemplados
    }

    public int estimateBasicAttackDamage(Game game, Robot actor, Robot target) {
        int damage = actor.getWeapon().getDamage();
        if (actor.getPosition() != null) {
            damage = Math.round(damage * actor.getPosition().getAttackDamageModifier());
        }
        if (actor.getPosition() != null && target.getPosition() != null) {
            damage = Math.round(damage * actor.getPosition().getHeightAttackModifierAgainst(target.getPosition()));
        }
        if (actor.getPosition() != null && target.getPosition() != null) {
            float coverMod = game.getMap().getCoverModifier(actor.getPosition(), target.getPosition());
            if (coverMod < 0f) {
                return 0;
            }
            damage = Math.round(damage * (1f - coverMod));
        }
        if (target.getPosition() != null) {
            damage = Math.max(0, Math.round(damage * target.getPosition().getDamageModifier()));
        }
        return damage;
    }

    private int minDistanceToEnemies(Game game, Hexagon from, Player enemy) {
        if (from == null) {
            return Integer.MAX_VALUE;
        }
        int best = Integer.MAX_VALUE;
        for (Robot robot : enemy.getAliveUnits()) {
            if (robot.getPosition() == null) {
                continue;
            }
            best = Math.min(best, game.getMap().getDistance(from, robot.getPosition()));
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    private boolean canAttackAnyEnemyFrom(Game game, Robot actor, Hexagon candidateHex) {
        if (candidateHex == null) {
            return false;
        }
        for (Robot enemy : game.getOpponent(actor.getOwner()).getAliveUnits()) {
            if (enemy.getPosition() == null) {
                continue;
            }
            int dist = game.getMap().getDistance(candidateHex, enemy.getPosition());
            if (dist <= actor.getWeapon().getRange()
                    && game.getMap().hasVisibility(candidateHex, enemy.getPosition())
                    && game.getMap().getCoverModifier(candidateHex, enemy.getPosition()) >= 0f) {
                return true;
            }
        }
        return false;
    }

    private int countEnemiesCanAttack(Game game, Hexagon hex, Player enemyPlayer) {
        int count = 0;
        for (Robot enemy : enemyPlayer.getAliveUnits()) {
            if (canAttackTargetFrom(game, enemy, enemy.getPosition(), hex)) {
                count++;
            }
        }
        return count;
    }

    public boolean canAttackTarget(Game game, Robot actor, Robot target) {
        return canAttackTargetFrom(game, actor, actor.getPosition(), target.getPosition());
    }

    private boolean canAttackTargetFrom(Game game, Robot actor, Hexagon from, Hexagon targetHex) {
        if (from == null || targetHex == null)
            return false;
        int dist = game.getMap().getDistance(from, targetHex);
        return dist <= actor.getWeapon().getRange()
                && game.getMap().hasVisibility(from, targetHex)
                && game.getMap().getCoverModifier(from, targetHex) >= 0f;
    }

    private boolean isThreateningBase(Game game, Robot robot) {
        Player owner = robot.getOwner();
        Player enemy = game.getOpponent(owner);
        Hexagon enemyBase = enemy.getBaseLocation();
        return enemyBase != null && robot.getPosition() == enemyBase;
    }
}
