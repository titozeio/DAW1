package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Map;
import com.titozeio.enums.TerrainType;
import java.util.List;
import org.junit.jupiter.api.Test;

class MapTest {

    @Test
    void getAndSetHexagonHandleBounds() {
        Map map = new Map(2, 2);
        Hexagon h = TestFixtures.hex(1, 1, TerrainType.NORMAL, 0);
        map.setHexagon(1, 1, h);
        assertSame(h, map.getHexagon(1, 1));
        assertNull(map.getHexagon(-1, 0));
        assertNull(map.getHexagon(0, 3));
    }

    @Test
    void distanceVisibilityAndCoverageAreComputed() {
        Map map = new Map(1, 3);
        Hexagon a = TestFixtures.hex(0, 0, TerrainType.NORMAL, 0);
        Hexagon mid = TestFixtures.hex(1, 0, TerrainType.NORMAL, 1);
        Hexagon b = TestFixtures.hex(2, 0, TerrainType.NORMAL, 0);
        map.setHexagon(0, 0, a);
        map.setHexagon(1, 0, mid);
        map.setHexagon(2, 0, b);

        assertEquals(2, map.getDistance(a, b));
        assertTrue(map.hasVisibility(a, b));
        assertEquals(0.5f, map.getCoverModifier(a, b));

        mid.setHeight(2);
        assertFalse(map.hasVisibility(a, b));
        assertEquals(-1f, map.getCoverModifier(a, b));
    }

    @Test
    void calculateVisibilityAndObjectivesWork() {
        Map map = new Map(1, 3);
        Hexagon a = TestFixtures.hex(0, 0, TerrainType.NORMAL, 0);
        Hexagon m = TestFixtures.hex(1, 0, TerrainType.NORMAL, 0);
        Hexagon b = TestFixtures.hex(2, 0, TerrainType.NORMAL, 0);
        map.setHexagon(0, 0, a);
        map.setHexagon(1, 0, m);
        map.setHexagon(2, 0, b);

        List<Hexagon> visible = map.calculateVisibility(a, 2);
        assertEquals(2, visible.size());
        assertTrue(visible.contains(m));
        assertTrue(visible.contains(b));

        com.titozeio.model.Objective obj = new com.titozeio.model.Objective("obj", m);
        map.addObjective(obj);
        assertEquals(1, map.getObjectives().size());
    }
}
