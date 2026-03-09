package com.titozeio.skills;

import com.titozeio.engine.Robot;
import com.titozeio.enums.RobotState;
import com.titozeio.enums.SkillType;

/**
 * Skill: Rayo Congelador — Ice Age.
 *
 * Tipo: Ataque | Alcance: 3 | Daño: 3 | Cooldown: 2 turnos.
 * Efecto especial GDD: El robot objetivo no puede actuar durante 1 turno
 * (estado FROZEN).
 */
public class FreezeRay extends Skill {

    private final int range = 3;
    private final int damage = 3;
    private final int freezeDuration = 1; // Turnos que dura el estado FROZEN

    /**
     * Crea una nueva instancia de la clase.
     */
    public FreezeRay() {
        super(
                "Rayo Congelador",
                SkillType.ATTACK,
                2,
                "El robot dispara un rayo de frío criogénico que congela al robot objetivo, "
                        + "impidiendo que pueda actuar durante 1 turno.");
    }

    @Override
    /**
     * Ejecuta la logica de execute.
     * @param actor parametro de entrada.
     * @param target parametro de entrada.
     */
    public void execute(Robot actor, Object target) {
        if (!(target instanceof Robot)) {
            System.out.println("Rayo Congelador necesita un robot como objetivo.");
            return;
        }
        Robot defender = (Robot) target;
        if (actor.getPosition() == null || defender.getPosition() == null)
            return;

        if (!isTargetInRange(actor, defender, range)) {
            System.out.println("Rayo Congelador fuera de alcance (" + range + ").");
            return;
        }
        System.out.println(actor.getModelName() + " dispara ¡Rayo Congelador! sobre " + defender.getModelName());
        defender.takeDamage(damage);
        defender.applyState(RobotState.FROZEN, freezeDuration);
        actor.setUsedAttack(true);
        resetCooldown();
        System.out.println(defender.getModelName() + " queda CONGELADO durante " + freezeDuration + " turno(s).");
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
     * Obtiene freeze duration.
     * @return valor resultante de la operacion.
     */
    public int getFreezeDuration() {
        return freezeDuration;
    }

    @Override
    /**
     * Obtiene display stats.
     * @return valor resultante de la operacion.
     */
    public java.util.List<String[]> getDisplayStats() {
        return java.util.List.of(
                new String[] { "Alcance:  ", String.valueOf(range) },
                new String[] { "Daño:  ", String.valueOf(damage) });
    }
}
