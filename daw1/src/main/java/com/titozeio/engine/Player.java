package com.titozeio.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa a un jugador (P1 o P2).
 * Gestiona su equipo de robots y el control de su base.
 */
public class Player {

    private String name;
    private List<Robot> units;
    private Hexagon baseLocation;
    private int baseControlTurns; // Turnos que un robot enemigo lleva en la base de este jugador

    public Player(String name) {
        this.name = name;
        this.units = new ArrayList<>();
        this.baseControlTurns = 0;
    }

    /**
     * Indica si el jugador ha sido eliminado.
     * Un jugador es eliminado cuando todos sus robots han sido destruidos.
     */
    public boolean isEliminated() {
        return units.stream().allMatch(Robot::isDestroyed);
    }

    /**
     * Comprueba si al jugador le quedan acciones disponibles
     * (algún robot vivo que no haya agotado sus dos acciones).
     */
    public boolean hasActionsLeft() {
        return units.stream()
                .filter(r -> !r.isDestroyed())
                .anyMatch(r -> !r.isExhausted());
    }

    /** Añade un robot al equipo del jugador. */
    public void addUnit(Robot robot) {
        units.add(robot);
    }

    /** Devuelve los robots vivos del jugador. */
    public List<Robot> getAliveUnits() {
        List<Robot> alive = new ArrayList<>();
        for (Robot r : units) {
            if (!r.isDestroyed())
                alive.add(r);
        }
        return alive;
    }

    // Getters y Setters
    public String getName() {
        return name;
    }

    public List<Robot> getUnits() {
        return units;
    }

    public Hexagon getBaseLocation() {
        return baseLocation;
    }

    public void setBaseLocation(Hexagon baseLocation) {
        this.baseLocation = baseLocation;
    }

    public int getBaseControlTurns() {
        return baseControlTurns;
    }

    public void incrementBaseControlTurns() {
        this.baseControlTurns++;
    }

    public void resetBaseControlTurns() {
        this.baseControlTurns = 0;
    }

    @Override
    public String toString() {
        return "Player{" + name + ", robots=" + units.size() + "}";
    }
}
