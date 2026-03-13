package com.titozeio.enums;

/**
 * Tipos de terreno de las casillas del mapa, según el GDD.
 *
 * NORMAL - Sin efectos adicionales.
 * VEGETATION - Reducción de daño 25%, coste de movimiento +1.
 * WATER_SHALLOW - Nivel -1 (profundidad 1): daño infligido y recibido +25%,
 * coste +1.
 * WATER_DEEP - Nivel -2 (profundidad 2): no puede atacar ni ser atacado, coste
 * +2.
 */
public enum TerrainType {
    NORMAL,
    VEGETATION,
    WATER_SHALLOW,
    WATER_DEEP
}
