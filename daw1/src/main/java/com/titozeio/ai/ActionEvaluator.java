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
 * Puntuación heurística simple para la IA fácil.
 */
public class ActionEvaluator {

    public int scoreMove(Game game, Robot actor, Hexagon target) {
        int score = 10;
        Player enemy = game.getOpponent(actor.getOwner());
        int currentBestDistance = minDistanceToEnemies(game, actor.getPosition(), enemy);
        int newBestDistance = minDistanceToEnemies(game, target, enemy);

        if (newBestDistance < currentBestDistance) {
            score += (currentBestDistance - newBestDistance) * 30;
        }

        if (canAttackAnyEnemyFrom(game, actor, target)) {
            score += 80;
        }

        if (target.getTerrain() == TerrainType.VEGETATION) {
            score += 20;
        } else if (target.getTerrain() == TerrainType.WATER_SHALLOW) {
            score -= 40;
        }

        Hexagon enemyBase = enemy.getBaseLocation();
        if (enemyBase != null && actor.getPosition() != null) {
            int currentBaseDistance = game.getMap().getDistance(actor.getPosition(), enemyBase);
            int newBaseDistance = game.getMap().getDistance(target, enemyBase);
            if (newBaseDistance < currentBaseDistance) {
                score += 15;
            }
        }

        return score;
    }

    public int scoreAttack(Game game, Robot actor, Robot target) {
        int estimatedDamage = estimateBasicAttackDamage(game, actor, target);
        int score = 200 + estimatedDamage * 25;
        if (target.getCurrentHp() <= estimatedDamage) {
            score += 1000;
        }
        if (isThreateningBase(game, target)) {
            score += 150;
        }
        return score;
    }

    public int scoreSkill(Game game, Robot actor, Object target) {
        Skill skill = actor.getSkill();
        if (skill == null) {
            return Integer.MIN_VALUE;
        }

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

        if (skill instanceof GuidedMissile guidedMissile && target instanceof Robot enemy) {
            int score = 260 + guidedMissile.getDamage() * 22;
            if (enemy.getCurrentHp() <= guidedMissile.getDamage()) {
                score += 1000;
            }
            return score;
        }

        if (skill instanceof PlasmaSaberAttack plasmaSaber && target instanceof Robot enemy) {
            int score = 280 + plasmaSaber.getDamage() * 20;
            if (enemy.getCurrentHp() <= plasmaSaber.getDamage()) {
                score += 1000;
            }
            return score;
        }

        if (skill instanceof RepairNanobots repairNanobots && target instanceof Robot ally) {
            int missingHp = ally.getMaxHp() - ally.getCurrentHp();
            if (missingHp <= 0) {
                return 0;
            }
            return 180 + Math.min(missingHp, repairNanobots.getHealAmount()) * 30;
        }

        if ((skill instanceof JetpackBoost || skill instanceof TurboPropulsion) && target instanceof Hexagon hex) {
            return 120 + scoreMove(game, actor, hex);
        }

        return 50;
    }

    private int estimateBasicAttackDamage(Game game, Robot actor, Robot target) {
        int damage = actor.getWeapon().getDamage();
        if (actor.getPosition() != null) {
            damage = Math.round(damage * actor.getPosition().getAttackDamageModifier());
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

    private boolean isThreateningBase(Game game, Robot robot) {
        Player owner = robot.getOwner();
        Player enemy = game.getOpponent(owner);
        Hexagon enemyBase = enemy.getBaseLocation();
        return enemyBase != null && robot.getPosition() == enemyBase;
    }
}
