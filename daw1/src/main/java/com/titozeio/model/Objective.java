package com.titozeio.model;

import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Player;

/**
 * Representa un objetivo en el mapa que puede ser capturado por un jugador.
 * Usado por la condición de victoria BaseCaptureVC.
 */
public class Objective {

    private String name;
    private Hexagon location;
    private Player controllingPlayer;
    private int controlTurns;

    public Objective(String name, Hexagon location) {
        this.name = name;
        this.location = location;
        this.controllingPlayer = null;
        this.controlTurns = 0;
    }

    /**
     * Indica si el objetivo está siendo controlado por el jugador indicado.
     */
    public boolean isControlledBy(Player player) {
        return player != null && player.equals(controllingPlayer);
    }

    /**
     * Actualiza el control del objetivo. Si el jugador controlador cambia, reinicia
     * el contador.
     */
    public void updateControl(Player player) {
        if (player == null) {
            // Nadie en el objetivo
            controllingPlayer = null;
            controlTurns = 0;
        } else if (!player.equals(controllingPlayer)) {
            // Nuevo jugador tomó el control
            controllingPlayer = player;
            controlTurns = 1;
        } else {
            // Mismo jugador mantiene el control
            controlTurns++;
        }
    }

    // Getters y Setters
    public String getName() {
        return name;
    }

    public Hexagon getLocation() {
        return location;
    }

    public Player getControllingPlayer() {
        return controllingPlayer;
    }

    public int getControlTurns() {
        return controlTurns;
    }

    @Override
    public String toString() {
        return "Objetivo[" + name + "] controlado por: "
                + (controllingPlayer != null ? controllingPlayer.getName() : "nadie")
                + " (" + controlTurns + " turnos)";
    }
}
