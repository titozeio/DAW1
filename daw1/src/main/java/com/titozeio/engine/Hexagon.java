package com.titozeio.engine;

import com.titozeio.enums.CoverType;
import com.titozeio.enums.TerrainType;

/**
 * Representa una casilla hexagonal del mapa.
 * Cada hexágono tiene coordenadas axiales (q, r) y una coordenada cúbica
 * derivada (s = -q - r), además de tipo de terreno, altura y ocupante.
 */
public class Hexagon {

    // Coordenadas axiales
    private final int q;
    private final int r;

    private TerrainType terrain;
    private int height;
    private Robot occupant;

    // Flags de zona especial
    private boolean isDeployZoneP1;
    private boolean isDeployZoneP2;
    private boolean isBaseP1;
    private boolean isBaseP2;

    public Hexagon(int q, int r, TerrainType terrain, int height) {
        this.q = q;
        this.r = r;
        this.terrain = terrain;
        this.height = height;
    }

    /**
     * Calcula el coste de movimiento para un robot que entra en esta casilla.
     * Se tienen en cuenta:
     * - Tipo de terreno (Vegetación +1, Agua nivel 1 +1, Agua nivel 2 +2)
     * - Diferencia de altura con la casilla de origen (pasada como parámetro)
     *
     * @param robot      el robot que se mueve (para posibles modificadores
     *                   futuros).
     * @param fromHeight altura de la casilla de origen.
     * @return coste total de movimiento.
     */
    public int getMovementCost(Robot robot, int fromHeight) {
        int cost = 1; // Coste base

        // Modificadores por tipo de terreno
        switch (terrain) {
            case VEGETATION:
                cost += 1;
                break;
            case WATER_SHALLOW:
                cost += 1;
                break;
            case WATER_DEEP:
                cost += 2;
                break;
            default:
                break;
        }

        // Modificadores por diferencia de altura
        int heightDiff = this.height - fromHeight;
        if (heightDiff == 1) {
            cost += 1; // Subir 1 nivel: +1
        } else if (heightDiff <= -2) {
            cost += 1; // Saltar hacia abajo 2+ niveles: +1 (+ daño de caída en Robot)
        } else if (heightDiff == -1) {
            cost += 1; // Bajar 1 nivel: +1
        }
        // heightDiff >= 2: bloqueado, no se llega a calcular coste (el movimiento no es
        // válido)

        return cost;
    }

    /**
     * Devuelve el modificador de daño RECIBIDO por un robot posicionado en esta
     * casilla.
     * - Vegetación: 0.75 (25% reducción)
     * - Agua nivel 1: 1.25 (25% incremento recibido + 25% infligido → implementado
     * en Robot.attack)
     * - Agua nivel 2: sin ataque posible (gestionado en Map)
     * - Normal: 1.0
     */
    public float getDamageModifier() {
        switch (terrain) {
            case VEGETATION:
                return 0.75f;
            case WATER_SHALLOW:
                return 1.25f;
            default:
                return 1.0f;
        }
    }

    /**
     * Devuelve el modificador de daño INFLIGIDO por un robot en agua poco profunda
     * (regla Overheat del GDD).
     */
    public float getAttackDamageModifier() {
        if (terrain == TerrainType.WATER_SHALLOW)
            return 1.25f;
        return 1.0f;
    }

    /**
     * Indica si la casilla puede ser atacada/entrar en ella (agua profunda → no).
     */
    public boolean isAccessibleForCombat() {
        return terrain != TerrainType.WATER_DEEP;
    }

    /** Indica si hay un robot ocupando esta casilla. */
    public boolean isOccupied() {
        return occupant != null;
    }

    /**
     * Indica si la diferencia de altura impide que un robot (en la casilla de
     * origen)
     * pueda entrar en esta casilla (diferencia >= 2 niveles hacia arriba).
     */
    public boolean isReachableFrom(int fromHeight) {
        return (this.height - fromHeight) < 2;
    }

    /**
     * Devuelve la cobertura que ofrece esta casilla respecto a una altura de
     * referencia.
     * Usado por Map.getCoverModifier al evaluar casillas intermedias.
     */
    public CoverType getCoverTypeAgainst(int referenceHeight) {
        int diff = this.height - referenceHeight;
        if (diff >= 2)
            return CoverType.FULL;
        if (diff == 1)
            return CoverType.HALF;
        return CoverType.NONE;
    }

    // Coordenada cúbica derivada
    public int getS() {
        return -q - r;
    }

    // Getters y Setters
    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    public TerrainType getTerrain() {
        return terrain;
    }

    public void setTerrain(TerrainType terrain) {
        this.terrain = terrain;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Robot getOccupant() {
        return occupant;
    }

    public void setOccupant(Robot occupant) {
        this.occupant = occupant;
    }

    public boolean isDeployZoneP1() {
        return isDeployZoneP1;
    }

    public void setDeployZoneP1(boolean deployZoneP1) {
        isDeployZoneP1 = deployZoneP1;
    }

    public boolean isDeployZoneP2() {
        return isDeployZoneP2;
    }

    public void setDeployZoneP2(boolean deployZoneP2) {
        isDeployZoneP2 = deployZoneP2;
    }

    public boolean isBaseP1() {
        return isBaseP1;
    }

    public void setBaseP1(boolean baseP1) {
        isBaseP1 = baseP1;
    }

    public boolean isBaseP2() {
        return isBaseP2;
    }

    public void setBaseP2(boolean baseP2) {
        isBaseP2 = baseP2;
    }

    @Override
    public String toString() {
        return "Hexagon(" + q + "," + r + ")[" + terrain + ", h=" + height + "]";
    }
}
