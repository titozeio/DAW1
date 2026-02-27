package com.titozeio.engine;

import com.titozeio.enums.RobotState;
import com.titozeio.model.Weapon;
import com.titozeio.skills.Skill;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad principal del juego. Representa un robot en el campo de batalla.
 * Según el GDD, cada robot tiene exactamente 1 arma y 1 skill,
 * puede moverse y atacar una vez por turno, y puede estar afectado
 * por estados como FROZEN o EXHAUSTED.
 */
public class Robot {

    private String modelName;
    private String description;
    private int maxHp;
    private int currentHp;
    private int movementPoints;
    private Weapon weapon;
    private Skill skill;
    private Player owner;
    private Hexagon position;

    // Control de acciones por turno
    private boolean usedMovement;
    private boolean usedAttack;

    // Estados activos: mapeados a los turnos que les quedan
    private HashMap<RobotState, Integer> activeStates;

    public Robot(String modelName, String description, int maxHp, int movementPoints,
            Weapon weapon, Skill skill, Player owner) {
        this.modelName = modelName;
        this.description = description;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.movementPoints = movementPoints;
        this.weapon = weapon;
        this.skill = skill;
        this.owner = owner;
        this.usedMovement = false;
        this.usedAttack = false;
        this.activeStates = new HashMap<>();
    }

    /**
     * Mueve el robot a un hexágono destino.
     * No valida el alcance ni el terreno (eso lo hace el sistema de movimiento).
     *
     * @param target hexágono destino.
     */
    public void move(Hexagon target) {
        if (usedMovement) {
            System.out.println(modelName + " ya ha usado su movimiento este turno.");
            return;
        }
        // Daño de caída si baja 2+ niveles
        if (position != null && target.getHeight() <= position.getHeight() - 2) {
            takeDamage(1);
            System.out.println(modelName + " recibe 1 punto de daño por caída.");
        }
        // Desocupar casilla actual
        if (position != null)
            position.setOccupant(null);
        // Ocupar nueva casilla
        target.setOccupant(this);
        this.position = target;
        usedMovement = true;
        System.out.println(modelName + " se mueve a " + target);
    }

    /**
     * Realiza un ataque contra otro robot.
     * Aplica modificadores de terreno (agua superficial, vegetación) y cobertura.
     *
     * @param target el robot objetivo.
     * @param map    el mapa actual (para calcular visibilidad y cobertura).
     */
    public void attack(Robot target, Map map) {
        if (usedAttack) {
            System.out.println(modelName + " ya ha atacado este turno.");
            return;
        }
        if (!canAttack(target, map)) {
            System.out.println(modelName + " no puede atacar a " + target.getModelName() + ".");
            return;
        }

        int damage = weapon.getDamage();

        // Modificador Overheat (atacante en agua poco profunda)
        if (position != null) {
            damage = Math.round(damage * position.getAttackDamageModifier());
        }

        // Modificador de cobertura
        float coverMod = map.getCoverModifier(position, target.getPosition());
        if (coverMod < 0) {
            System.out.println(target.getModelName() + " tiene cobertura total. No se puede atacar.");
            return;
        }
        damage = Math.round(damage * (1f - coverMod));

        // Aplicar daño (con modificador del terreno del defensor)
        target.takeDamage(damage);
        usedAttack = true;
        System.out.println(modelName + " ha impactado en " + target.getModelName()
                + " causando " + damage + " puntos de daño!");
    }

    /**
     * Usa la skill del robot.
     * 
     * @param target puede ser un Hexagon (skill de movimiento) o un Robot (skill de
     *               ataque).
     */
    public void useSkill(Object target) {
        if (skill == null) {
            System.out.println(modelName + " no tiene ninguna skill.");
            return;
        }
        if (!skill.isReady()) {
            System.out.println("La skill " + skill.getName() + " está en cooldown ("
                    + skill.getCurrentCooldown() + " turnos restantes).");
            return;
        }
        skill.execute(this, target);
    }

    /**
     * Aplica daño al robot, teniendo en cuenta el modificador de terreno.
     * El daño mínimo tras modificadores es 0.
     */
    public void takeDamage(int rawDamage) {
        float modifier = (position != null) ? position.getDamageModifier() : 1.0f;
        int finalDamage = Math.max(0, Math.round(rawDamage * modifier));
        currentHp -= finalDamage;
        if (currentHp < 0)
            currentHp = 0;
    }

    /**
     * Recupera HP (usado por la skill Nanobots reparadores).
     * Los HP nunca pueden superar el máximo original.
     */
    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
        System.out.println(modelName + " recupera " + amount + " HP. HP actual: " + currentHp + "/" + maxHp);
    }

    /** @return true si el robot ha sido destruido (HP ≤ 0). */
    public boolean isDestroyed() {
        return currentHp <= 0;
    }

    /** @return true si el robot ha usado tanto movimiento como ataque. */
    public boolean isExhausted() {
        return usedMovement && usedAttack;
    }

    /**
     * Verifica si este robot puede atacar al objetivo.
     * Condiciones: alcance, visibilidad (no cobertura total).
     */
    public boolean canAttack(Robot target, Map map) {
        if (position == null || target.getPosition() == null)
            return false;
        int dist = map.getDistance(position, target.getPosition());
        if (dist > weapon.getRange())
            return false;
        if (!map.hasVisibility(position, target.getPosition()))
            return false;
        float coverMod = map.getCoverModifier(position, target.getPosition());
        return coverMod >= 0; // -1 significa cobertura total
    }

    /**
     * Aplica un estado al robot durante un número de turnos.
     */
    public void applyState(RobotState state, int duration) {
        activeStates.put(state, duration);
        System.out.println(modelName + " tiene el estado " + state + " durante " + duration + " turnos.");
    }

    /** Elimina un estado activo del robot. */
    public void removeState(RobotState state) {
        activeStates.remove(state);
    }

    /** @return true si el robot tiene actualmente el estado indicado. */
    public boolean hasState(RobotState state) {
        return activeStates.containsKey(state) && activeStates.get(state) > 0;
    }

    /**
     * Reduce 1 turno los contadores de estados activos y elimina los que expiren.
     * Llamar al inicio de cada turno del robot.
     */
    public void tickStatusEffects() {
        Set<RobotState> toRemove = new HashSet<>();
        for (java.util.Map.Entry<RobotState, Integer> entry : activeStates.entrySet()) {
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                toRemove.add(entry.getKey());
            } else {
                activeStates.put(entry.getKey(), remaining);
            }
        }
        toRemove.forEach(activeStates::remove);
    }

    /** Resetea las acciones del robot al inicio de cada turno. */
    public void resetActions() {
        usedMovement = false;
        usedAttack = false;
        tickStatusEffects();
        if (skill != null)
            skill.tickCooldown();
    }

    // Getters y Setters
    public String getModelName() {
        return modelName;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMovementPoints() {
        return movementPoints;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public Skill getSkill() {
        return skill;
    }

    public Player getOwner() {
        return owner;
    }

    public Hexagon getPosition() {
        return position;
    }

    public void setPosition(Hexagon position) {
        this.position = position;
    }

    public boolean isUsedMovement() {
        return usedMovement;
    }

    public void setUsedMovement(boolean usedMovement) {
        this.usedMovement = usedMovement;
    }

    public boolean isUsedAttack() {
        return usedAttack;
    }

    public void setUsedAttack(boolean usedAttack) {
        this.usedAttack = usedAttack;
    }

    @Override
    public String toString() {
        return modelName + " [HP:" + currentHp + "/" + maxHp + "]";
    }
}
