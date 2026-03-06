package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.TerrainType;
import com.titozeio.skills.FreezeRay;
import com.titozeio.skills.GuidedMissile;
import com.titozeio.skills.JetpackBoost;
import com.titozeio.skills.PlasmaSaberAttack;
import com.titozeio.skills.RepairNanobots;
import com.titozeio.skills.TurboPropulsion;
import org.junit.jupiter.api.Test;

class SkillsTest {

    @Test
    void freezeRayDamagesFreezesAndCooldown() {
        Player p1 = new Player("P1");
        Player p2 = new Player("P2");
        FreezeRay skill = new FreezeRay();
        Robot actor = TestFixtures.robot("A", p1, 10, 3, 1, 3, skill);
        Robot target = TestFixtures.robot("T", p2, 10, 3, 1, 3, null);
        actor.setPosition(TestFixtures.hex(0, 0, TerrainType.NORMAL, 0));
        target.setPosition(TestFixtures.hex(1, 0, TerrainType.NORMAL, 0));

        skill.execute(actor, target);

        assertTrue(actor.isUsedAttack());
        assertEquals(2, skill.getCurrentCooldown());
        assertTrue(target.hasState(com.titozeio.enums.RobotState.FROZEN));
        assertEquals(7, target.getCurrentHp());
    }

    @Test
    void guidedMissileAndPlasmaSaberDealDamage() {
        Player p1 = new Player("P1");
        Player p2 = new Player("P2");
        Robot actor = TestFixtures.robot("A", p1, 20, 3, 1, 3, null);
        Robot target = TestFixtures.robot("T", p2, 20, 3, 1, 3, null);
        actor.setPosition(TestFixtures.hex(0, 0, TerrainType.NORMAL, 0));
        target.setPosition(TestFixtures.hex(1, 0, TerrainType.NORMAL, 0));

        GuidedMissile gm = new GuidedMissile();
        gm.execute(actor, target);
        assertEquals(15, target.getCurrentHp());
        assertTrue(gm.isIgnoresCover());
        assertTrue(gm.isIgnoresVisibility());

        target.heal(100);
        PlasmaSaberAttack saber = new PlasmaSaberAttack();
        saber.execute(actor, target);
        assertEquals(10, target.getCurrentHp());
    }

    @Test
    void repairNanobotsOnlyHealsAllies() {
        Player p1 = new Player("P1");
        Player p2 = new Player("P2");
        RepairNanobots skill = new RepairNanobots();
        Robot actor = TestFixtures.robot("A", p1, 10, 3, 1, 3, skill);
        Robot ally = TestFixtures.robot("Al", p1, 10, 3, 1, 3, null);
        Robot enemy = TestFixtures.robot("En", p2, 10, 3, 1, 3, null);
        actor.setPosition(TestFixtures.hex(0, 0, TerrainType.NORMAL, 0));
        ally.takeDamage(6);

        skill.execute(actor, enemy);
        assertFalse(actor.isUsedAttack());

        skill.execute(actor, ally);
        assertTrue(actor.isUsedAttack());
        assertEquals(9, ally.getCurrentHp());
    }

    @Test
    void movementSkillsMoveAndRespectOccupiedTarget() {
        Player p = new Player("P");
        Robot actor = TestFixtures.robot("A", p, 10, 3, 1, 3, null);
        Hexagon from = TestFixtures.hex(0, 0, TerrainType.NORMAL, 0);
        Hexagon to = TestFixtures.hex(1, 0, TerrainType.NORMAL, 0);
        actor.setPosition(from);
        from.setOccupant(actor);

        JetpackBoost jetpack = new JetpackBoost();
        jetpack.execute(actor, to);
        assertSame(actor, to.getOccupant());
        assertEquals(1, jetpack.getCurrentCooldown());
        assertTrue(jetpack.isIgnoresHeightLimits());

        Hexagon blocked = TestFixtures.hex(2, 0, TerrainType.NORMAL, 0);
        blocked.setOccupant(TestFixtures.robot("B", p, 10, 3, 1, 2, null));
        TurboPropulsion turbo = new TurboPropulsion();
        turbo.execute(actor, blocked);
        assertNotSame(blocked, actor.getPosition());

        Hexagon free = TestFixtures.hex(2, 1, TerrainType.NORMAL, 0);
        turbo.execute(actor, free);
        assertSame(actor, free.getOccupant());
        assertEquals(1, turbo.getCurrentCooldown());
    }
}
