package com.titozeio.skills;

import com.titozeio.engine.Robot;
import com.titozeio.enums.SkillType;

/**
 * Skill: Misiles Teledirigidos — Bullseye.
 *
 * Tipo: Ataque | Alcance: 6 | Daño: 5 | Cooldown: 2 turnos.
 * Efecto especial GDD: Permite atacar sin visibilidad e ignorando reglas de
 * cobertura.
 */
public class GuidedMissile extends Skill {

    private static final int RANGE = 6;
    private static final int DAMAGE = 5;
    private final boolean ignoresCover = true;
    private final boolean ignoresVisibility = true;

    /**
     * Crea una nueva instancia de la clase.
     */
    public GuidedMissile() {
        super(
                "Misiles Teledirigidos",
                SkillType.ATTACK,
                2,
                "El robot cuenta con misiles teledirigidos que viajan hacia el objetivo rodeando obstáculos, "
                        + "permitiendo ignorar todo tipo de cobertura y visibilidad.");
    }

    @Override
    /**
     * Ejecuta la logica de execute.
     * @param actor parametro de entrada.
     * @param target parametro de entrada.
     */
    public void execute(Robot actor, Object target) {
        if (!(target instanceof Robot)) {
            System.out.println("Misiles Teledirigidos necesita un robot como objetivo.");
            return;
        }
        Robot defender = (Robot) target;
        if (actor.getPosition() == null || defender.getPosition() == null)
            return;

        if (!isTargetInRange(actor, defender, RANGE)) {
            System.out.println("Misiles Teledirigidos fuera de alcance (" + RANGE + ").");
            return;
        }
        System.out.println(actor.getModelName() + " usa ¡Misiles Teledirigidos! sobre " + defender.getModelName()
                + " (ignora visibilidad y cobertura).");
        // Daño directo: ignora visibilidad y cobertura
        defender.takeDamage(DAMAGE);
        actor.setUsedAttack(true);
        resetCooldown();
        System.out.println("Impacto: " + DAMAGE + " puntos de daño a " + defender);
    }

    /**
     * Obtiene range.
     * @return valor resultante de la operacion.
     */
    public int getRange() {
        return RANGE;
    }

    /**
     * Obtiene damage.
     * @return valor resultante de la operacion.
     */
    public int getDamage() {
        return DAMAGE;
    }

    /**
     * Indica si ignores cover.
     * @return true si la condicion evaluada se cumple; false en caso contrario.
     */
    public boolean isIgnoresCover() {
        return ignoresCover;
    }

    /**
     * Indica si ignores visibility.
     * @return true si la condicion evaluada se cumple; false en caso contrario.
     */
    public boolean isIgnoresVisibility() {
        return ignoresVisibility;
    }

    @Override
    /**
     * Obtiene display stats.
     * @return valor resultante de la operacion.
     */
    public java.util.List<String[]> getDisplayStats() {
        return java.util.List.of(
                new String[] { "Alcance:  ", String.valueOf(RANGE) },
                new String[] { "Daño:  ", String.valueOf(DAMAGE) });
    }
}
