package com.titozeio.ai;

import com.titozeio.commands.GameCommand;
import com.titozeio.engine.Game;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.match.TurnEngine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Estrategia de IA nivel DARK SOULS.
 * Implementa una Búsqueda en Profundidad (DFS) Combinatoria sobre el turno
 * actual (Combo Search),
 * apoyada en una fuerte evaluación del tablero (Board Heuristics) y clonación
 * de estados.
 */
public class DarkSoulsAiStrategy implements AiStrategy {
    private final ActionEvaluator evaluator;
    // Configuración para el tamaño de la búsqueda en el propio turno de la IA
    private static final int MAX_TURN_ACTIONS = 4; // Profundidad máxima del combo en el mismo turno
    private static final int BRANCHES_PER_STEP = 3; // Cuántas mejores ramificaciones probar por acción

    public DarkSoulsAiStrategy(ActionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public TurnDecision chooseDecision(MatchContext context, Player player, TurnEngine turnEngine) {
        Game game = context.getGame();
        if (!player.hasActionsLeft()) {
            return new EndTurnDecision();
        }

        List<GameCommand> legal = getTopLegalCommands(game, player, turnEngine, BRANCHES_PER_STEP + 1); // un poco más
                                                                                                        // de ancho en
                                                                                                        // la raíz
        if (legal.isEmpty()) {
            return new EndTurnDecision();
        }

        GameCommand bestCommand = null;
        int bestScore = Integer.MIN_VALUE;

        // Iniciar la búsqueda DFS sobre las mejores posibles acciones para ESTE turno
        for (GameCommand cmd : legal) {
            GameStateSnapshot snap = new GameStateSnapshot(game);
            executeSimulatedCommand(cmd, game);

            int score = searchTurnCombos(game, player, turnEngine, 1);

            snap.restore(game);

            if (bestCommand == null || score > bestScore) {
                bestScore = score;
                bestCommand = cmd;
            }
        }

        if (bestCommand == null) {
            return new EndTurnDecision();
        }
        return new ExecuteCommandDecision(bestCommand);
    }

    private int searchTurnCombos(Game game, Player aiPlayer, TurnEngine turnEngine, int depth) {
        if (!aiPlayer.hasActionsLeft() || depth >= MAX_TURN_ACTIONS) {
            return evaluateBoardDarkSouls(game, aiPlayer);
        }

        List<GameCommand> legal = getTopLegalCommands(game, aiPlayer, turnEngine, BRANCHES_PER_STEP);
        if (legal.isEmpty()) {
            return evaluateBoardDarkSouls(game, aiPlayer); // Nudo terminal de este combo
        }

        int maxComboScore = Integer.MIN_VALUE;
        for (GameCommand cmd : legal) {
            GameStateSnapshot snap = new GameStateSnapshot(game);
            executeSimulatedCommand(cmd, game);

            int score = searchTurnCombos(game, aiPlayer, turnEngine, depth + 1);

            snap.restore(game);
            if (score > maxComboScore) {
                maxComboScore = score;
            }
        }
        return maxComboScore;
    }

    private List<GameCommand> getTopLegalCommands(Game game, Player player, TurnEngine turnEngine, int limit) {
        List<GameCommand> allLegal = new ArrayList<>();
        for (Robot robot : player.getAliveUnits()) {
            if (!robot.isExhausted()) {
                allLegal.addAll(turnEngine.getLegalCommands(game, robot));
            }
        }

        List<ScoredDecision> heuristics = new ArrayList<>();
        for (GameCommand cmd : allLegal) {
            int score = scoreCommandFast(game, getActorFromCommand(cmd), cmd);
            if (score > -5000) { // Reject absolutely terrible actions (like healing full HP)
                heuristics.add(new ScoredDecision(cmd, score));
            }
        }

        heuristics.sort(Comparator.comparingInt(ScoredDecision::score).reversed());
        int actualLimit = Math.min(limit, heuristics.size());

        List<GameCommand> top = new ArrayList<>();
        for (int i = 0; i < actualLimit; i++) {
            top.add(heuristics.get(i).command());
        }
        return top;
    }

    private int scoreCommandFast(Game game, Robot actor, GameCommand cmd) {
        if (cmd instanceof com.titozeio.commands.MoveCommand mc) {
            return evaluator.scoreMove(game, actor, mc.getTargetHex());
        } else if (cmd instanceof com.titozeio.commands.AttackCommand ac) {
            return evaluator.scoreAttack(game, actor, ac.getTarget());
        } else if (cmd instanceof com.titozeio.commands.SkillCommand sc) {
            return evaluator.scoreSkill(game, actor, sc.getTarget());
        }
        return 0;
    }

    private void executeSimulatedCommand(GameCommand cmd, Game game) {
        // Ejecución simulada "sucia" (sin trigger de UI, sonidos ni logs)
        if (cmd instanceof com.titozeio.commands.MoveCommand mc) {
            Robot r = mc.getRobot();
            if (r.getPosition() != null)
                r.getPosition().setOccupant(null);
            if (mc.getTargetHex() != null)
                mc.getTargetHex().setOccupant(r);
            r.setPosition(mc.getTargetHex());
            r.setUsedMovement(true);
        } else if (cmd instanceof com.titozeio.commands.AttackCommand ac) {
            Robot attacker = ac.getAttacker();
            Robot target = ac.getTarget();
            int dmg = evaluator.estimateBasicAttackDamage(game, attacker, target);
            applySimulatedDamage(target, dmg);
            attacker.setUsedAttack(true);
        } else if (cmd instanceof com.titozeio.commands.SkillCommand sc) {
            Robot r = sc.getRobot();
            r.setUsedMovement(true); // El uso de skill consume ambas acciones usualmente
            r.setUsedAttack(true);
            if (sc.getRobot().getSkill() instanceof com.titozeio.skills.RepairNanobots rep
                    && sc.getTarget() instanceof Robot ally) {
                ally.setCurrentHp(ally.getCurrentHp() + rep.getHealAmount());
            } else if (sc.getRobot().getSkill() instanceof com.titozeio.skills.GuidedMissile mis
                    && sc.getTarget() instanceof Robot enemy) {
                applySimulatedDamage(enemy, mis.getDamage());
            } else if (sc.getRobot().getSkill() instanceof com.titozeio.skills.FreezeRay fre
                    && sc.getTarget() instanceof Robot enemy) {
                applySimulatedDamage(enemy, fre.getDamage());
            } else if (sc.getRobot().getSkill() instanceof com.titozeio.skills.PlasmaSaberAttack pla
                    && sc.getTarget() instanceof Robot enemy) {
                applySimulatedDamage(enemy, pla.getDamage());
            } else if ((sc.getRobot().getSkill() instanceof com.titozeio.skills.JetpackBoost
                    || sc.getRobot().getSkill() instanceof com.titozeio.skills.TurboPropulsion)
                    && sc.getTarget() instanceof Hexagon hex) {
                if (r.getPosition() != null)
                    r.getPosition().setOccupant(null);
                if (hex != null)
                    hex.setOccupant(r);
                r.setPosition(hex);
            }
        }
    }

    private void applySimulatedDamage(Robot target, int damage) {
        target.setCurrentHp(target.getCurrentHp() - damage);
        if (target.getCurrentHp() <= 0 && target.getPosition() != null) {
            target.getPosition().setOccupant(null);
            target.setPosition(null);
        }
    }

    private Robot getActorFromCommand(GameCommand cmd) {
        if (cmd instanceof com.titozeio.commands.MoveCommand mc)
            return mc.getRobot();
        if (cmd instanceof com.titozeio.commands.AttackCommand ac)
            return ac.getAttacker();
        if (cmd instanceof com.titozeio.commands.SkillCommand sc)
            return sc.getRobot();
        return null;
    }

    private int evaluateBoardDarkSouls(Game game, Player aiPlayer) {
        int score = 0;
        Player opponent = game.getOpponent(aiPlayer);

        // 1. Evaluación material cruda (Ventaja por vidas y daños)
        for (Robot r : aiPlayer.getAliveUnits()) {
            if (r.getCurrentHp() <= 0)
                continue; // Simuladamente muerto
            score += r.getCurrentHp() * 15;
            score += 600; // Bonus por estar vivo

            // Prioridad por altura / terreno
            if (r.getPosition() != null) {
                score += r.getPosition().getHeight() * 30;
                if (r.getPosition().getTerrain() == com.titozeio.enums.TerrainType.VEGETATION)
                    score += 100;
            }
        }

        for (Robot r : opponent.getAliveUnits()) {
            if (r.getCurrentHp() <= 0)
                continue;
            score -= r.getCurrentHp() * 20; // Agresivo (valora más ver caer el HP enemigo)
            score -= 1000; // Gran bonus por cada enemigo destruido
        }

        // 2. Penalización de Exposición Futura del Tablero (El turno del oponente)
        // Solo evaluamos el daño puro de los que podrían atacar AHORA
        for (Robot r : aiPlayer.getAliveUnits()) {
            if (r.getCurrentHp() <= 0 || r.getPosition() == null)
                continue;
            int maxInboundDamage = 0; // Calculamos un acumulado peor caso ligero

            for (Robot enemy : opponent.getAliveUnits()) {
                if (enemy.getCurrentHp() <= 0 || enemy.getPosition() == null)
                    continue;
                if (evaluator.canAttackTarget(game, enemy, r)) {
                    maxInboundDamage += evaluator.estimateBasicAttackDamage(game, enemy, r);
                }
            }
            if (maxInboundDamage > 0) {
                score -= maxInboundDamage * 12; // Penalización por dolor factible
                if (r.getCurrentHp() <= maxInboundDamage) {
                    score -= 800; // Riesgo crítico de que te destruyan el robot
                }
            }
        }

        // 3. Control del centro del mapa
        Hexagon center = game.getMap().getHexagon(4, 5);
        if (center != null) {
            for (Robot r : aiPlayer.getAliveUnits()) {
                if (r.getCurrentHp() > 0 && r.getPosition() != null) {
                    int dist = game.getMap().getDistance(r.getPosition(), center);
                    score += Math.max(0, 100 - (dist * 20)); // Bonus si la pieza domina el centro
                }
            }
        }

        return score;
    }

    private record ScoredDecision(GameCommand command, int score) {
    }

    // Implementación interna rápida para clonado de estado en memoria
    private static class GameStateSnapshot {
        private final List<RobotStateRecord> p1Robots = new ArrayList<>();
        private final List<RobotStateRecord> p2Robots = new ArrayList<>();

        public GameStateSnapshot(Game game) {
            for (Robot r : game.getP1().getUnits()) {
                p1Robots.add(new RobotStateRecord(r));
            }
            for (Robot r : game.getP2().getUnits()) {
                p2Robots.add(new RobotStateRecord(r));
            }
        }

        public void restore(Game game) {
            // First pass: clear occupants to prevent interference
            for (RobotStateRecord rr : p1Robots)
                rr.clearCurrentPosition();
            for (RobotStateRecord rr : p2Robots)
                rr.clearCurrentPosition();

            // Second pass: restore strictly
            for (RobotStateRecord rr : p1Robots)
                rr.restoreSavedState();
            for (RobotStateRecord rr : p2Robots)
                rr.restoreSavedState();
        }
    }

    private static class RobotStateRecord {
        private final Robot robot;
        private final int currentHp;
        private final Hexagon position;
        private final boolean usedMovement;
        private final boolean usedAttack;

        public RobotStateRecord(Robot robot) {
            this.robot = robot;
            this.currentHp = robot.getCurrentHp();
            // IMPORTANTE: Un robot destruido en la partida real puede conservar su última
            // posición para la animación de muerte. Ignorar esa posición para no
            // resucitarlo como "fantasma".
            this.position = robot.getCurrentHp() > 0 ? robot.getPosition() : null;
            this.usedMovement = robot.isUsedMovement();
            this.usedAttack = robot.isUsedAttack();
        }

        public void clearCurrentPosition() {
            if (robot.getPosition() != null && robot.getPosition().getOccupant() == robot) {
                robot.getPosition().setOccupant(null);
            }
        }

        public void restoreSavedState() {
            robot.setCurrentHp(currentHp);
            robot.setPosition(position);
            if (position != null)
                position.setOccupant(robot);
            robot.setUsedMovement(usedMovement);
            robot.setUsedAttack(usedAttack);
        }
    }
}
