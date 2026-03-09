package com.titozeio.engine;

import com.titozeio.commands.GameCommand;
import java.util.Stack;

/**
 * Invoker del patrón Comando.
 * Gestiona la ejecución y el historial de acciones.
 */
public class ActionManager {
    private static ActionManager instance;
    private final Stack<GameCommand> history = new Stack<>();

    public static synchronized ActionManager getInstance() {
        if (instance == null)
            instance = new ActionManager();
        return instance;
    }

    private ActionManager() {
    }

    /**
     * Ejecuta un comando y lo añade al historial.
     */
    public void executeCommand(GameCommand command) {
        command.execute();
        history.push(command);
        System.out.println("[ActionLog] " + command.getDescription());
    }

    /**
     * Deshace la última acción si es posible.
     */
    public void undoLastCommand() {
        if (!history.isEmpty()) {
            GameCommand cmd = history.pop();
            try {
                cmd.undo();
                System.out.println("[ActionUndo] " + cmd.getDescription());
            } catch (UnsupportedOperationException e) {
                System.err.println("No se pudo deshacer: " + e.getMessage());
                // Si no se puede, lo volvemos a meter? Generalmente se pierde.
            }
        }
    }

    public void clearHistory() {
        history.clear();
    }
}
