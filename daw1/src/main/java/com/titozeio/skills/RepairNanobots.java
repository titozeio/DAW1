package com.titozeio.skills;

import com.titozeio.engine.Robot;
import com.titozeio.enums.SkillType;

/**
 * Skill: Nanobots Reparadores — Bulwark.
 *
 * Tipo: Ataque | Alcance: 1 | Heal: 5 | Cooldown: 2 turnos.
 * Efecto especial GDD: El robot objetivo RECUPERA HP en lugar de recibir daño.
 * No se puede usar sobre robots ya destruidos.
 */
public class RepairNanobots extends Skill {

    private final int range = 1;
    private final int healAmount = 5;

    public RepairNanobots() {
        super(
                "Nanobots Reparadores",
                SkillType.ATTACK,
                2,
                "Despliega un enjambre de nanobots sobre un robot aliado. "
                        + "Los nanobots restauran las partes dañadas del robot aliado.");
    }

    @Override
    public void execute(Robot actor, Object target) {
        if (!(target instanceof Robot)) {
            System.out.println("Nanobots Reparadores necesita un robot aliado como objetivo.");
            return;
        }
        Robot ally = (Robot) target;

        if (ally.isDestroyed()) {
            System.out.println("No se puede usar Nanobots Reparadores sobre un robot destruido.");
            return;
        }
        if (!actor.getOwner().equals(ally.getOwner())) {
            System.out.println("Nanobots Reparadores solo puede usarse sobre robots aliados.");
            return;
        }

        // TODO: validar alcance 1 con el mapa
        System.out.println(actor.getModelName() + " despliega ¡Nanobots Reparadores! sobre " + ally.getModelName());
        ally.heal(healAmount);
        actor.setUsedAttack(true);
        resetCooldown();
    }

    public int getRange() {
        return range;
    }

    public int getHealAmount() {
        return healAmount;
    }

    @Override
    public java.util.List<String[]> getDisplayStats() {
        return java.util.List.of(
                new String[] { "Alcance:  ", String.valueOf(range) },
                new String[] { "Curación:  ", String.valueOf(healAmount) + " HP" });
    }
}
