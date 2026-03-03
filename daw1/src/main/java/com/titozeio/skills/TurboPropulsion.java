package com.titozeio.skills;

import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Robot;
import com.titozeio.enums.SkillType;

/**
 * Skill: Turbo Propulsores — Death Knight.
 *
 * Tipo: Movimiento | Puntos de movimiento: 7 | Cooldown: 1 turno.
 * Efecto especial GDD: Ninguno (movimiento normal pero con 7 puntos).
 */
public class TurboPropulsion extends Skill {

    private final int movementPoints = 7;

    public TurboPropulsion() {
        super(
                "Turbo Propulsores",
                SkillType.MOVEMENT,
                1,
                "Cada dos turnos, el robot puede activar unos turbo propulsores en sus patas, "
                        + "que le impulsan a una velocidad mucho mayor de su movimiento normal.");
    }

    @Override
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
                + "(7 puntos de movimiento) → " + destination);

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

    public int getMovementPoints() {
        return movementPoints;
    }

    @Override
    public java.util.List<String[]> getDisplayStats() {
        java.util.List<String[]> stats = new java.util.ArrayList<>();
        stats.add(new String[] { "Movimiento:  ", String.valueOf(movementPoints) });
        return stats;
    }
}
