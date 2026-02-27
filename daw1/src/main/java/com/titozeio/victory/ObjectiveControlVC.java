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

    public ObjectiveControlVC(Objective targetObjective, int turnsToControl) {
        this.targetObjective = targetObjective;
        this.turnsToControl = turnsToControl;
    }

    @Override
    public Player check(Game game) {
        Player controller = targetObjective.getControllingPlayer();
        if (controller != null && targetObjective.getControlTurns() >= turnsToControl) {
            return controller;
        }
        return null;
    }

    public Objective getTargetObjective() {
        return targetObjective;
    }

    public int getTurnsToControl() {
        return turnsToControl;
    }
}
