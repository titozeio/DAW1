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

    public FreezeRay() {
        super(
                "Rayo Congelador",
                SkillType.ATTACK,
                2,
                "El robot dispara un rayo de frío criogénico que congela al robot objetivo, "
                        + "impidiendo que pueda actuar durante 1 turno.");
    }

    @Override
    public void execute(Robot actor, Object target) {
        if (!(target instanceof Robot)) {
            System.out.println("Rayo Congelador necesita un robot como objetivo.");
            return;
        }
        Robot defender = (Robot) target;
        if (actor.getPosition() == null || defender.getPosition() == null)
            return;

        // TODO: validar alcance 3 con el mapa
        System.out.println(actor.getModelName() + " dispara ¡Rayo Congelador! sobre " + defender.getModelName());
        defender.takeDamage(damage);
        defender.applyState(RobotState.FROZEN, freezeDuration);
        actor.setUsedAttack(true);
        resetCooldown();
        System.out.println(defender.getModelName() + " queda CONGELADO durante " + freezeDuration + " turno(s).");
    }

    public int getRange() {
        return range;
    }

    public int getDamage() {
        return damage;
    }

    public int getFreezeDuration() {
        return freezeDuration;
    }
}
