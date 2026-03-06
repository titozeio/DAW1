package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.engine.Map;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.RobotState;
import com.titozeio.enums.SkillType;
import com.titozeio.enums.TerrainType;
import com.titozeio.skills.Skill;
import java.util.List;
import org.junit.jupiter.api.Test;

class RobotTest {

    @Test
    void moveAppliesFallDamageAndOccupancy() {
        Player p = new Player("P1");
        Robot robot = TestFixtures.robot("A", p, 10, 3, 3, 4, null);
        var from = TestFixtures.hex(0, 0, TerrainType.NORMAL, 2);
        var to = TestFixtures.hex(1, 0, TerrainType.NORMAL, 0);
        robot.setPosition(from);
        from.setOccupant(robot);

        robot.move(to);

        assertSame(robot, to.getOccupant());
        assertNull(from.getOccupant());
        assertTrue(robot.isUsedMovement());
        assertEquals(9, robot.getCurrentHp());

        robot.move(from);
        assertSame(to, robot.getPosition());
    }

    @Test
    void attackAndCanAttackRespectRules() {
        Player p1 = new Player("P1");
        Player p2 = new Player("P2");
        Robot attacker = TestFixtures.robot("AT", p1, 20, 3, 3, 8, null);
        Robot defender = TestFixtures.robot("DF", p2, 20, 3, 3, 4, null);

        Map map = new Map(1, 3);
        var a = TestFixtures.hex(0, 0, TerrainType.WATER_SHALLOW, 0);
        var mid = TestFixtures.hex(1, 0, TerrainType.NORMAL, 0);
        var d = TestFixtures.hex(2, 0, TerrainType.VEGETATION, 0);
        map.setHexagon(0, 0, a);
        map.setHexagon(1, 0, mid);
        map.setHexagon(2, 0, d);

        attacker.setPosition(a);
        defender.setPosition(d);

        assertTrue(attacker.canAttack(defender, map));
        attacker.attack(defender, map);
        assertTrue(attacker.isUsedAttack());
        assertTrue(defender.getCurrentHp() < 20);

        int hp = defender.getCurrentHp();
        attacker.attack(defender, map);
        assertEquals(hp, defender.getCurrentHp());

        defender.setPosition(null);
        assertFalse(attacker.canAttack(defender, map));
    }

    @Test
    void statusesDamageHealAndResetActionsWork() {
        Player p = new Player("P");
        CoolSkill skill = new CoolSkill();
        Robot r = TestFixtures.robot("R", p, 10, 2, 1, 3, skill);
        var pos = TestFixtures.hex(0, 0, TerrainType.VEGETATION, 0);
        r.setPosition(pos);

        r.takeDamage(8);
        assertEquals(4, r.getCurrentHp());

        r.heal(100);
        assertEquals(10, r.getCurrentHp());

        r.applyState(RobotState.FROZEN, 1);
        assertTrue(r.hasState(RobotState.FROZEN));
        r.tickStatusEffects();
        assertFalse(r.hasState(RobotState.FROZEN));

        r.setUsedAttack(true);
        r.setUsedMovement(true);
        skill.resetCooldown();
        r.resetActions();
        assertFalse(r.isUsedAttack());
        assertFalse(r.isUsedMovement());
        assertEquals(1, skill.getCurrentCooldown());

        assertFalse(r.isDestroyed());
        r.takeDamage(1000);
        assertTrue(r.isDestroyed());
        assertTrue(r.toString().contains("R"));
    }

    @Test
    void useSkillHonorsNullAndCooldownChecks() {
        Player p = new Player("P");
        Robot noSkill = TestFixtures.robot("R1", p, 10, 2, 1, 3, null);
        noSkill.useSkill(new Object());

        CoolSkill skill = new CoolSkill();
        Robot withSkill = TestFixtures.robot("R2", p, 10, 2, 1, 3, skill);
        skill.resetCooldown();
        withSkill.useSkill(new Object());
        assertFalse(skill.executed);

        skill.setCurrentCooldownForTest(0);
        withSkill.useSkill(new Object());
        assertTrue(skill.executed);
    }

    static class CoolSkill extends Skill {
        boolean executed;

        CoolSkill() {
            super("cool", SkillType.ATTACK, 2, "d");
        }

        void setCurrentCooldownForTest(int value) {
            this.currentCooldown = value;
        }

        @Override
        public void execute(Robot actor, Object target) {
            executed = true;
        }

        @Override
        public List<String[]> getDisplayStats() {
            return java.util.Collections.singletonList(new String[] { "x", "1" });
        }
    }
}
