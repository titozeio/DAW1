package com.titozeio.states;

import com.titozeio.engine.Robot;

public class FrozenState implements RobotStateBehavior {
    @Override
    public boolean canMove(Robot robot) {
        return false;
    }

    @Override
    public boolean canAttack(Robot robot) {
        return false;
    }

    @Override
    public boolean canUseSkill(Robot robot) {
        return false;
    }

    @Override
    public void onTurnBegin(Robot robot) {
        System.out.println(robot.getModelName() + " está congelado y pierde el turno.");
    }
}
