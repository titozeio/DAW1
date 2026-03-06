package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.engine.Game;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.model.Objective;
import com.titozeio.victory.BaseCaptureVC;
import com.titozeio.victory.EliminationVC;
import com.titozeio.victory.ObjectiveControlVC;
import org.junit.jupiter.api.Test;

class VictoryConditionsTest {

    @Test
    void eliminationVictoryDetectsWinner() {
        Game game = new Game(null);
        Player p1 = game.getP1();
        Player p2 = game.getP2();

        Robot p1Unit = TestFixtures.robot("P1", p1, 10, 3, 1, 2, null);
        Robot p2Unit = TestFixtures.robot("P2", p2, 10, 3, 1, 2, null);
        p1.addUnit(p1Unit);
        p2.addUnit(p2Unit);

        assertNull(new EliminationVC().check(game));
        p2Unit.takeDamage(100);
        assertEquals(p1, new EliminationVC().check(game));
    }

    @Test
    void baseCaptureRequiresConsecutiveTurns() {
        Game game = new Game(null);
        Player p1 = game.getP1();
        Player p2 = game.getP2();

        var p1Base = game.getMap().getHexagon(0, 4);
        var p2Base = game.getMap().getHexagon(10, 4);
        p1.setBaseLocation(p1Base);
        p2.setBaseLocation(p2Base);

        Robot attacker = TestFixtures.robot("A", p1, 10, 3, 1, 2, null);
        p1.addUnit(attacker);
        p2Base.setOccupant(attacker);

        BaseCaptureVC vc = new BaseCaptureVC(2);
        assertNull(vc.check(game));
        assertEquals(p1, vc.check(game));

        p2Base.setOccupant(null);
        assertNull(vc.check(game));
        assertEquals(0, p2.getBaseControlTurns());
    }

    @Test
    void objectiveControlVictoryChecksControlTurns() {
        Objective obj = new Objective("O", null);
        Player p = new Player("P");
        ObjectiveControlVC vc = new ObjectiveControlVC(obj, 2);

        assertNull(vc.check(new Game(null)));
        obj.updateControl(p);
        assertNull(vc.check(new Game(null)));
        obj.updateControl(p);
        assertEquals(p, vc.check(new Game(null)));
    }
}
