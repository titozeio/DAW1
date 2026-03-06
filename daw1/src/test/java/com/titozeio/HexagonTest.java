package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Map;
import com.titozeio.enums.CoverType;
import com.titozeio.enums.TerrainType;
import org.junit.jupiter.api.Test;

class HexagonTest {

    @Test
    void movementCostAndModifiersAreAppliedByTerrainAndHeight() {
        Hexagon normal = TestFixtures.hex(0, 0, TerrainType.NORMAL, 0);
        Hexagon vegetation = TestFixtures.hex(1, 0, TerrainType.VEGETATION, 1);
        Hexagon shallow = TestFixtures.hex(2, 0, TerrainType.WATER_SHALLOW, -1);
        Hexagon deep = TestFixtures.hex(3, 0, TerrainType.WATER_DEEP, -2);

        assertEquals(1, normal.getMovementCost(null, 0));
        assertEquals(3, vegetation.getMovementCost(null, 0));
        assertEquals(3, shallow.getMovementCost(null, 0));
        assertEquals(4, deep.getMovementCost(null, 0));

        assertEquals(0.75f, vegetation.getDamageModifier());
        assertEquals(1.25f, shallow.getDamageModifier());
        assertEquals(1.25f, shallow.getAttackDamageModifier());
        assertEquals(1.0f, normal.getAttackDamageModifier());
    }

    @Test
    void accessibilityReachabilityAndCoverTypeWork() {
        Hexagon h = TestFixtures.hex(0, 0, TerrainType.NORMAL, 2);
        assertTrue(h.isAccessibleForCombat());
        h.setTerrain(TerrainType.WATER_DEEP);
        assertFalse(h.isAccessibleForCombat());

        h.setTerrain(TerrainType.NORMAL);
        assertFalse(h.isReachableFrom(0));
        assertTrue(h.isReachableFrom(1));

        assertEquals(CoverType.FULL, h.getCoverTypeAgainst(0));
        assertEquals(CoverType.HALF, h.getCoverTypeAgainst(1));
        assertEquals(CoverType.NONE, h.getCoverTypeAgainst(2));
    }

    @Test
    void occupancyAndToStringExposeState() {
        Hexagon h = TestFixtures.hex(1, 2, TerrainType.NORMAL, 0);
        assertFalse(h.isOccupied());
        h.setOccupant(TestFixtures.robot("r", new com.titozeio.engine.Player("p"), 10, 3, 1, 2, null));
        assertTrue(h.isOccupied());
        assertTrue(h.toString().contains("Hexagon(1,2)"));
    }
}
