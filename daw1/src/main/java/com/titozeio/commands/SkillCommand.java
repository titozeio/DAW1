package com.titozeio.commands;

import com.titozeio.engine.Robot;

/**
 * Comando que encapsula el uso de una habilidad especial.
 */
public class SkillCommand implements GameCommand {
    private final Robot robot;
    private final Object target; // Puede ser Hexagon o Robot
    private final String skillName;

    public SkillCommand(Robot robot, Object target) {
        this.robot = robot;
        this.target = target;
        this.skillName = (robot.getSkill() != null) ? robot.getSkill().getName() : "Habilidad";
    }

    @Override
    public void execute() {
        robot.useSkill(target);
    }

    @Override
    public void undo() {
        throw new UnsupportedOperationException("No se puede deshacer una habilidad.");
    }

    @Override
    public String getDescription() {
        return "⚡ " + robot.getModelName() + " usa " + skillName;
    }
}
