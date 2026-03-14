package com.titozeio.match;

import com.titozeio.ai.AiDifficulty;
import com.titozeio.ai.MatchContext;
import com.titozeio.ai.PlayerController;
import com.titozeio.ai.TurnDecision;
import com.titozeio.ai.ExecuteCommandDecision;
import com.titozeio.ai.EndTurnDecision;
import com.titozeio.engine.Game;
import com.titozeio.engine.GameMode;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.RobotTemplate;
import com.titozeio.commands.AttackCommand;
import com.titozeio.commands.GameCommand;
import com.titozeio.commands.SkillCommand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Ejecutor headless de partidas completas para simulación.
 */
public class MatchRunner {
    private final TurnEngine turnEngine;

    public MatchRunner(TurnEngine turnEngine) {
        this.turnEngine = turnEngine;
    }

    public MatchResult runMatch(long matchId, long seed, AiDifficulty difficultyP1, AiDifficulty difficultyP2,
            List<RobotTemplate> teamP1, List<RobotTemplate> teamP2, int maxTurns) {
        Game game = new Game(null);
        game.configureControllers(GameMode.EVE, difficultyP1, difficultyP2);

        populateTeams(game, teamP1, teamP2);
        assignBases(game);
        deployTeams(game, new Random(seed));

        Player winner = null;
        String victoryType = "timeout";
        int safety = 0;
        Map<String, Integer> killsByRobot = new HashMap<>();
        Map<String, Integer> damageDealtByRobot = new HashMap<>();
        Map<String, Integer> damageReceivedByRobot = new HashMap<>();
        while (winner == null && game.getTurnCounter() <= maxTurns && safety < maxTurns * 12) {
            Player current = game.getCurrentPlayer();
            PlayerController controller = game.getController(current);
            TurnDecision decision = controller.decideNextStep(new MatchContext(game), current);
            if (decision == null || decision instanceof EndTurnDecision) {
                turnEngine.endTurn(game);
                winner = game.checkVictory();
                if (winner != null) {
                    victoryType = resolveVictoryType(game, winner);
                    break;
                }
            } else if (decision instanceof ExecuteCommandDecision executeCommandDecision) {
                GameCommand command = executeCommandDecision.getCommand();

                Map<Robot, Integer> hpMap = new HashMap<>();
                for (Robot r : game.getP1().getUnits())
                    hpMap.put(r, r.getCurrentHp());
                for (Robot r : game.getP2().getUnits())
                    hpMap.put(r, r.getCurrentHp());

                turnEngine.execute(command);

                Robot actor = null;
                if (command instanceof AttackCommand ac)
                    actor = ac.getAttacker();
                else if (command instanceof SkillCommand sc)
                    actor = sc.getRobot();

                int totalDamageDealtThisTurn = 0;
                for (Player p : List.of(game.getP1(), game.getP2())) {
                    for (Robot r : p.getUnits()) {
                        int before = hpMap.getOrDefault(r, r.getCurrentHp());
                        int lost = before - r.getCurrentHp();
                        if (lost > 0) {
                            damageReceivedByRobot.merge(r.getModelName(), lost, Integer::sum);
                            if (actor != null && r != actor) {
                                totalDamageDealtThisTurn += lost;
                            }
                        }
                    }
                }

                if (actor != null && totalDamageDealtThisTurn > 0) {
                    damageDealtByRobot.merge(actor.getModelName(), totalDamageDealtThisTurn, Integer::sum);
                }

                registerKill(command, killsByRobot);
                cleanupDestroyedRobots(game);
                winner = game.checkVictory();
                if (winner != null) {
                    victoryType = resolveVictoryType(game, winner);
                    break;
                }
            }
            safety++;
        }

        return new MatchResult(
                matchId,
                seed,
                difficultyP1.name(),
                difficultyP2.name(),
                toNames(game.getP1().getUnits()),
                toNames(game.getP2().getUnits()),
                winner != null ? winner.getName() : "Empate",
                victoryType,
                game.getTurnCounter(),
                toNames(game.getP1().getAliveUnits()),
                toNames(game.getP2().getAliveUnits()),
                toDestroyedNames(game.getP1().getUnits()),
                toDestroyedNames(game.getP2().getUnits()),
                Map.copyOf(killsByRobot),
                Map.copyOf(damageDealtByRobot),
                Map.copyOf(damageReceivedByRobot));
    }

    private void populateTeams(Game game, List<RobotTemplate> teamP1, List<RobotTemplate> teamP2) {
        game.getP1().getUnits().clear();
        game.getP2().getUnits().clear();
        for (RobotTemplate template : teamP1) {
            game.getP1().addUnit(template.createRobot(game.getP1()));
        }
        for (RobotTemplate template : teamP2) {
            game.getP2().addUnit(template.createRobot(game.getP2()));
        }
    }

    private void assignBases(Game game) {
        for (Hexagon[] row : game.getMap().getGrid()) {
            for (Hexagon hex : row) {
                if (hex == null) {
                    continue;
                }
                if (hex.isBaseP1()) {
                    game.getP1().setBaseLocation(hex);
                }
                if (hex.isBaseP2()) {
                    game.getP2().setBaseLocation(hex);
                }
            }
        }
    }

    private void deployTeams(Game game, Random random) {
        deploySide(game, game.getP1(), turnEngine.getDeployHexes(game, game.getP1()), random);
        deploySide(game, game.getP2(), turnEngine.getDeployHexes(game, game.getP2()), random);
    }

    private void deploySide(Game game, Player player, Set<Hexagon> deployHexes, Random random) {
        List<Hexagon> positions = new ArrayList<>(new LinkedHashSet<>(deployHexes));
        positions.sort(Comparator.comparingInt(Hexagon::getR).thenComparingInt(Hexagon::getQ));
        java.util.Collections.shuffle(positions, random);
        for (int i = 0; i < player.getUnits().size() && i < positions.size(); i++) {
            Robot robot = player.getUnits().get(i);
            Hexagon target = positions.get(i);
            target.setOccupant(robot);
            robot.setPosition(target);
        }
    }

    private String resolveVictoryType(Game game, Player winner) {
        if (game.getOpponent(winner).isEliminated()) {
            return "eliminación";
        }
        return "conquista de base";
    }

    private List<String> toNames(List<Robot> robots) {
        return robots.stream().map(Robot::getModelName).toList();
    }

    private List<String> toDestroyedNames(List<Robot> robots) {
        return robots.stream().filter(Robot::isDestroyed).map(Robot::getModelName).toList();
    }

    private void registerKill(GameCommand command, Map<String, Integer> killsByRobot) {
        if (command instanceof AttackCommand attackCommand && attackCommand.isFatal()) {
            killsByRobot.merge(attackCommand.getAttacker().getModelName(), 1, Integer::sum);
            return;
        }
        if (command instanceof SkillCommand skillCommand
                && skillCommand.getTarget() instanceof Robot target
                && target.isDestroyed()) {
            killsByRobot.merge(skillCommand.getRobot().getModelName(), 1, Integer::sum);
        }
    }

    private void cleanupDestroyedRobots(Game game) {
        for (Player player : List.of(game.getP1(), game.getP2())) {
            for (Robot robot : player.getUnits()) {
                if (robot.isDestroyed() && robot.getPosition() != null) {
                    robot.getPosition().setOccupant(null);
                    robot.setPosition(null);
                }
            }
        }
    }
}
