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

    /**
     * Crea una nueva instancia de la clase.
     * @param name parametro de entrada.
     * @param location parametro de entrada.
     */
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
    /**
     * Obtiene name.
     * @return valor resultante de la operacion.
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene location.
     * @return valor resultante de la operacion.
     */
    public Hexagon getLocation() {
        return location;
    }

    /**
     * Obtiene controlling player.
     * @return valor resultante de la operacion.
     */
    public Player getControllingPlayer() {
        return controllingPlayer;
    }

    /**
     * Obtiene control turns.
     * @return valor resultante de la operacion.
     */
    public int getControlTurns() {
        return controlTurns;
    }

    @Override
    /**
     * Devuelve una representacion textual del objeto.
     * @return valor resultante de la operacion.
     */
    public String toString() {
        return "Objetivo[" + name + "] controlado por: "
                + (controllingPlayer != null ? controllingPlayer.getName() : "nadie")
                + " (" + controlTurns + " turnos)";
    }
}
