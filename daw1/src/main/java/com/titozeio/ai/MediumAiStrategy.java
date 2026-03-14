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
 * Estrategia de IA de nivel medio.
 * Hereda la lógica de utilidad de la IA fácil pero utiliza un evaluador
 * con sensibilidad al riesgo (exposición) y priorización táctica (Focus Fire).
 */
public class MediumAiStrategy implements AiStrategy {
    private final ActionEvaluator evaluator;

    /**
     * Construye la estrategia con un evaluador configurado.
     * 
     * @param evaluator Instancia del evaluador que aplicará los pesos de dificultad
     *                  media.
     */
    public MediumAiStrategy(ActionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Selecciona la mejor decisión para el conjunto de robots del jugador.
     * Evalúa todas las acciones legales de todos los robots vivos y elige la que
     * tenga mayor puntuación heurística.
     * 
     * @param context    Contexto actual de la partida.
     * @param player     Jugador que debe tomar la decisión.
     * @param turnEngine Motor de reglas para consultar acciones legales.
     * @return La decisión tomada (ejecutar comando o finalizar turno).
     */
    @Override
    public TurnDecision chooseDecision(MatchContext context, Player player, TurnEngine turnEngine) {
        Game game = context.getGame();
        if (!player.hasActionsLeft()) {
            return new EndTurnDecision();
        }

        // Buscamos la mejor acción globalmente entre todos los robots
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

    /**
     * Calcula y devuelve la mejor acción posible para un robot específico.
     * 
     * @param game       Estado del juego.
     * @param robot      Unidad a evaluar.
     * @param turnEngine Motor de reglas.
     * @return La mejor decisión puntuada o null si no tiene acciones legales.
     */
    private ScoredDecision scoreBestCommandForRobot(Game game, Robot robot, TurnEngine turnEngine) {
        List<GameCommand> commands = turnEngine.getLegalCommands(game, robot);
        if (commands.isEmpty()) {
            return null;
        }

        return commands.stream()
                .map(cmd -> new ScoredDecision(cmd, scoreCommand(game, robot, cmd)))
                .max(Comparator.comparingInt(ScoredDecision::score))
                .orElse(null);
    }

    /**
     * Delega en el evaluador la puntuación de un comando concreto.
     * 
     * @param game    Estado del juego.
     * @param robot   Robot que ejecuta el comando.
     * @param command Comando a puntuar.
     * @return Puntuación entera (mayor es mejor).
     */
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

    private record ScoredDecision(GameCommand command, int score) {
    }
}
