package com.titozeio.engine;

import com.titozeio.enums.RobotState;
import com.titozeio.model.Weapon;
import com.titozeio.skills.Skill;
import com.titozeio.states.ExhaustedState;
import com.titozeio.states.FrozenState;
import com.titozeio.states.NormalState;
import com.titozeio.states.RobotStateBehavior;

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
    private Hexagon previousPosition; // Para animaciones de movimiento

    // Control de acciones por turno
    private boolean usedMovement;
    private boolean usedAttack;

    // Estados activos: mapeados a los turnos que les quedan
    private HashMap<RobotState, Integer> activeStates;

    // Patrón State: Comportamiento dinámico según el estado actual
    private RobotStateBehavior currentState;

    /**
     * Crea un nuevo Robot con sus estadísticas base, equipo y dueño.
     * 
     * @param modelName      Nombre del modelo (ej: "Bullseye").
     * @param description    Descripción narrativa del robot.
     * @param maxHp          Puntos de vida máximos.
     * @param movementPoints Puntos de movimiento por turno.
     * @param weapon         Objeto Weapon asignado.
     * @param skill          Objeto Skill asignado.
     * @param owner          Jugador que controla este robot.
     */
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
        this.currentState = new NormalState();
    }

    /**
     * Mueve el robot a un hexágono destino.
     * No valida el alcance ni el terreno (eso lo hace el sistema de movimiento).
     *
     * @param target hexágono destino.
     */
    public void move(Hexagon target) {
        if (!currentState.canMove(this)) {
            System.out.println(modelName + " no puede mover en su estado actual.");
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
        // Ocupar nueva casilla
        target.setOccupant(this);
        setPosition(target);
        usedMovement = true;
        System.out.println(modelName + " se mueve a " + target);
    }

    /**
     * Realiza un ataque contra otro robot.
     * Aplica modificadores de terreno, diferencia de altura y cobertura.
     *
     * @param target el robot objetivo.
     * @param map    el mapa actual (para calcular visibilidad y cobertura).
     */
    public void attack(Robot target, Map map) {
        if (!currentState.canAttack(this)) {
            System.out.println(modelName + " no puede atacar en su estado actual.");
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
        // Modificador por diferencia de altura entre atacante y defensor
        if (position != null && target.getPosition() != null) {
            damage = Math.round(damage * position.getHeightAttackModifierAgainst(target.getPosition()));
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
        if (!currentState.canUseSkill(this)) {
            System.out.println(modelName + " no puede usar habilidades en su estado actual.");
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

    /** @return true si el estado actual permite mover al robot. */
    public boolean canMove() {
        return currentState.canMove(this);
    }

    /** @return true si el estado actual permite atacar al robot. */
    public boolean canAttack() {
        return currentState.canAttack(this);
    }

    /** @return true si el estado actual permite usar skill al robot. */
    public boolean canUseSkill() {
        return currentState.canUseSkill(this);
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
        updateStatusBehavior();
        System.out.println(modelName + " tiene el estado " + state + " durante " + duration + " turnos.");
    }

    /** Elimina un estado activo del robot. */
    public void removeState(RobotState state) {
        activeStates.remove(state);
        updateStatusBehavior();
    }

    /** @return true si el robot tiene actualmente el estado indicado. */
    public boolean hasState(RobotState state) {
        return activeStates.containsKey(state) && activeStates.get(state) > 0;
    }

    /** @return la duración restante de un estado, o 0 si no lo tiene. */
    public int getStateDuration(RobotState state) {
        return activeStates.getOrDefault(state, 0);
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
        updateStatusBehavior();
    }

    /**
     * Actualiza el objeto de comportamiento según los estados activos.
     * Prioridad: FROZEN > EXHAUSTED > NORMAL.
     */
    private void updateStatusBehavior() {
        if (hasState(RobotState.FROZEN)) {
            this.currentState = new FrozenState();
        } else if (isExhausted()) {
            this.currentState = new ExhaustedState();
        } else {
            this.currentState = new NormalState();
        }
    }

    /** Resetea las acciones del robot al inicio de cada turno. */
    public void resetActions() {
        usedMovement = false;
        usedAttack = false;
        tickStatusEffects(); // Esto actualizará el comportamiento a Normal si no hay estados
        if (skill != null)
            skill.tickCooldown();
        currentState.onTurnBegin(this);
    }

    // Getters y Setters
    /**
     * Obtiene el nombre del modelo.
     * 
     * @return El nombre del robot (ej: "Ice Age").
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Obtiene description.
     * 
     * @return valor resultante de la operacion.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtiene max hp.
     * 
     * @return valor resultante de la operacion.
     */
    public int getMaxHp() {
        return maxHp;
    }

    /**
     * Obtiene los puntos de vida actuales.
     * 
     * @return HP actual.
     */
    public int getCurrentHp() {
        return currentHp;
    }

    /**
     * Obtiene movement points.
     * 
     * @return valor resultante de la operacion.
     */
    public int getMovementPoints() {
        return movementPoints;
    }

    /**
     * Obtiene weapon.
     * 
     * @return valor resultante de la operacion.
     */
    public Weapon getWeapon() {
        return weapon;
    }

    /**
     * Obtiene skill.
     * 
     * @return valor resultante de la operacion.
     */
    public Skill getSkill() {
        return skill;
    }

    /**
     * Obtiene owner.
     * 
     * @return valor resultante de la operacion.
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Obtiene position.
     * 
     * @return valor resultante de la operacion.
     */
    public Hexagon getPosition() {
        return position;
    }

    /**
     * Actualiza la posición física del robot en el mapa.
     * 
     * @param position El Hexagon donde se ubica ahora.
     */
    public void setPosition(Hexagon position) {
        if (this.position != position) {
            this.previousPosition = this.position;
            this.position = position;
        }
    }

    /**
     * Obtiene previous position.
     * 
     * @return valor resultante de la operacion.
     */
    public Hexagon getPreviousPosition() {
        return previousPosition;
    }

    /**
     * Ejecuta la logica de clear previous position.
     */
    public void clearPreviousPosition() {
        this.previousPosition = null;
    }

    /**
     * Indica si used movement.
     * 
     * @return true si la condicion evaluada se cumple; false en caso contrario.
     */
    public boolean isUsedMovement() {
        return usedMovement;
    }

    /**
     * Actualiza used movement.
     * 
     * @param usedMovement parametro de entrada.
     */
    public void setUsedMovement(boolean usedMovement) {
        this.usedMovement = usedMovement;
        updateStatusBehavior();
    }

    /**
     * Indica si used attack.
     * 
     * @return true si la condicion evaluada se cumple; false en caso contrario.
     */
    public boolean isUsedAttack() {
        return usedAttack;
    }

    /**
     * Actualiza used attack.
     * 
     * @param usedAttack parametro de entrada.
     */
    public void setUsedAttack(boolean usedAttack) {
        this.usedAttack = usedAttack;
        updateStatusBehavior();
    }

    @Override
    /**
     * Devuelve una representacion textual del objeto.
     * 
     * @return valor resultante de la operacion.
     */
    public String toString() {
        return modelName + " [HP:" + currentHp + "/" + maxHp + "]";
    }
}
