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
        for (int r = 1; r <= 7; r++) {
            // Centro: agua profunda en r=4
            TerrainType water = (r == 4) ? TerrainType.WATER_DEEP : TerrainType.WATER_SHALLOW;
            int height = (r == 4) ? -2 : -1;
            map.setHexagon(5, r, new Hexagon(5, r, water, height));
        }

        // 3. Mesetas — lado izquierdo (q=2 y q=3)
        // Altura 1 en q=2 (filas 1-7 excepto 4)
        for (int r = 1; r <= 7; r++) {
            if (r != 4) {
                map.setHexagon(2, r, new Hexagon(2, r, TerrainType.NORMAL, 1));
            }
        }
        // Altura 2 → cimas en q=3, r=2 y r=6
        map.setHexagon(3, 1, new Hexagon(3, 1, TerrainType.NORMAL, 1));
        map.setHexagon(3, 2, new Hexagon(3, 2, TerrainType.NORMAL, 2));
        map.setHexagon(3, 3, new Hexagon(3, 3, TerrainType.NORMAL, 1));
        map.setHexagon(3, 5, new Hexagon(3, 5, TerrainType.NORMAL, 1));
        map.setHexagon(3, 6, new Hexagon(3, 6, TerrainType.NORMAL, 2));
        map.setHexagon(3, 7, new Hexagon(3, 7, TerrainType.NORMAL, 1));

        // 4. Mesetas — lado derecho (q=8 y q=7), simétricas
        for (int r = 1; r <= 7; r++) {
            if (r != 4) {
                map.setHexagon(8, r, new Hexagon(8, r, TerrainType.NORMAL, 1));
            }
        }
        map.setHexagon(7, 1, new Hexagon(7, 1, TerrainType.NORMAL, 1));
        map.setHexagon(7, 2, new Hexagon(7, 2, TerrainType.NORMAL, 2));
        map.setHexagon(7, 3, new Hexagon(7, 3, TerrainType.NORMAL, 1));
        map.setHexagon(7, 5, new Hexagon(7, 5, TerrainType.NORMAL, 1));
        map.setHexagon(7, 6, new Hexagon(7, 6, TerrainType.NORMAL, 2));
        map.setHexagon(7, 7, new Hexagon(7, 7, TerrainType.NORMAL, 1));

        // 5. Vegetación en flancos exteriores
        // Lado izquierdo superior/inferior
        map.setHexagon(1, 0, new Hexagon(1, 0, TerrainType.VEGETATION, 0));
        map.setHexagon(2, 0, new Hexagon(2, 0, TerrainType.VEGETATION, 0));
        map.setHexagon(1, 8, new Hexagon(1, 8, TerrainType.VEGETATION, 0));
        map.setHexagon(2, 8, new Hexagon(2, 8, TerrainType.VEGETATION, 0));
        // Lado derecho superior/inferior
        map.setHexagon(8, 0, new Hexagon(8, 0, TerrainType.VEGETATION, 0));
        map.setHexagon(9, 0, new Hexagon(9, 0, TerrainType.VEGETATION, 0));
        map.setHexagon(8, 8, new Hexagon(8, 8, TerrainType.VEGETATION, 0));
        map.setHexagon(9, 8, new Hexagon(9, 8, TerrainType.VEGETATION, 0));

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
