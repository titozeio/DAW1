package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.engine.Game;
import com.titozeio.engine.GamePhase;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.RobotTemplate;
import com.titozeio.model.Objective;
import com.titozeio.model.Weapon;
import com.titozeio.skills.Skill;
import com.titozeio.ui.Screen;
import com.titozeio.victory.VictoryCondition;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainCoverageTest {

    @Test
    void robotTemplatesCreateConfiguredRobots() {
        Player owner = new Player("Owner");
        for (RobotTemplate template : RobotTemplate.values()) {
            Robot robot = template.createRobot(owner);
            assertSame(owner, robot.getOwner());
            assertNotNull(robot.getWeapon());
            assertNotNull(robot.getSkill());
            assertTrue(robot.getMaxHp() > 0);
            assertTrue(robot.getMovementPoints() > 0);
            assertTrue(robot.getWeapon().getRange() > 0);
        }
    }

    @Test
    void objectiveAndWeaponExposeStateAndTransitions() {
        Player p1 = new Player("P1");
        Player p2 = new Player("P2");

        Objective objective = new Objective("Obj", null);
        assertFalse(objective.isControlledBy(null));
        assertFalse(objective.isControlledBy(p1));

        objective.updateControl(p1);
        assertTrue(objective.isControlledBy(p1));
        assertEquals(1, objective.getControlTurns());

        objective.updateControl(p1);
        assertEquals(2, objective.getControlTurns());

        objective.updateControl(p2);
        assertTrue(objective.isControlledBy(p2));
        assertEquals(1, objective.getControlTurns());

        objective.updateControl(null);
        assertNull(objective.getControllingPlayer());
        assertEquals(0, objective.getControlTurns());
        assertTrue(objective.toString().contains("Obj"));

        Weapon weapon = new Weapon("Laser", 3, 4, "Desc", com.titozeio.enums.AttackType.ENERGY);
        assertEquals("Laser", weapon.getName());
        assertEquals(3, weapon.getRange());
        assertEquals(4, weapon.getDamage());
        assertEquals("Desc", weapon.getDescription());
        assertEquals(com.titozeio.enums.AttackType.ENERGY, weapon.getAttackType());
        assertTrue(weapon.toString().contains("Laser"));
    }

    @Test
    void gameCoreMethodsAndVictoryChecksWork() throws Exception {
        Game game = new Game(null);
        Player p1 = game.getP1();
        Player p2 = game.getP2();

        assertEquals(1, game.getTurnCounter());
        assertEquals(GamePhase.DEPLOYING, game.getPhase());
        assertSame(p1, game.getCurrentPlayer());
        assertSame(p2, game.getOpponent(p1));
        assertSame(p1, game.getOpponent(p2));

        game.nextTurn();
        assertEquals(2, game.getTurnCounter());
        assertSame(p2, game.getCurrentPlayer());

        game.setPhase(GamePhase.COMBAT);
        assertEquals(GamePhase.COMBAT, game.getPhase());

        Player np1 = new Player("NP1");
        Player np2 = new Player("NP2");
        game.setP1(np1);
        game.setP2(np2);
        assertSame(np1, game.getP1());
        assertSame(np2, game.getP2());

        StubScreen screen = new StubScreen();
        game.displayScreen(screen);
        assertTrue(screen.displayed);

        List<VictoryCondition> vcs = new ArrayList<>();
        vcs.add(new VictoryCondition() {
            @Override
            public Player check(Game g) {
                return null;
            }
        });
        vcs.add(new VictoryCondition() {
            @Override
            public Player check(Game g) {
                return g.getP2();
            }
        });

        Field field = Game.class.getDeclaredField("victoryConditions");
        field.setAccessible(true);
        field.set(game, vcs);
        assertSame(np2, game.checkVictory());

        game.handleInput("dummy");

    }

    @Test
    void skillBaseMethodsWork() {
        DummySkill skill = new DummySkill();
        assertTrue(skill.isReady());
        skill.resetCooldown();
        assertFalse(skill.isReady());
        skill.tickCooldown();
        assertEquals(1, skill.getCurrentCooldown());
        skill.tickCooldown();
        assertEquals(0, skill.getCurrentCooldown());
        assertEquals("Dummy", skill.getName());
        assertNotNull(skill.toString());
        assertEquals(1, skill.getDisplayStats().size());
    }

    static class StubScreen extends Screen {
        boolean displayed;

        @Override
        public void display() {
            displayed = true;
        }

        @Override
        public void handleInput(String input) {
        }
    }

    static class DummySkill extends Skill {
        DummySkill() {
            super("Dummy", com.titozeio.enums.SkillType.ATTACK, 2, "d");
        }

        @Override
        public void execute(Robot actor, Object target) {
        }

        @Override
        public List<String[]> getDisplayStats() {
            return java.util.Collections.singletonList(new String[] { "a", "b" });
        }
    }
}
