package com.titozeio.states;

import com.titozeio.engine.Robot;

/**
 * Define el comportamiento de un robot según su estado actual.
 * Patrón de diseño: State.
 */
public interface RobotStateBehavior {
    /** @return true si el robot puede moverse en este estado. */
    boolean canMove(Robot robot);

    /** @return true si el robot puede atacar en este estado. */
    boolean canAttack(Robot robot);

    /** @return true si el robot puede usar habilidades en este estado. */
    boolean canUseSkill(Robot robot);

    /** Lógica a ejecutar al inicio del turno del robot. */
    void onTurnBegin(Robot robot);
}
