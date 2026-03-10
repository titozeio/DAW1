package com.titozeio.states;

import com.titozeio.engine.Robot;

public class NormalState implements RobotStateBehavior {
    @Override
    public boolean canMove(Robot robot) {
        if (robot == null) {
            return false;
        }
        return !robot.isUsedMovement();
    }

    @Override
    public boolean canAttack(Robot robot) {
        if (robot == null) {
            return false;
        }
        return !robot.isUsedAttack();
    }

    @Override
    public boolean canUseSkill(Robot robot) {
        if (robot == null) {
            return false;
        }
        return robot.getSkill() != null && robot.getSkill().isReady();
    }

    @Override
    public void onTurnBegin(Robot robot) {
        // Nada especial, el robot está operativo
    }
}
