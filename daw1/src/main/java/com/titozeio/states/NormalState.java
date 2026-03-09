package com.titozeio.states;

import com.titozeio.engine.Robot;

public class NormalState implements RobotStateBehavior {
    @Override
    public boolean canMove(Robot robot) {
        return !robot.isUsedMovement();
    }

    @Override
    public boolean canAttack(Robot robot) {
        return !robot.isUsedAttack();
    }

    @Override
    public boolean canUseSkill(Robot robot) {
        return robot.getSkill() != null && robot.getSkill().isReady();
    }

    @Override
    public void onTurnBegin(Robot robot) {
        // Nada especial, el robot está operativo
    }
}
