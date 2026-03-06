package com.titozeio.engine;

/**
 * Fase actual de la partida.
 */
public enum GamePhase {
    /** Los jugadores están posicionando sus robots en las zonas de despliegue. */
    DEPLOYING,
    /** Combate activo por turnos. */
    COMBAT
}
