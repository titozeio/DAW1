package com.titozeio.ai;

import com.titozeio.commands.AttackCommand;
import com.titozeio.commands.GameCommand;
import com.titozeio.commands.MoveCommand;
import com.titozeio.commands.SkillCommand;
import com.titozeio.engine.Game;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.match.TurnEngine;

import java.util.Comparator;
import java.util.List;

/**
 * IA fácil basada en utilidad simple y determinista.
 */
public class EasyAiStrategy implements AiStrategy {
    private final ActionEvaluator evaluator;

    public EasyAiStrategy(ActionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public TurnDecision chooseDecision(MatchContext context, Player player, TurnEngine turnEngine) {
        Game game = context.getGame();
        if (!player.hasActionsLeft()) {
            return new EndTurnDecision();
        }

        ScoredDecision best = player.getAliveUnits().stream()
                .filter(robot -> !robot.isExhausted())
                .map(robot -> scoreBestCommandForRobot(game, robot, turnEngine))
                .filter(scored -> scored != null)
                .max(Comparator.comparingInt(ScoredDecision::score))
                .orElse(null);

        if (best == null) {
            return new EndTurnDecision();
        }
        return new ExecuteCommandDecision(best.command());
    }

    private ScoredDecision scoreBestCommandForRobot(Game game, Robot robot, TurnEngine turnEngine) {
        List<GameCommand> commands = turnEngine.getLegalCommands(game, robot);
        return commands.stream()
                .map(command -> new ScoredDecision(command, scoreCommand(game, command)))
                .max(Comparator.comparingInt(ScoredDecision::score))
                .orElse(null);
    }

    private int scoreCommand(Game game, GameCommand command) {
        if (command instanceof AttackCommand attackCommand) {
            return evaluator.scoreAttack(game, attackCommand.getAttacker(), attackCommand.getTarget());
        }
        if (command instanceof SkillCommand skillCommand) {
            return evaluator.scoreSkill(game, skillCommand.getRobot(), skillCommand.getTarget());
        }
        if (command instanceof MoveCommand moveCommand) {
            return evaluator.scoreMove(game, moveCommand.getRobot(), moveCommand.getTargetHex());
        }
        return Integer.MIN_VALUE;
    }

    private record ScoredDecision(GameCommand command, int score) {
    }
}
