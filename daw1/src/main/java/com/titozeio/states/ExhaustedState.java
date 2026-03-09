package com.titozeio.states;

import com.titozeio.engine.Robot;

public class ExhaustedState implements RobotStateBehavior {
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
        // Al inicio del turno se reseteará a NormalState
    }
}
