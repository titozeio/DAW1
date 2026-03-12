package com.titozeio.ai;

import com.titozeio.commands.GameCommand;

/**
 * Decisión que encapsula la ejecución de un comando.
 */
public class ExecuteCommandDecision extends TurnDecision {
    private final GameCommand command;

    public ExecuteCommandDecision(GameCommand command) {
        this.command = command;
    }

    public GameCommand getCommand() {
        return command;
    }
}
