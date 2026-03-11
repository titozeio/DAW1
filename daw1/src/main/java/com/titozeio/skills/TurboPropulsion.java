package com.titozeio.skills;

import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Robot;
import com.titozeio.enums.SkillType;

/**
 * Skill: Turbo Propulsores — Death Knight.
 *
 * Tipo: Movimiento | Puntos de movimiento: 5 | Cooldown: 1 turno.
 * Efecto especial GDD: Ninguno (movimiento normal pero con 5 puntos).
 */
public class TurboPropulsion extends Skill {

    private final int movementPoints = 5;

    /**
     * Crea una nueva instancia de la clase.
     */
    public TurboPropulsion() {
        super(
                "Turbo Propulsores",
                SkillType.MOVEMENT,
                1,
                "Cada dos turnos, el robot puede activar unos turbo propulsores en sus patas, "
                        + "que le impulsan a una velocidad mucho mayor de su movimiento normal.");
    }

    @Override
    /**
     * Ejecuta la logica de execute.
     * @param actor parametro de entrada.
     * @param target parametro de entrada.
     */
    public void execute(Robot actor, Object target) {
        if (!(target instanceof Hexagon)) {
            System.out.println("Turbo Propulsores necesita un Hexagon como destino.");
            return;
        }
        Hexagon destination = (Hexagon) target;

        if (destination.isOccupied()) {
            System.out.println("La casilla destino está ocupada.");
            return;
        }

        System.out.println(actor.getModelName() + " activa ¡Turbo Propulsores! "
                + "(5 puntos de movimiento) → " + destination);

        // Movimiento normal aplicando reglas de terreno y altura (gestionado
        // externamente)
        // Aquí simplemente desplazamos al robot al destino indicado
        if (actor.getPosition() != null)
            actor.getPosition().setOccupant(null);
        destination.setOccupant(actor);
        actor.setPosition(destination);
        actor.setUsedMovement(true);
        resetCooldown();
    }

    /**
     * Obtiene movement points.
     * @return valor resultante de la operacion.
     */
    public int getMovementPoints() {
        return movementPoints;
    }

    @Override
    /**
     * Obtiene display stats.
     * @return valor resultante de la operacion.
     */
    public java.util.List<String[]> getDisplayStats() {
        java.util.List<String[]> stats = new java.util.ArrayList<>();
        stats.add(new String[] { "Movimiento:  ", String.valueOf(movementPoints) });
        return stats;
    }
}
