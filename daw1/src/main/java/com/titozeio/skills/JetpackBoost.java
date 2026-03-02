package com.titozeio.skills;

import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Robot;
import com.titozeio.enums.SkillType;

/**
 * Skill: Retropropulsores — Scout.
 *
 * Tipo: Movimiento | Puntos de movimiento: 3 | Cooldown: 1 turno.
 * Efecto especial GDD: Ignora el tipo de terreno Y los límites de altura al
 * calcular
 * el coste de movimiento (puede pasar de altura 0 a altura 2, por ejemplo).
 */
public class JetpackBoost extends Skill {

    private final int movementPoints = 3;
    private final boolean ignoresTerrainCost = true;
    private final boolean ignoresHeightLimits = true;

    public JetpackBoost() {
        super(
                "Retropropulsores",
                SkillType.MOVEMENT,
                1,
                "El robot cuenta con unos retropropulsores que le impulsan por el aire. "
                        + "No llega a poder volar, ya que el impulso dura muy poco dado el coste energético.");
    }

    @Override
    public void execute(Robot actor, Object target) {
        if (!(target instanceof Hexagon)) {
            System.out.println("Retropropulsores necesita un Hexagon como destino.");
            return;
        }
        Hexagon destination = (Hexagon) target;

        if (destination.isOccupied()) {
            System.out.println("La casilla destino está ocupada.");
            return;
        }

        System.out.println(actor.getModelName() + " activa ¡Retropropulsores! "
                + "(ignora terreno y altura) → " + destination);

        // Desocupar casilla actual
        if (actor.getPosition() != null)
            actor.getPosition().setOccupant(null);
        // Mover sin aplicar daño de caída ni restricciones de terreno
        destination.setOccupant(actor);
        actor.setPosition(destination);
        actor.setUsedMovement(true);
        resetCooldown();
    }

    public int getMovementPoints() {
        return movementPoints;
    }

    public boolean isIgnoresTerrainCost() {
        return ignoresTerrainCost;
    }

    public boolean isIgnoresHeightLimits() {
        return ignoresHeightLimits;
    }

    @Override
    public java.util.List<String[]> getDisplayStats() {
        java.util.List<String[]> stats = new java.util.ArrayList<>();
        stats.add(new String[] { "Movimiento:  ", String.valueOf(movementPoints) });
        return stats;
    }
}
