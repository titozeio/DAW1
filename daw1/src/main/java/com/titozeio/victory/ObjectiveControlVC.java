package com.titozeio.victory;

import com.titozeio.engine.Game;
import com.titozeio.engine.Player;
import com.titozeio.model.Objective;

/**
 * Condición de victoria: Control de un objetivo del mapa durante N turnos.
 * Generalización de BaseCaptureVC para futuros mapas con objetivos.
 */
public class ObjectiveControlVC extends VictoryCondition {

    private final Objective targetObjective;
    private final int turnsToControl;

    /**
     * Crea una nueva instancia de la clase.
     * @param targetObjective parametro de entrada.
     * @param turnsToControl parametro de entrada.
     */
    public ObjectiveControlVC(Objective targetObjective, int turnsToControl) {
        this.targetObjective = targetObjective;
        this.turnsToControl = turnsToControl;
    }

    @Override
    /**
     * Ejecuta la logica de check.
     * @param game parametro de entrada.
     * @return valor resultante de la operacion.
     */
    public Player check(Game game) {
        Player controller = targetObjective.getControllingPlayer();
        if (controller != null && targetObjective.getControlTurns() >= turnsToControl) {
            return controller;
        }
        return null;
    }

    /**
     * Obtiene target objective.
     * @return valor resultante de la operacion.
     */
    public Objective getTargetObjective() {
        return targetObjective;
    }

    /**
     * Obtiene turns to control.
     * @return valor resultante de la operacion.
     */
    public int getTurnsToControl() {
        return turnsToControl;
    }
}
