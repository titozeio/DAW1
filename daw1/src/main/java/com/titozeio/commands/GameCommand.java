package com.titozeio.commands;

/**
 * Interfaz base para el patrón Comando.
 * Permite encapsular una acción de juego para su ejecución, registro y posible
 * deshacer.
 */
public interface GameCommand {
    /** Ejecuta la acción. */
    void execute();

    /** Deshace la acción (vuelve al estado anterior si es posible). */
    void undo();

    /** @return Una descripción legible de lo que hace el comando. */
    String getDescription();
}
