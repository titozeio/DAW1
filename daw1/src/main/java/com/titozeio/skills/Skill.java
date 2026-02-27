package com.titozeio.skills;

import com.titozeio.engine.Robot;
import com.titozeio.enums.SkillType;

/**
 * Clase base abstracta para todas las habilidades especiales de los robots.
 * Según el GDD, las skills pueden ser de tipo MOVIMIENTO o ATAQUE, y sustituyen
 * a la acción correspondiente del turno.
 *
 * Todos los hijos deben implementar execute().
 */
public abstract class Skill {

    protected String name;
    protected SkillType type;
    protected int cooldown;
    protected int currentCooldown;
    protected String description;

    protected Skill(String name, SkillType type, int cooldown, String description) {
        this.name = name;
        this.type = type;
        this.cooldown = cooldown;
        this.currentCooldown = 0; // Lista para usar al comenzar
        this.description = description;
    }

    /**
     * Ejecuta la skill.
     * 
     * @param actor  el robot que usa la skill.
     * @param target el objetivo (puede ser Hexagon para movimiento, Robot para
     *               ataque).
     */
    public abstract void execute(Robot actor, Object target);

    /**
     * @return true si la skill puede usarse (cooldown a 0).
     */
    public boolean isReady() {
        return currentCooldown == 0;
    }

    /**
     * Reduce el cooldown en 1 turno. Llamar al inicio de cada turno del robot.
     */
    public void tickCooldown() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }
    }

    /**
     * Activa el cooldown después de usar la skill.
     */
    public void resetCooldown() {
        this.currentCooldown = this.cooldown;
    }

    // Getters
    public String getName() {
        return name;
    }

    public SkillType getType() {
        return type;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getCurrentCooldown() {
        return currentCooldown;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name + " [" + type + ", Cooldown:" + cooldown + ", Restante:" + currentCooldown + "]";
    }
}
