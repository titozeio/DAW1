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

    /**
     * Resetea las acciones del robot al inicio de cada turno.
     * Restablece los flags de movimiento y ataque, actualiza los efectos de estado
     * y reduce el tiempo de reutilización de la habilidad especial.
     */
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
     * Obtiene el nombre del modelo del robot.
     * 
     * @return El nombre identificativo (ej: "Saber Prime").
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Obtiene la descripción narrativa del robot.
     * 
     * @return Texto con el lore o detalles del modelo.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtiene los puntos de vida máximos originales del robot.
     * 
     * @return El valor de HP máximo.
     */
    public int getMaxHp() {
        return maxHp;
    }

    /**
     * Obtiene los puntos de vida actuales del robot.
     * 
     * @return HP actual restante.
     */
    public int getCurrentHp() {
        return currentHp;
    }

    /**
     * Obtiene los puntos de movimiento base del robot.
     * 
     * @return Capacidad de desplazamiento por turno.
     */
    public int getMovementPoints() {
        return movementPoints;
    }

    /**
     * Obtiene el arma equipada por el robot.
     * 
     * @return Instancia de Weapon vinculada.
     */
    public Weapon getWeapon() {
        return weapon;
    }

    /**
     * Obtiene la habilidad especial del robot.
     * 
     * @return Instancia de Skill vinculada.
     */
    public Skill getSkill() {
        return skill;
    }

    /**
     * Obtiene el jugador propietario del robot.
     * 
     * @return Referencia al Player dueño.
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Obtiene la casilla actual del robot en el mapa.
     * 
     * @return Instancia de Hexagon donde reside el robot.
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
     * Obtiene la posición inmediatamente anterior del robot.
     * Útil para interpolación de animaciones.
     * 
     * @return El Hexagon donde estaba antes del último movimiento.
     */
    public Hexagon getPreviousPosition() {
        return previousPosition;
    }

    /**
     * Limpia la referencia a la posición anterior.
     */
    public void clearPreviousPosition() {
        this.previousPosition = null;
    }

    /**
     * Comprueba si el robot ya ha realizado su acción de movimiento este turno.
     * 
     * @return true si ya movió.
     */
    public boolean isUsedMovement() {
        return usedMovement;
    }

    /**
     * Establece si el robot ha usado su movimiento y actualiza su comportamiento.
     * 
     * @param usedMovement Nuevo estado de la acción de movimiento.
     */
    public void setUsedMovement(boolean usedMovement) {
        this.usedMovement = usedMovement;
        updateStatusBehavior();
    }

    /**
     * Comprueba si el robot ya ha realizado su acción de ataque este turno.
     * 
     * @return true si ya atacó.
     */
    public boolean isUsedAttack() {
        return usedAttack;
    }

    /**
     * Establece si el robot ha usado su ataque y actualiza su comportamiento.
     * 
     * @param usedAttack Nuevo estado de la acción de ataque.
     */
    public void setUsedAttack(boolean usedAttack) {
        this.usedAttack = usedAttack;
        updateStatusBehavior();
    }

    @Override
    /**
     * Devuelve una representación textual simplificada del robot.
     * 
     * @return Nombre del modelo y estado de salud actual.
     */
    public String toString() {
        return modelName + " [HP:" + currentHp + "/" + maxHp + "]";
    }
}
