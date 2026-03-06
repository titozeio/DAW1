package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import org.junit.jupiter.api.Test;

class PlayerTest {

    @Test
    void eliminationActionsAliveUnitsAndBaseControlWork() {
        Player p = new Player("P");
        Robot r1 = TestFixtures.robot("R1", p, 10, 3, 1, 2, null);
        Robot r2 = TestFixtures.robot("R2", p, 10, 3, 1, 2, null);

        p.addUnit(r1);
        p.addUnit(r2);

        assertFalse(p.isEliminated());
        assertTrue(p.hasActionsLeft());

        r1.takeDamage(100);
        assertEquals(1, p.getAliveUnits().size());

        r2.setUsedAttack(true);
        r2.setUsedMovement(true);
        assertFalse(p.hasActionsLeft());

        r2.takeDamage(100);
        assertTrue(p.isEliminated());

        p.incrementBaseControlTurns();
        p.incrementBaseControlTurns();
        assertEquals(2, p.getBaseControlTurns());
        p.resetBaseControlTurns();
        assertEquals(0, p.getBaseControlTurns());
        assertTrue(p.toString().contains("Player"));
    }
}
