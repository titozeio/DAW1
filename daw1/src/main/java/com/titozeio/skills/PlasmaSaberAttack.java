package com.titozeio.skills;

import com.titozeio.engine.Robot;
import com.titozeio.enums.SkillType;

/**
 * Skill: Sable de Plasma — Victory Saber.
 *
 * Tipo: Ataque | Alcance: 1 | Daño: 10 | Cooldown: 3 turnos.
 * Descripción GDD: El robot despliega un sable de plasma que hace daño masivo.
 */
public class PlasmaSaberAttack extends Skill {

    private static final int RANGE = 1;
    private static final int DAMAGE = 10;

    public PlasmaSaberAttack() {
        super(
                "Sable de Plasma",
                SkillType.ATTACK,
                3,
                "El robot despliega un sable de plasma que hace daño masivo. "
                        + "El sable es tan potente que necesita una cantidad brutal de energía para volver a generarse.");
    }

    @Override
    public void execute(Robot actor, Object target) {
        if (!(target instanceof Robot)) {
            System.out.println("Sable de Plasma necesita un robot como objetivo.");
            return;
        }
        Robot defender = (Robot) target;
        if (actor.getPosition() == null || defender.getPosition() == null)
            return;

        // TODO: validar alcance 1 con el mapa (pasarlo como parámetro en una iteración
        // futura)
        System.out.println(actor.getModelName() + " usa ¡Sable de Plasma! sobre " + defender.getModelName());
        defender.takeDamage(DAMAGE);
        actor.setUsedAttack(true);
        resetCooldown();
        System.out.println("Impacto: " + DAMAGE + " puntos de daño a " + defender);
    }

    public int getRange() {
        return RANGE;
    }

    public int getDamage() {
        return DAMAGE;
    }
}
