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
        assertEquals(TerrainType.WATER_SHALLOW, map.getHexagon(6, 3).getTerrain());
        assertEquals(-1, map.getHexagon(6, 3).getHeight());
        assertEquals(TerrainType.WATER_SHALLOW, map.getHexagon(6, 6).getTerrain());
        assertEquals(-1, map.getHexagon(6, 6).getHeight());
        assertTrue(map.getHexagon(0, 4).isBaseP1());
        assertTrue(map.getHexagon(10, 4).isBaseP2());
        assertTrue(map.getHexagon(0, 1).isDeployZoneP1());
        assertTrue(map.getHexagon(10, 7).isDeployZoneP2());
    }

    @Test
    void map1RespectsCurrentTerrainHeightConstraints() {
        var map = MapFactory.createMap1();

        for (var row : map.getGrid()) {
            for (var hex : row) {
                assertNotNull(hex);
                switch (hex.getTerrain()) {
                    case VEGETATION -> assertEquals(0, hex.getHeight(),
                            "La vegetación del mapa actual debe estar siempre a altura 0");
                    case WATER_SHALLOW -> assertEquals(-1, hex.getHeight(),
                            "El agua poco profunda del mapa actual debe estar siempre a altura -1");
                    case WATER_DEEP -> assertEquals(-2, hex.getHeight(),
                            "El agua profunda del mapa actual debe estar siempre a altura -2");
                    case NORMAL -> assertTrue(hex.getHeight() == 0 || hex.getHeight() == 1 || hex.getHeight() == 2,
                            "El terreno normal del mapa actual solo debe usar alturas 0, 1 o 2");
                    default -> fail("Terreno no contemplado en las restricciones actuales: " + hex.getTerrain());
                }
            }
        }
    }
}
