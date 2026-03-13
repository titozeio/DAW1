package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.ai.ActionEvaluator;
import com.titozeio.ai.AiController;
import com.titozeio.ai.AiDifficulty;
import com.titozeio.ai.AiStrategy;
import com.titozeio.ai.EasyAiStrategy;
import com.titozeio.ai.EndTurnDecision;
import com.titozeio.ai.ExecuteCommandDecision;
import com.titozeio.ai.HumanController;
import com.titozeio.ai.MatchContext;
import com.titozeio.ai.TurnDecision;
import com.titozeio.commands.GameCommand;
import com.titozeio.commands.MoveCommand;
import com.titozeio.commands.SkillCommand;
import com.titozeio.engine.Game;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Map;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.RobotState;
import com.titozeio.enums.RobotTemplate;
import com.titozeio.enums.TerrainType;
import com.titozeio.match.BatchSimulationConfig;
import com.titozeio.match.BatchSimulationService;
import com.titozeio.match.MatchResult;
import com.titozeio.match.MatchRunner;
import com.titozeio.match.TurnEngine;
import com.titozeio.skills.FreezeRay;
import com.titozeio.skills.GuidedMissile;
import com.titozeio.skills.RepairNanobots;
import com.titozeio.skills.TurboPropulsion;
import com.titozeio.victory.ObjectiveControlVC;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class AiMatchCoreTest {

    @Test
    void actionEvaluatorScoresMovesAttacksAndSkills() throws Exception {
        Game game = buildGameWithMap(3, 3);
        Player p1 = game.getP1();
        Player p2 = game.getP2();

        Hexagon actorHex = game.getMap().getHexagon(0, 1);
        Hexagon moveHex = game.getMap().getHexagon(1, 0);
        Hexagon enemyHex = game.getMap().getHexagon(2, 1);
        Hexagon p1Base = game.getMap().getHexagon(0, 0);
        Hexagon p2Base = game.getMap().getHexagon(2, 2);
        moveHex.setTerrain(TerrainType.VEGETATION);

        p1.setBaseLocation(p1Base);
        p2.setBaseLocation(p2Base);

        Robot mover = TestFixtures.robot("Mover", p1, 10, 4, 3, 4, new TurboPropulsion());
        Robot enemy = TestFixtures.robot("Enemy", p2, 4, 4, 3, 3, null);
        place(mover, actorHex, p1);
        place(enemy, enemyHex, p2);

        ActionEvaluator evaluator = new ActionEvaluator();
        assertEquals(110, evaluator.scoreMove(game, mover, moveHex));

        enemyHex.setOccupant(null);
        place(enemy, p1Base, p2);
        assertEquals(1450, evaluator.scoreAttack(game, mover, enemy));

        Robot freezer = TestFixtures.robot("Freezer", p1, 10, 4, 3, 2, new FreezeRay());
        place(freezer, actorHex, p1);
        assertEquals(510, evaluator.scoreSkill(game, freezer, enemy));

        Robot missile = TestFixtures.robot("Missile", p1, 10, 4, 3, 2, new GuidedMissile());
        place(missile, actorHex, p1);
        enemy.takeDamage(1);
        assertEquals(1370, evaluator.scoreSkill(game, missile, enemy));

        Robot healer = TestFixtures.robot("Healer", p1, 10, 4, 3, 2, new RepairNanobots());
        Robot ally = TestFixtures.robot("Ally", p1, 10, 4, 3, 2, null);
        place(healer, actorHex, p1);
        place(ally, moveHex, p1);
        ally.takeDamage(3);
        assertEquals(240, evaluator.scoreSkill(game, healer, ally));
        assertNotEquals(Integer.MIN_VALUE, evaluator.scoreSkill(game, mover, p2Base));

        Robot noSkill = TestFixtures.robot("NoSkill", p1, 10, 4, 3, 2, null);
        assertEquals(Integer.MIN_VALUE, evaluator.scoreSkill(game, noSkill, ally));
    }

    @Test
    void easyAiStrategyControllersAndDecisionsUseTurnEngine() throws Exception {
        Game game = buildGameWithMap(3, 3);
        Player p1 = game.getP1();
        Player p2 = game.getP2();
        TurnEngine turnEngine = new TurnEngine();

        Robot attacker = TestFixtures.robot("Attacker", p1, 10, 4, 3, 4, new GuidedMissile());
        Robot enemy = TestFixtures.robot("Enemy", p2, 3, 4, 3, 2, null);
        place(attacker, game.getMap().getHexagon(0, 1), p1);
        place(enemy, game.getMap().getHexagon(1, 1), p2);

        EasyAiStrategy strategy = new EasyAiStrategy(new ActionEvaluator());
        TurnDecision decision = strategy.chooseDecision(new MatchContext(game), p1, turnEngine);
        assertInstanceOf(ExecuteCommandDecision.class, decision);
        assertInstanceOf(SkillCommand.class, ((ExecuteCommandDecision) decision).getCommand());

        attacker.setUsedMovement(true);
        attacker.setUsedAttack(true);
        assertInstanceOf(EndTurnDecision.class, strategy.chooseDecision(new MatchContext(game), p1, turnEngine));

        TurnDecision delegated = new EndTurnDecision();
        AiStrategy fakeStrategy = (context, player, engine) -> delegated;
        AiController aiController = new AiController(fakeStrategy, turnEngine);
        assertSame(delegated, aiController.decideNextStep(new MatchContext(game), p1));
        assertTrue(aiController.isAiControlled());

        HumanController humanController = new HumanController();
        assertNull(humanController.decideNextStep(new MatchContext(game), p1));
        assertFalse(humanController.isAiControlled());

        assertEquals(game, new MatchContext(game).getGame());
        assertEquals("Fácil", AiDifficulty.EASY.getDisplayName());
        assertEquals("Fácil", AiDifficulty.EASY.toString());
        assertNotNull(
                new ExecuteCommandDecision(new MoveCommand(attacker, game.getMap().getHexagon(0, 0))).getCommand());
        assertNotNull(new EndTurnDecision());
    }

    @Test
    void turnEngineHandlesDeployMovementAttackSkillsAndEndTurn() throws Exception {
        Game game = buildGameWithMap(4, 4);
        Player p1 = game.getP1();
        Player p2 = game.getP2();
        TurnEngine turnEngine = new TurnEngine();

        Hexagon p1Deploy = game.getMap().getHexagon(0, 0);
        Hexagon p1Deploy2 = game.getMap().getHexagon(1, 0);
        Hexagon p2Deploy = game.getMap().getHexagon(3, 3);
        p1Deploy.setDeployZoneP1(true);
        p1Deploy2.setDeployZoneP1(true);
        p2Deploy.setDeployZoneP2(true);

        Robot attacker = TestFixtures.robot("Attacker", p1, 10, 4, 3, 4, new GuidedMissile());
        Robot ally = TestFixtures.robot("Ally", p1, 10, 4, 3, 2, new RepairNanobots());
        Robot enemy = TestFixtures.robot("Enemy", p2, 4, 4, 3, 2, null);

        Hexagon attackerHex = game.getMap().getHexagon(0, 1);
        Hexagon allyHex = game.getMap().getHexagon(0, 2);
        Hexagon blocker = game.getMap().getHexagon(1, 1);
        Hexagon enemyHex = game.getMap().getHexagon(2, 1);
        Hexagon deepWater = game.getMap().getHexagon(1, 2);
        Hexagon turboOrigin = game.getMap().getHexagon(2, 2);
        Hexagon steep = game.getMap().getHexagon(3, 2);
        blocker.setHeight(4);
        deepWater.setTerrain(TerrainType.WATER_DEEP);
        steep.setHeight(4);

        place(attacker, attackerHex, p1);
        place(ally, allyHex, p1);
        place(enemy, enemyHex, p2);

        assertEquals(2, turnEngine.getDeployHexes(game, p1).size());
        assertTrue(turnEngine.getAttackTargets(game, attacker).isEmpty(),
                "La cobertura total bloquea el ataque normal");
        assertTrue(turnEngine.getSkillAttackTargets(game, attacker).contains(enemyHex),
                "GuidedMissile debe ignorar la visibilidad");

        ally.takeDamage(2);
        assertTrue(turnEngine.getSkillAttackTargets(game, ally).contains(attackerHex));
        assertFalse(turnEngine.canUseSkill(null));

        List<GameCommand> attackerCommands = turnEngine.getLegalCommands(game, attacker);
        assertTrue(attackerCommands.stream().anyMatch(MoveCommand.class::isInstance));
        assertTrue(attackerCommands.stream().anyMatch(SkillCommand.class::isInstance));

        Robot turbo = TestFixtures.robot("Turbo", p1, 10, 4, 3, 2, new TurboPropulsion());
        place(turbo, turboOrigin, p1);
        var turboTargets = turnEngine.getSkillMovementTargets(game, turbo);
        assertFalse(turboTargets.contains(deepWater));
        assertFalse(turboTargets.contains(steep));

        Robot freeze = TestFixtures.robot("Freeze", p1, 10, 4, 3, 2, new FreezeRay());
        place(freeze, attackerHex, p1);
        freeze.getSkill().resetCooldown();
        assertFalse(turnEngine.canUseSkill(freeze));
        freeze.applyState(RobotState.FROZEN, 1);
        assertTrue(turnEngine.getLegalCommands(game, freeze).isEmpty(), "un robot congelado no debe tener comandos");

        MoveCommand move = new MoveCommand(attacker, p1Deploy);
        turnEngine.execute(move);
        assertSame(p1Deploy, attacker.getPosition());

        Robot nextPlayerRobot = TestFixtures.robot("Next", p2, 10, 4, 3, 2, null);
        place(nextPlayerRobot, p2Deploy, p2);
        nextPlayerRobot.setUsedMovement(true);
        nextPlayerRobot.setUsedAttack(true);
        turnEngine.endTurn(game);
        assertSame(p2, game.getCurrentPlayer());
        assertFalse(nextPlayerRobot.isUsedMovement());
        assertFalse(nextPlayerRobot.isUsedAttack());
    }

    @Test
    void matchRunnerAndBatchSimulationServiceProduceDeterministicCoreResults() {
        MatchRunner runner = new MatchRunner(new TurnEngine());
        List<RobotTemplate> team1 = List.of(RobotTemplate.BULLSEYE, RobotTemplate.SCOUT, RobotTemplate.BULWARK);
        List<RobotTemplate> team2 = List.of(RobotTemplate.ICE_AGE, RobotTemplate.DEATH_KNIGHT,
                RobotTemplate.VICTORY_SABER);

        MatchResult result = runner.runMatch(7L, 99L, AiDifficulty.EASY, AiDifficulty.MEDIUM, team1, team2, 0);
        assertEquals(7L, result.matchId());
        assertEquals(99L, result.seed());
        assertEquals("EASY", result.difficultyP1());
        assertEquals("MEDIUM", result.difficultyP2());
        assertEquals("Empate", result.winner());
        assertEquals("timeout", result.victoryType());
        assertEquals(1, result.turnCount());
        assertEquals(3, result.teamP1().size());
        assertEquals(3, result.teamP2().size());
        assertEquals(3, result.survivorsP1().size());
        assertEquals(3, result.survivorsP2().size());
        assertTrue(result.destroyedP1().isEmpty());
        assertTrue(result.destroyedP2().isEmpty());
        assertTrue(result.killsByRobot().isEmpty());

        AtomicInteger callbacks = new AtomicInteger();
        MatchRunner fakeRunner = new MatchRunner(new TurnEngine()) {
            @Override
            public MatchResult runMatch(long matchId, long seed, AiDifficulty difficultyP1, AiDifficulty difficultyP2,
                    List<RobotTemplate> batchTeamP1, List<RobotTemplate> batchTeamP2, int maxTurns) {
                assertEquals(3, batchTeamP1.size());
                assertEquals(3, batchTeamP2.size());
                return new MatchResult(
                        matchId,
                        seed,
                        difficultyP1.name(),
                        difficultyP2.name(),
                        List.of("A", "B", "C"),
                        List.of("D", "E", "F"),
                        matchId == 1 ? "Jugador 1" : "Empate",
                        matchId == 1 ? "eliminación" : "timeout",
                        4,
                        List.of("A"),
                        List.of(),
                        List.of("B"),
                        List.of("D"),
                        java.util.Map.of("A", 1),
                        java.util.Map.of("A", 1),
                        java.util.Map.of("A", 1));
            }
        };

        BatchSimulationService service = new BatchSimulationService(fakeRunner);
        BatchSimulationConfig config = new BatchSimulationConfig(
                3, 123L, 5, AiDifficulty.EASY, AiDifficulty.HARD, null, null);

        var stats = service.runBatch(config, ignored -> callbacks.incrementAndGet(), () -> callbacks.get() >= 2);
        assertEquals(2, callbacks.get());
        assertEquals(2, stats.getResults().size());
    }

    @Test
    void objectiveControlVictoryConditionReturnsControllerOnlyWhenThresholdMet() {
        Game game = new Game(null);
        var objective = new com.titozeio.model.Objective("Central", null);
        ObjectiveControlVC vc = new ObjectiveControlVC(objective, 2);

        assertNull(vc.check(game));
        objective.updateControl(game.getP1());
        assertNull(vc.check(game));
        objective.updateControl(game.getP1());
        assertSame(game.getP1(), vc.check(game));
        assertSame(objective, vc.getTargetObjective());
        assertEquals(2, vc.getTurnsToControl());
    }

    private static Game buildGameWithMap(int rows, int cols) throws Exception {
        Game game = new Game(null);
        Map map = new Map(rows, cols);
        for (int r = 0; r < rows; r++) {
            for (int q = 0; q < cols; q++) {
                map.setHexagon(q, r, TestFixtures.hex(q, r, TerrainType.NORMAL, 0));
            }
        }
        setField(game, "map", map);
        game.getP1().getUnits().clear();
        game.getP2().getUnits().clear();
        return game;
    }

    private static void place(Robot robot, Hexagon hex, Player owner) {
        if (!owner.getUnits().contains(robot)) {
            owner.addUnit(robot);
        }
        if (hex.getOccupant() != null && hex.getOccupant() != robot) {
            hex.getOccupant().setPosition(null);
        }
        hex.setOccupant(robot);
        robot.setPosition(hex);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
