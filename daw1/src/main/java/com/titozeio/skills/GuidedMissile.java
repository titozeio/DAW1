package com.titozeio.skills;

import com.titozeio.engine.Robot;
import com.titozeio.enums.SkillType;

/**
 * Skill: Misiles Teledirigidos — Bullseye.
 *
 * Tipo: Ataque | Alcance: 5 | Daño: 5 | Cooldown: 2 turnos.
 * Efecto especial GDD: Permite atacar sin visibilidad e ignorando reglas de
 * cobertura.
 */
public class GuidedMissile extends Skill {

    private static final int RANGE = 5;
    private static final int DAMAGE = 5;
    private final boolean ignoresCover = true;
    private final boolean ignoresVisibility = true;

    public GuidedMissile() {
        super(
                "Misiles Teledirigidos",
                SkillType.ATTACK,
                2,
                "El robot cuenta con misiles teledirigidos que viajan hacia el objetivo rodeando obstáculos, "
                        + "permitiendo ignorar todo tipo de cobertura y visibilidad.");
    }

    @Override
    public void execute(Robot actor, Object target) {
        if (!(target instanceof Robot)) {
            System.out.println("Misiles Teledirigidos necesita un robot como objetivo.");
            return;
        }
        Robot defender = (Robot) target;
        if (actor.getPosition() == null || defender.getPosition() == null)
            return;

        // TODO: validar alcance 5 con el mapa
        System.out.println(actor.getModelName() + " usa ¡Misiles Teledirigidos! sobre " + defender.getModelName()
                + " (ignora visibilidad y cobertura).");
        // Daño directo: ignora visibilidad y cobertura
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

    public boolean isIgnoresCover() {
        return ignoresCover;
    }

    public boolean isIgnoresVisibility() {
        return ignoresVisibility;
    }

    @Override
    public java.util.List<String[]> getDisplayStats() {
        return java.util.List.of(
                new String[] { "Alcance:  ", String.valueOf(RANGE) },
                new String[] { "Daño:  ", String.valueOf(DAMAGE) });
    }
}
