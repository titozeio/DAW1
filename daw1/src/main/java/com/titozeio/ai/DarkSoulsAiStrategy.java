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
 * Implementa un Minimax con Poda Alfa-Beta con profundidad limitada.
 */
public class DarkSoulsAiStrategy implements AiStrategy {
    private final ActionEvaluator evaluator;
    private static final int DEPTH = 2; // 2 plies (Yo -> Oponente)
    private static final int MAX_BRANCHES = 4; // Poda heurística de ramificación (Iterative Deepening parcial)

    public DarkSoulsAiStrategy(ActionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public TurnDecision chooseDecision(MatchContext context, Player player, TurnEngine turnEngine) {
        Game game = context.getGame();
        if (!player.hasActionsLeft()) {
            return new EndTurnDecision();
        }

        List<GameCommand> allLegalCommands = new ArrayList<>();
        for (Robot robot : player.getAliveUnits()) {
            if (!robot.isExhausted()) {
                allLegalCommands.addAll(turnEngine.getLegalCommands(game, robot));
            }
        }

        if (allLegalCommands.isEmpty()) {
            return new EndTurnDecision();
        }

        // 1. Move Ordering heurístico (Poda agresiva del árbol)
        List<ScoredDecision> initialScores = new ArrayList<>();
        for (GameCommand cmd : allLegalCommands) {
            Robot actor = getActorFromCommand(cmd);
            int score = evaluator.scoreMove(game, actor, actor.getPosition()); // Usamos score por defecto si no podemos
                                                                               // derivar
            // Por simplicidad, usamos el ActionEvaluator de forma similar a Hard
            if (cmd instanceof com.titozeio.commands.MoveCommand mc)
                score = evaluator.scoreMove(game, actor, mc.getTargetHex());
            else if (cmd instanceof com.titozeio.commands.AttackCommand ac)
                score = evaluator.scoreAttack(game, actor, ac.getTarget());
            else if (cmd instanceof com.titozeio.commands.SkillCommand sc)
                score = evaluator.scoreSkill(game, actor, sc.getTarget());

            initialScores.add(new ScoredDecision(cmd, score));
        }

        initialScores.sort(Comparator.comparingInt(ScoredDecision::score).reversed());
        List<GameCommand> topCommands = new ArrayList<>();
        int limit = Math.min(MAX_BRANCHES, initialScores.size());
        for (int i = 0; i < limit; i++) {
            topCommands.add(initialScores.get(i).command());
        }

        // 2. Minimax con Poda Alfa-Beta
        GameCommand bestCommand = null;
        int bestValue = Integer.MIN_VALUE;

        for (GameCommand cmd : topCommands) {
            GameStateSnapshot snapshot = new GameStateSnapshot(game);

            // Simular ejecución directa
            executeSimulatedCommand(cmd, game);

            // Turno del oponente (profundidad 1)
            int boardValue = minimax(game, turnEngine, DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, player);

            snapshot.restore(game);

            if (boardValue > bestValue) {
                bestValue = boardValue;
                bestCommand = cmd;
            }
        }

        if (bestCommand == null) {
            return new EndTurnDecision();
        }

        return new ExecuteCommandDecision(bestCommand);
    }

    private int minimax(Game game, TurnEngine turnEngine, int depth, int alpha, int beta, boolean isMaximizingPlayer,
            Player aiPlayer) {
        if (depth == 0 || checkVictoryConditionApproximation(game)) {
            return evaluateBoardDeeply(game, aiPlayer);
        }

        Player currentPlayer = isMaximizingPlayer ? aiPlayer : game.getOpponent(aiPlayer);
        List<GameCommand> legalCommands = new ArrayList<>();
        for (Robot robot : currentPlayer.getAliveUnits()) {
            if (!robot.isExhausted()) {
                legalCommands.addAll(turnEngine.getLegalCommands(game, robot));
            }
        }

        if (legalCommands.isEmpty()) {
            // Si el jugador no tiene más acciones, se pasaría turno. Analizamos un ply más
            // simulando el pase de turno.
            return evaluateBoardDeeply(game, aiPlayer); // En un modelo real de auto-battler por turnos, un pase
                                                        // finaliza el turno del jugador y avanza la ronda
        }

        // Simplificado: tomar una subida del ramal aleatoria o heurística para no
        // explotar memoria (solo top MAX_BRANCHES)
        List<ScoredDecision> heuristics = new ArrayList<>();
        for (GameCommand cmd : legalCommands) {
            Robot actor = getActorFromCommand(cmd);
            int score = 0;
            if (cmd instanceof com.titozeio.commands.MoveCommand mc)
                score = evaluator.scoreMove(game, actor, mc.getTargetHex());
            else if (cmd instanceof com.titozeio.commands.AttackCommand ac)
                score = evaluator.scoreAttack(game, actor, ac.getTarget());
            else if (cmd instanceof com.titozeio.commands.SkillCommand sc)
                score = evaluator.scoreSkill(game, actor, sc.getTarget());

            heuristics.add(new ScoredDecision(cmd, score));
        }
        heuristics.sort(Comparator.comparingInt(ScoredDecision::score).reversed());

        int limit = Math.min(MAX_BRANCHES, heuristics.size());

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (int i = 0; i < limit; i++) {
                GameCommand cmd = heuristics.get(i).command();
                GameStateSnapshot snap = new GameStateSnapshot(game);
                executeSimulatedCommand(cmd, game);
                int eval = minimax(game, turnEngine, depth - 1, alpha, beta, false, aiPlayer);
                snap.restore(game);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha)
                    break; // Poda Alpha-Beta
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int i = 0; i < limit; i++) {
                GameCommand cmd = heuristics.get(i).command();
                GameStateSnapshot snap = new GameStateSnapshot(game);
                executeSimulatedCommand(cmd, game);
                int eval = minimax(game, turnEngine, depth - 1, alpha, beta, true, aiPlayer);
                snap.restore(game);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha)
                    break; // Poda Alpha-Beta
            }
            return minEval;
        }
    }

    private void executeSimulatedCommand(GameCommand cmd, Game game) {
        // En una simulación, solo actualizamos HPs, Posiciones, Exhaustion para evitar
        // trigger de UI o Logs.
        // Dado que GameCommand original dispara eventos y UI, hacemos una simulación
        // "sucia"
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
            target.setCurrentHp(target.getCurrentHp() - dmg);
            attacker.setUsedAttack(true);
        } else if (cmd instanceof com.titozeio.commands.SkillCommand sc) {
            sc.getRobot().setUsedMovement(true); // asunción segura
            sc.getRobot().setUsedAttack(true);
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

    private boolean checkVictoryConditionApproximation(Game game) {
        return game.getP1().getAliveUnits().isEmpty() || game.getP2().getAliveUnits().isEmpty();
    }

    private int evaluateBoardDeeply(Game game, Player aiPlayer) {
        Player opponent = game.getOpponent(aiPlayer);
        int score = 0;

        // Evaluación Heurística Madura Avanzada
        // 1. Vida restante
        for (Robot r : aiPlayer.getAliveUnits())
            score += r.getCurrentHp() * 10;
        for (Robot r : opponent.getAliveUnits())
            score -= r.getCurrentHp() * 10;

        // 2. Ventaja material
        score += aiPlayer.getAliveUnits().size() * 500;
        score -= opponent.getAliveUnits().size() * 500;

        // 3. Control del centro
        Hexagon center = game.getMap().getHexagon(4, 5); // Aprox centro en tablero 9x11
        if (center != null) {
            for (Robot r : aiPlayer.getAliveUnits()) {
                if (r.getPosition() != null) {
                    int dist = game.getMap().getDistance(r.getPosition(), center);
                    score += Math.max(0, 100 - (dist * 20)); // Bonus por dominio central
                }
            }
            for (Robot r : opponent.getAliveUnits()) {
                if (r.getPosition() != null) {
                    int dist = game.getMap().getDistance(r.getPosition(), center);
                    score -= Math.max(0, 100 - (dist * 20)); // Resta por dominio central enemigo
                }
            }
        }

        return score;
    }

    private record ScoredDecision(GameCommand command, int score) {
    }

    // Implementación interna rápida para clonado de estado de todos los robots
    // (Momento 0) sin usar UI
    private static class GameStateSnapshot {
        private final List<RobotStateRecord> p1Robots = new ArrayList<>();
        private final List<RobotStateRecord> p2Robots = new ArrayList<>();

        public GameStateSnapshot(Game game) {
            for (Robot r : game.getP1().getUnits())
                p1Robots.add(new RobotStateRecord(r));
            for (Robot r : game.getP2().getUnits())
                p2Robots.add(new RobotStateRecord(r));
        }

        public void restore(Game game) {
            for (RobotStateRecord rr : p1Robots)
                rr.restore();
            for (RobotStateRecord rr : p2Robots)
                rr.restore();
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
            this.position = robot.getPosition();
            this.usedMovement = robot.isUsedMovement();
            this.usedAttack = robot.isUsedAttack();
        }

        public void restore() {
            if (robot.getPosition() != null)
                robot.getPosition().setOccupant(null); // desocupamos actual
            robot.setCurrentHp(currentHp);
            robot.setPosition(position);
            if (position != null)
                position.setOccupant(robot);
            robot.setUsedMovement(usedMovement);
            robot.setUsedAttack(usedAttack);
        }
    }
}
