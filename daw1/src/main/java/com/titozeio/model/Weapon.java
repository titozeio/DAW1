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

    public Weapon(String name, int range, int damage, String description, AttackType attackType) {
        this.name = name;
        this.range = range;
        this.damage = damage;
        this.description = description;
        this.attackType = attackType;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getRange() {
        return range;
    }

    public int getDamage() {
        return damage;
    }

    public String getDescription() {
        return description;
    }

    public AttackType getAttackType() {
        return attackType;
    }

    @Override
    public String toString() {
        return name + " [Alcance:" + range + ", Daño:" + damage + ", Tipo:" + attackType + "]";
    }
}
