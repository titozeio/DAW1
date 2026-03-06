package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

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

class SkillsEdgeCasesTest {

    @Test
    void attackSkillsIgnoreInvalidTargetsAndNullPositions() {
        Player p1 = new Player("P1");
        Player p2 = new Player("P2");
        Robot actor = TestFixtures.robot("A", p1, 10, 3, 3, 4, null);
        Robot target = TestFixtures.robot("T", p2, 10, 3, 3, 4, null);

        FreezeRay fr = new FreezeRay();
        GuidedMissile gm = new GuidedMissile();
        PlasmaSaberAttack ps = new PlasmaSaberAttack();

        fr.execute(actor, "bad");
        gm.execute(actor, "bad");
        ps.execute(actor, "bad");
        assertEquals(10, target.getCurrentHp());

        actor.setPosition(TestFixtures.hex(0, 0, TerrainType.NORMAL, 0));
        fr.execute(actor, target);
        gm.execute(actor, target);
        ps.execute(actor, target);
        assertEquals(10, target.getCurrentHp());
    }

    @Test
    void movementSkillsIgnoreInvalidOrOccupiedTargets() {
        Player p = new Player("P");
        Robot actor = TestFixtures.robot("A", p, 10, 3, 3, 4, null);

        JetpackBoost jet = new JetpackBoost();
        TurboPropulsion turbo = new TurboPropulsion();

        jet.execute(actor, "bad");
        turbo.execute(actor, "bad");
        assertNull(actor.getPosition());

        var dest = TestFixtures.hex(1, 0, TerrainType.NORMAL, 0);
        dest.setOccupant(TestFixtures.robot("B", p, 10, 2, 1, 1, null));
        jet.execute(actor, dest);
        turbo.execute(actor, dest);
        assertNull(actor.getPosition());
    }

    @Test
    void repairNanobotsRejectsDestroyedTargets() {
        Player p = new Player("P");
        RepairNanobots rn = new RepairNanobots();
        Robot actor = TestFixtures.robot("A", p, 10, 3, 3, 4, rn);
        Robot ally = TestFixtures.robot("Al", p, 10, 3, 3, 4, null);

        ally.takeDamage(100);
        assertTrue(ally.isDestroyed());

        rn.execute(actor, ally);
        assertFalse(actor.isUsedAttack());
    }
}
