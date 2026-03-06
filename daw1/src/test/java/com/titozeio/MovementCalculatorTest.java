package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.engine.Map;
import com.titozeio.engine.MovementCalculator;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.TerrainType;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MovementCalculatorTest {

    @Test
    void reachableRespectsTerrainHeightAndOccupancy() {
        Player p = new Player("P");
        Robot actor = TestFixtures.robot("A", p, 10, 3, 1, 3, null);

        Map map = new Map(3, 3);
        for (int r = 0; r < 3; r++) {
            for (int q = 0; q < 3; q++) {
                map.setHexagon(q, r, TestFixtures.hex(q, r, TerrainType.NORMAL, 0));
            }
        }

        var origin = map.getHexagon(1, 1);
        actor.setPosition(origin);

        map.getHexagon(1, 0).setTerrain(TerrainType.WATER_DEEP);
        map.getHexagon(2, 1).setHeight(3);
        map.getHexagon(0, 1).setOccupant(TestFixtures.robot("B", p, 10, 2, 1, 2, null));

        Set<com.titozeio.engine.Hexagon> reachable = MovementCalculator.getReachable(actor, map);

        assertFalse(reachable.contains(origin));
        assertFalse(reachable.contains(map.getHexagon(1, 0)));
        assertFalse(reachable.contains(map.getHexagon(2, 1)));
        assertFalse(reachable.contains(map.getHexagon(0, 1)));
        assertTrue(reachable.contains(map.getHexagon(1, 2)));
    }

    @Test
    void nullOriginReturnsEmptySet() {
        Player p = new Player("P");
        Robot actor = TestFixtures.robot("A", p, 10, 3, 1, 3, null);
        Set<com.titozeio.engine.Hexagon> reachable = MovementCalculator.getReachable(actor, new Map(1, 1));
        assertTrue(reachable.isEmpty());
    }
}
