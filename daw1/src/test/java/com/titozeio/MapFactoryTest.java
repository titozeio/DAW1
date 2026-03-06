package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.engine.MapFactory;
import com.titozeio.enums.TerrainType;
import org.junit.jupiter.api.Test;

class MapFactoryTest {

    @Test
    void map1HasExpectedSpecialZonesAndTerrain() {
        var map = MapFactory.createMap1();
        assertEquals(TerrainType.WATER_DEEP, map.getHexagon(5, 4).getTerrain());
        assertTrue(map.getHexagon(0, 4).isBaseP1());
        assertTrue(map.getHexagon(10, 4).isBaseP2());
        assertTrue(map.getHexagon(0, 1).isDeployZoneP1());
        assertTrue(map.getHexagon(10, 7).isDeployZoneP2());
    }
}
