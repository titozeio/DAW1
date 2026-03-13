package com.titozeio.ai;

import com.titozeio.commands.AttackCommand;
import com.titozeio.commands.GameCommand;
import com.titozeio.commands.MoveCommand;
import com.titozeio.commands.SkillCommand;
import com.titozeio.engine.Game;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.match.TurnEngine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Estrategia de IA difícil.
 * Introducce un Minimax con profundidad 1 ("Lookahead") con poda previa sobre
 * un número de acciones prometedoras.
 */
public class HardAiStrategy implements AiStrategy {
    private final ActionEvaluator evaluator;
    private static final int MAX_EVALUATED_ACTIONS = 6;

    public HardAiStrategy(ActionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public TurnDecision chooseDecision(MatchContext context, Player player, TurnEngine turnEngine) {
        Game game = context.getGame();
        if (!player.hasActionsLeft()) {
            return new EndTurnDecision();
        }

        List<ScoredDecision> allDecisions = new ArrayList<>();

        for (Robot robot : player.getAliveUnits()) {
            if (!robot.isExhausted()) {
                List<GameCommand> commands = turnEngine.getLegalCommands(game, robot);
                for (GameCommand cmd : commands) {
                    int score = scoreCommand(game, robot, cmd);
                    allDecisions.add(new ScoredDecision(cmd, score));
                }
            }
        }

        if (allDecisions.isEmpty()) {
            return new EndTurnDecision();
        }

        allDecisions.sort(Comparator.comparingInt(ScoredDecision::score).reversed());
        int limit = Math.min(MAX_EVALUATED_ACTIONS, allDecisions.size());
        List<ScoredDecision> topDecisions = allDecisions.subList(0, limit);

        ScoredDecision bestDecision = null;
        int bestNetScore = Integer.MIN_VALUE;

        for (ScoredDecision scored : topDecisions) {
            int netScore = scored.score() - evaluateOpponentResponsePenalty(game, scored.command(), player);
            if (bestDecision == null || netScore > bestNetScore) {
                bestNetScore = netScore;
                bestDecision = new ScoredDecision(scored.command(), netScore); // guardamos con el netScore para trazas
            }
        }

        if (bestDecision == null) {
            return new EndTurnDecision();
        }
        return new ExecuteCommandDecision(bestDecision.command());
    }

    private int scoreCommand(Game game, Robot robot, GameCommand command) {
        if (command instanceof MoveCommand move) {
            return evaluator.scoreMove(game, robot, move.getTargetHex());
        } else if (command instanceof AttackCommand attack) {
            return evaluator.scoreAttack(game, robot, attack.getTarget());
        } else if (command instanceof SkillCommand skill) {
            return evaluator.scoreSkill(game, robot, skill.getTarget());
        }
        return 0;
    }

    private int evaluateOpponentResponsePenalty(Game game, GameCommand command, Player myPlayer) {
        Robot actor = null;
        Hexagon originalHex = null;
        Hexagon targetHex = null;
        Robot attackedTarget = null;
        int previousHp = 0;

        // Fase 1: Pre-ejecución simulada
        if (command instanceof MoveCommand moveCmd) {
            actor = moveCmd.getRobot();
            originalHex = actor.getPosition();
            targetHex = moveCmd.getTargetHex();

            if (originalHex != null)
                originalHex.setOccupant(null);
            if (targetHex != null)
                targetHex.setOccupant(actor);
            actor.setPosition(targetHex);
        } else if (command instanceof AttackCommand attackCmd) {
            actor = attackCmd.getAttacker();
            attackedTarget = attackCmd.getTarget();
            previousHp = attackedTarget.getCurrentHp();
            // Asumimos el peor caso (letal o casi letal). Para la estimación, restamos HP.
            int damage = evaluator.estimateBasicAttackDamage(game, actor, attackedTarget);
            attackedTarget.setCurrentHp(Math.max(0, previousHp - damage));
        }

        Player opponent = game.getOpponent(myPlayer);
        int maxPenalty = 0;

        // Fase 2: ¿Qué es lo peor que puede hacer el rival en respuesta al actor?
        if (actor != null && !actor.isDestroyed()) {
            for (Robot enemy : opponent.getAliveUnits()) {
                // Si este enemigo puede atacar al actor en su posición (hipotética) nueva
                if (evaluator.canAttackTarget(game, enemy, actor)) {
                    int expectedDmg = evaluator.estimateBasicAttackDamage(game, enemy, actor);
                    // Escala de dolor. Restar HP a mi robot es malo. Y matar es peor (GDD => *25 de
                    // peso e instakill => 1000).
                    int penalty = expectedDmg * 25;
                    if (actor.getCurrentHp() <= expectedDmg) {
                        penalty += 1000;
                    }
                    maxPenalty += penalty;
                }
            }
        }

        // Fase 3: Deshacer pre-ejecución simulada
        if (command instanceof MoveCommand) {
            actor.setPosition(originalHex);
            if (targetHex != null)
                targetHex.setOccupant(null);
            if (originalHex != null)
                originalHex.setOccupant(actor);
        } else if (command instanceof AttackCommand) {
            if (attackedTarget != null) {
                attackedTarget.setCurrentHp(previousHp);
            }
        }

        // Si es Skill de cura o movimiento instant, no implementamos el lookahead
        // complejo aquí por simplificar a MVP
        // complejo aquí por simplificar a MVP
        return maxPenalty;
    }

    private record ScoredDecision(GameCommand command, int score) {
    }
}
