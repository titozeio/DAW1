package com.titozeio.commands;

import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Robot;

/**
 * Comando que encapsula el movimiento de un robot de una casilla a otra.
 */
public class MoveCommand implements GameCommand {
    private final Robot robot;
    private final Hexagon targetHex;
    private final Hexagon previousHex;

    public MoveCommand(Robot robot, Hexagon targetHex) {
        this.robot = robot;
        this.targetHex = targetHex;
        this.previousHex = robot.getPosition();
    }

    @Override
    public void execute() {
        robot.move(targetHex);
    }

    @Override
    public void undo() {
        if (previousHex != null) {
            // Revertir posición
            if (robot.getPosition() != null) {
                robot.getPosition().setOccupant(null);
            }
            robot.setPosition(previousHex);
            previousHex.setOccupant(robot);

            // Revertir estado de acción (permitir mover de nuevo este turno)
            robot.setUsedMovement(false);
        }
    }

    @Override
    public String getDescription() {
        return robot.getModelName() + " se mueve a (" + targetHex.getQ() + "," + targetHex.getR() + ")";
    }
}
