package com.titozeio.enums;

/**
 * Tipos de cobertura que puede ofrecer el terreno intermedio en un ataque.
 *
 * NONE - Sin cobertura. El ataque se resuelve con daño normal.
 * HALF - Cobertura media (casilla intermedia +1 nivel): daño reducido al 50%.
 * FULL - Cobertura total (casilla intermedia +2 niveles): el ataque no puede
 * realizarse.
 */
public enum CoverType {
    NONE,
    HALF,
    FULL
}
