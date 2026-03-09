package com.titozeio.model;

import com.titozeio.enums.AttackType;

/**
 * Representa el arma de un robot.
 * Según el GDD, cada robot tiene exactamente 1 arma con alcance y daño
 * definidos.
 */
public class Weapon {

    private String name;
    private int range;
    private int damage;
    private String description;
    private AttackType attackType;

    /**
     * Crea una nueva instancia de la clase.
     * @param name parametro de entrada.
     * @param range parametro de entrada.
     * @param damage parametro de entrada.
     * @param description parametro de entrada.
     * @param attackType parametro de entrada.
     */
    public Weapon(String name, int range, int damage, String description, AttackType attackType) {
        this.name = name;
        this.range = range;
        this.damage = damage;
        this.description = description;
        this.attackType = attackType;
    }

    // Getters
    /**
     * Obtiene name.
     * @return valor resultante de la operacion.
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene range.
     * @return valor resultante de la operacion.
     */
    public int getRange() {
        return range;
    }

    /**
     * Obtiene damage.
     * @return valor resultante de la operacion.
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Obtiene description.
     * @return valor resultante de la operacion.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtiene attack type.
     * @return valor resultante de la operacion.
     */
    public AttackType getAttackType() {
        return attackType;
    }

    @Override
    /**
     * Devuelve una representacion textual del objeto.
     * @return valor resultante de la operacion.
     */
    public String toString() {
        return name + " [Alcance:" + range + ", Daño:" + damage + ", Tipo:" + attackType + "]";
    }
}
