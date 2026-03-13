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
 * Estrategia de IA de nivel fácil.
 * Utiliza un sistema de puntuación heurística simple y determinista para
 * decidir
 * qué acción (mover, atacar o usar skill) de qué robot vivo proporciona el
 * mayor
 * beneficio inmediato ("utility model").
 */
public class EasyAiStrategy implements AiStrategy {
    private final ActionEvaluator evaluator;

    public EasyAiStrategy(ActionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public TurnDecision chooseDecision(MatchContext context, Player player, TurnEngine turnEngine) {
        Game game = context.getGame();
        // Si el jugador ya no tiene acciones globales (o robots hábiles), terminamos el
        // turno.
        if (!player.hasActionsLeft()) {
            return new EndTurnDecision();
        }

        // Buscamos la mejor acción entre todos los robots que aún no se han agotado.
        ScoredDecision best = player.getAliveUnits().stream()
                .filter(robot -> !robot.isExhausted())
                .map(robot -> scoreBestCommandForRobot(game, robot, turnEngine))
                .filter(scored -> scored != null)
                .max(Comparator.comparingInt(ScoredDecision::score))
                .orElse(null);

        // Si no encontramos ninguna acción válida con puntuación aceptable, terminamos
        // el turno.
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
