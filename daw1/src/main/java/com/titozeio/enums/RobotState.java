package com.titozeio.enums;

/**
 * Estados que puede tener un robot durante el combate.
 *
 * FROZEN - El robot no puede actuar durante el o los turnos indicados (Rayo
 * congelador).
 * EXHAUSTED - El robot ha agotado sus dos acciones del turno (movimiento +
 * ataque).
 */
public enum RobotState {
    FROZEN,
    EXHAUSTED
}
