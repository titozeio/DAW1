package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.ai.ActionEvaluator;
import com.titozeio.ai.DarkSoulsAiStrategy;
import com.titozeio.ai.EndTurnDecision;
import com.titozeio.ai.ExecuteCommandDecision;
import com.titozeio.ai.HardAiStrategy;
import com.titozeio.ai.MatchContext;
import com.titozeio.ai.MediumAiStrategy;
import com.titozeio.ai.TurnDecision;
import com.titozeio.commands.AttackCommand;
import com.titozeio.commands.GameCommand;
import com.titozeio.commands.MoveCommand;
import com.titozeio.commands.SkillCommand;
import com.titozeio.engine.Game;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Map;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.TerrainType;
import com.titozeio.match.TurnEngine;
import com.titozeio.skills.FreezeRay;
import com.titozeio.skills.GuidedMissile;
import com.titozeio.skills.JetpackBoost;
import com.titozeio.skills.PlasmaSaberAttack;
import com.titozeio.skills.RepairNanobots;
import com.titozeio.skills.TurboPropulsion;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class AdvancedAiStrategiesTest {

    @Test
    void mediumHardAndDarkSoulsEndTurnWhenNoRobotCanAct() throws Exception {
        Game game = buildGameWithMap(3, 3);
        Player p1 = game.getP1();
        Robot exhausted = TestFixtures.robot("Exhausted", p1, 10, 4, 3, 4, new GuidedMissile());
        place(exhausted, game.getMap().getHexagon(0, 0), p1);
        exhausted.setUsedMovement(true);
        exhausted.setUsedAttack(true);

        TurnEngine turnEngine = new TurnEngine();
        MatchContext context = new MatchContext(game);

        assertInstanceOf(EndTurnDecision.class,
                new MediumAiStrategy(new ActionEvaluator()).chooseDecision(context, p1, turnEngine));
        assertInstanceOf(EndTurnDecision.class,
                new HardAiStrategy(new ActionEvaluator()).chooseDecision(context, p1, turnEngine));
        assertInstanceOf(EndTurnDecision.class,
                new DarkSoulsAiStrategy(new ActionEvaluator()).chooseDecision(context, p1, turnEngine));
    }

    @Test
    void mediumAndHardEvaluateAvailableCommandsAndChooseExecutableAction() throws Exception {
        Game game = buildGameWithMap(4, 4);
        Player p1 = game.getP1();
        Player p2 = game.getP2();
        TurnEngine turnEngine = new TurnEngine();

        Robot attacker = TestFixtures.robot("Attacker", p1, 10, 4, 3, 4, new GuidedMissile());
        Robot ally = TestFixtures.robot("Ally", p1, 10, 4, 3, 2, new RepairNanobots());
        Robot enemy = TestFixtures.robot("Enemy", p2, 6, 4, 3, 2, null);

        place(attacker, game.getMap().getHexagon(0, 1), p1);
        place(ally, game.getMap().getHexagon(0, 2), p1);
        place(enemy, game.getMap().getHexagon(2, 1), p2);
        ally.takeDamage(2);

        TurnDecision mediumDecision = new MediumAiStrategy(new ActionEvaluator()).chooseDecision(
                new MatchContext(game), p1, turnEngine);
        TurnDecision hardDecision = new HardAiStrategy(new ActionEvaluator()).chooseDecision(
                new MatchContext(game), p1, turnEngine);

        assertInstanceOf(ExecuteCommandDecision.class, mediumDecision);
        assertInstanceOf(ExecuteCommandDecision.class, hardDecision);
        assertNotNull(((ExecuteCommandDecision) mediumDecision).getCommand());
        assertNotNull(((ExecuteCommandDecision) hardDecision).getCommand());
    }

    @Test
    void darkSoulsStrategySearchAndSimulationHelpersCoverMainBranches() throws Exception {
        Game game = buildGameWithMap(5, 5);
        Player p1 = game.getP1();
        Player p2 = game.getP2();
        TurnEngine turnEngine = new TurnEngine();
        DarkSoulsAiStrategy strategy = new DarkSoulsAiStrategy(new ActionEvaluator());

        Robot mover = TestFixtures.robot("Mover", p1, 10, 4, 3, 4, new JetpackBoost());
        Robot healer = TestFixtures.robot("Healer", p1, 10, 4, 3, 2, new RepairNanobots());
        Robot missile = TestFixtures.robot("Missile", p1, 10, 4, 3, 2, new GuidedMissile());
        Robot freezer = TestFixtures.robot("Freezer", p1, 10, 4, 3, 2, new FreezeRay());
        Robot saber = TestFixtures.robot("Saber", p1, 10, 4, 1, 4, new PlasmaSaberAttack());
        Robot turbo = TestFixtures.robot("Turbo", p1, 10, 4, 3, 2, new TurboPropulsion());

        Robot enemyA = TestFixtures.robot("EnemyA", p2, 10, 4, 3, 2, null);
        Robot enemyB = TestFixtures.robot("EnemyB", p2, 10, 4, 3, 2, null);

        place(mover, game.getMap().getHexagon(0, 0), p1);
        place(healer, game.getMap().getHexagon(0, 1), p1);
        place(missile, game.getMap().getHexagon(1, 0), p1);
        place(freezer, game.getMap().getHexagon(1, 1), p1);
        place(saber, game.getMap().getHexagon(2, 1), p1);
        place(turbo, game.getMap().getHexagon(0, 2), p1);

        place(enemyA, game.getMap().getHexagon(2, 0), p2);
        place(enemyB, game.getMap().getHexagon(3, 1), p2);
        healer.takeDamage(3);

        TurnDecision decision = strategy.chooseDecision(new MatchContext(game), p1, turnEngine);
        assertInstanceOf(ExecuteCommandDecision.class, decision);

        Method executeSimulatedCommand = DarkSoulsAiStrategy.class.getDeclaredMethod(
                "executeSimulatedCommand", GameCommand.class, Game.class);
        executeSimulatedCommand.setAccessible(true);

        Hexagon moveTarget = game.getMap().getHexagon(2, 2);
        executeSimulatedCommand.invoke(strategy, new MoveCommand(mover, moveTarget), game);
        assertSame(moveTarget, mover.getPosition());
        assertTrue(mover.isUsedMovement());

        int enemyABefore = enemyA.getCurrentHp();
        executeSimulatedCommand.invoke(strategy, new AttackCommand(saber, enemyA, game.getMap()), game);
        assertTrue(enemyA.getCurrentHp() <= enemyABefore);
        assertTrue(saber.isUsedAttack());

        healer.setUsedAttack(false);
        healer.setUsedMovement(false);
        int healerHpBefore = healer.getCurrentHp();
        executeSimulatedCommand.invoke(strategy, new SkillCommand(healer, healer), game);
        assertTrue(healer.getCurrentHp() >= healerHpBefore);
        assertTrue(healer.isUsedAttack());
        assertTrue(healer.isUsedMovement());

        missile.setUsedAttack(false);
        missile.setUsedMovement(false);
        int enemyBBefore = enemyB.getCurrentHp();
        executeSimulatedCommand.invoke(strategy, new SkillCommand(missile, enemyB), game);
        assertTrue(enemyB.getCurrentHp() < enemyBBefore);

        freezer.setUsedAttack(false);
        freezer.setUsedMovement(false);
        enemyABefore = enemyA.getCurrentHp();
        executeSimulatedCommand.invoke(strategy, new SkillCommand(freezer, enemyA), game);
        assertTrue(enemyA.getCurrentHp() <= enemyABefore);

        saber.setUsedAttack(false);
        saber.setUsedMovement(false);
        enemyABefore = enemyA.getCurrentHp();
        executeSimulatedCommand.invoke(strategy, new SkillCommand(saber, enemyA), game);
        assertTrue(enemyA.getCurrentHp() <= enemyABefore);

        turbo.setUsedAttack(false);
        turbo.setUsedMovement(false);
        Hexagon turboTarget = game.getMap().getHexagon(2, 3);
        executeSimulatedCommand.invoke(strategy, new SkillCommand(turbo, turboTarget), game);
        assertSame(turboTarget, turbo.getPosition());

        Method evaluateBoard = DarkSoulsAiStrategy.class.getDeclaredMethod(
                "evaluateBoardDarkSouls", Game.class, Player.class);
        evaluateBoard.setAccessible(true);
        int score = (Integer) evaluateBoard.invoke(strategy, game, p1);
        assertNotEquals(0, score);
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
