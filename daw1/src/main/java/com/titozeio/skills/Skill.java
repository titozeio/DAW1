package com.titozeio.skills;

import com.titozeio.engine.Hexagon;
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
    /**
     * Obtiene name.
     * @return valor resultante de la operacion.
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene type.
     * @return valor resultante de la operacion.
     */
    public SkillType getType() {
        return type;
    }

    /**
     * Obtiene cooldown.
     * @return valor resultante de la operacion.
     */
    public int getCooldown() {
        return cooldown;
    }

    /**
     * Obtiene current cooldown.
     * @return valor resultante de la operacion.
     */
    public int getCurrentCooldown() {
        return currentCooldown;
    }

    /**
     * Ajusta el cooldown actual para simulaciones internas de la IA.
     * No debe usarse desde la UI ni desde el flujo normal del combate.
     *
     * @param currentCooldown nuevo valor del cooldown restante.
     */
    public void setCurrentCooldownForSimulation(int currentCooldown) {
        this.currentCooldown = Math.max(0, currentCooldown);
    }

    /**
     * Obtiene description.
     * @return valor resultante de la operacion.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Devuelve los parámetros específicos de esta skill para mostrar en la
     * tarjeta de selección de robots. Cada elemento es un array de dos Strings:
     * [etiqueta, valor]. Por ejemplo: ["Alcance: ", "3"] o ["Movimiento: ", "7"].
     *
     * Cada subclase debe implementar este método con sus stats reales.
     */
    public abstract java.util.List<String[]> getDisplayStats();

    /**
     * Calcula la distancia hexagonal entre dos casillas usando coordenadas axiales.
     */
    protected int hexDistance(Hexagon a, Hexagon b) {
        int dq = a.getQ() - b.getQ();
        int dr = a.getR() - b.getR();
        int ds = a.getS() - b.getS();
        return (Math.abs(dq) + Math.abs(dr) + Math.abs(ds)) / 2;
    }

    /**
     * Indica si el objetivo está dentro de alcance desde la posición del actor.
     */
    protected boolean isTargetInRange(Robot actor, Robot target, int range) {
        if (actor == null || target == null || actor.getPosition() == null || target.getPosition() == null) {
            return false;
        }
        return hexDistance(actor.getPosition(), target.getPosition()) <= range;
    }

    @Override
    /**
     * Devuelve una representacion textual del objeto.
     * @return valor resultante de la operacion.
     */
    public String toString() {
        return name + " [" + type + ", Cooldown:" + cooldown + ", Restante:" + currentCooldown + "]";
    }
}
