package com.titozeio.engine;

import com.titozeio.enums.TerrainType;

/**
 * Factoría de mapas del juego.
 *
 * Cada método estático crea y devuelve un Map completamente inicializado,
 * listo para pasarse al Game. Los robots no están posicionados aquí;
 * eso ocurre durante la fase de despliegue.
 *
 * Añadir aquí nuevos mapas en el futuro (createMap2, createMap3, etc.)
 * o migrar a carga desde JSON cuando haga falta.
 */
public class MapFactory {

    // ── Constantes de diseño de Map1 ──────────────────────────────────────────
    private static final int MAP1_ROWS = 9; // r: 0..8
    private static final int MAP1_COLS = 11; // q: 0..10

    /**
     * Crea el primer mapa: "Yermo Industrial".
     *
     * Dimensiones: 11 columnas × 9 filas (coordenadas axiales q, r)
     *
     * Zonas especiales:
     * P1 despliega en q=0, r=1-7 | P2 despliega en q=10, r=1-7
     * Base P1: (q=0, r=4) | Base P2: (q=10, r=4)
     *
     * Características del terreno:
     * - Canal central de agua (q=5, r=1-7): agua poco profunda, centro profunda
     * - Mesetas industriales (q=2-3 / q=7-8): height 1 y 2
     * - Vegetación en flancos exteriores (q=1-2 / q=8-9, filas 0 y 8)
     * - Pasillos de terreno normal a ambos lados del canal
     */
    public static Map createMap1() {
        Map map = new Map(MAP1_ROWS, MAP1_COLS);

        // 1. Inicializar todo el mapa con terreno NORMAL, altura 0
        for (int r = 0; r < MAP1_ROWS; r++) {
            for (int q = 0; q < MAP1_COLS; q++) {
                map.setHexagon(q, r, new Hexagon(q, r, TerrainType.NORMAL, 0));
            }
        }

        // 2. Canal de agua central (columna q=5)
        for (int r = 0; r <= 8; r++) {
            // Centro: agua profunda en r=4
            TerrainType water = (r == 4 || r == 5) ? TerrainType.WATER_DEEP : TerrainType.WATER_SHALLOW;
            int height = (r == 4 || r == 5) ? -2 : -1;
            map.setHexagon(5, r, new Hexagon(5, r, water, height));
        }

        // Agua en q=4, r=4-6, q=6, r=3-6
        map.setHexagon(4, 4, new Hexagon(4, 4, TerrainType.WATER_SHALLOW, 1));
        map.setHexagon(4, 5, new Hexagon(4, 5, TerrainType.WATER_SHALLOW, 1));
        map.setHexagon(4, 6, new Hexagon(4, 6, TerrainType.WATER_SHALLOW, 1));
        map.setHexagon(6, 3, new Hexagon(6, 3, TerrainType.WATER_SHALLOW, 1));
        map.setHexagon(6, 4, new Hexagon(6, 4, TerrainType.WATER_SHALLOW, 1));
        map.setHexagon(6, 5, new Hexagon(6, 5, TerrainType.WATER_SHALLOW, 1));
        map.setHexagon(6, 6, new Hexagon(6, 6, TerrainType.WATER_SHALLOW, 1));

        // 3. Mesetas q=2 y q=3
        // Altura 1 en q=2 (filas 1-7 excepto 4)
        map.setHexagon(2, 1, new Hexagon(2, 1, TerrainType.NORMAL, 1));
        map.setHexagon(2, 2, new Hexagon(2, 2, TerrainType.NORMAL, 1));
        map.setHexagon(2, 5, new Hexagon(2, 5, TerrainType.NORMAL, 1));
        map.setHexagon(2, 6, new Hexagon(2, 6, TerrainType.NORMAL, 2));
        map.setHexagon(2, 7, new Hexagon(2, 7, TerrainType.NORMAL, 2));

        // Altura 2 en q=3 (filas 1-7 excepto 4 y 5)
        map.setHexagon(3, 1, new Hexagon(3, 1, TerrainType.NORMAL, 1));
        map.setHexagon(3, 2, new Hexagon(3, 2, TerrainType.NORMAL, 2));
        map.setHexagon(3, 5, new Hexagon(3, 5, TerrainType.NORMAL, 1));
        map.setHexagon(3, 6, new Hexagon(3, 6, TerrainType.NORMAL, 2));
        map.setHexagon(3, 7, new Hexagon(3, 7, TerrainType.NORMAL, 2));

        // Mesetas q=7 y q=8
        // Altura 1 en q=8 (filas 1-7 excepto 4)
        // Altura 2 en q=7 (filas 1-7 excepto 4 y 5)
        map.setHexagon(8, 1, new Hexagon(8, 1, TerrainType.NORMAL, 2));
        map.setHexagon(8, 2, new Hexagon(8, 2, TerrainType.NORMAL, 2));
        map.setHexagon(8, 5, new Hexagon(8, 5, TerrainType.NORMAL, 1));
        map.setHexagon(8, 6, new Hexagon(8, 6, TerrainType.NORMAL, 2));

        map.setHexagon(6, 2, new Hexagon(6, 2, TerrainType.NORMAL, 1));
        map.setHexagon(7, 1, new Hexagon(7, 1, TerrainType.NORMAL, 1));
        map.setHexagon(7, 2, new Hexagon(7, 2, TerrainType.NORMAL, 2));
        map.setHexagon(7, 5, new Hexagon(7, 5, TerrainType.NORMAL, 2));
        map.setHexagon(7, 6, new Hexagon(7, 6, TerrainType.NORMAL, 2));
        map.setHexagon(7, 7, new Hexagon(7, 7, TerrainType.NORMAL, 1));

        map.setHexagon(6, 7, new Hexagon(6, 7, TerrainType.NORMAL, 1));
        map.setHexagon(6, 8, new Hexagon(6, 8, TerrainType.NORMAL, 1));

        // 5. Vegetación
        // P1 section: q=3, r=0-2
        // P2 section: q=7, r=5-8
        map.setHexagon(3, 0, new Hexagon(3, 0, TerrainType.VEGETATION, 0));
        map.setHexagon(4, 0, new Hexagon(4, 0, TerrainType.VEGETATION, 0));
        map.setHexagon(4, 1, new Hexagon(4, 1, TerrainType.VEGETATION, 0));
        map.setHexagon(4, 2, new Hexagon(4, 2, TerrainType.VEGETATION, 0));

        map.setHexagon(9, 2, new Hexagon(9, 2, TerrainType.VEGETATION, 0));
        map.setHexagon(9, 3, new Hexagon(9, 3, TerrainType.VEGETATION, 0));
        map.setHexagon(9, 4, new Hexagon(9, 4, TerrainType.VEGETATION, 0));
        map.setHexagon(8, 4, new Hexagon(8, 4, TerrainType.VEGETATION, 0));
        map.setHexagon(8, 3, new Hexagon(8, 3, TerrainType.VEGETATION, 0));

        map.setHexagon(8, 8, new Hexagon(8, 8, TerrainType.VEGETATION, 0));
        map.setHexagon(7, 8, new Hexagon(7, 8, TerrainType.VEGETATION, 0));
        map.setHexagon(4, 7, new Hexagon(4, 7, TerrainType.VEGETATION, 0));

        // 6. Zonas de despliegue P1 (q=0, r=1-7)
        for (int r = 1; r <= 7; r++) {
            Hexagon h = map.getHexagon(0, r);
            if (h != null)
                h.setDeployZoneP1(true);
        }

        // 7. Zonas de despliegue P2 (q=10, r=1-7)
        for (int r = 1; r <= 7; r++) {
            Hexagon h = map.getHexagon(10, r);
            if (h != null)
                h.setDeployZoneP2(true);
        }

        // 8. Bases
        Hexagon baseP1 = map.getHexagon(0, 4);
        if (baseP1 != null) {
            baseP1.setBaseP1(true);
            baseP1.setDeployZoneP1(true);
        }
        Hexagon baseP2 = map.getHexagon(10, 4);
        if (baseP2 != null) {
            baseP2.setBaseP2(true);
            baseP2.setDeployZoneP2(true);
        }

        System.out.println("MapFactory: Mapa 1 (Yermo Industrial) creado. " +
                MAP1_ROWS + "x" + MAP1_COLS + " hexágonos.");
        return map;
    }
}
