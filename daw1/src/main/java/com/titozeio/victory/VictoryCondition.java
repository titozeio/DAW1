package com.titozeio.victory;

import com.titozeio.engine.Game;
import com.titozeio.engine.Player;

/**
 * Interfaz abstracta para las condiciones de victoria.
 * Cada condición implementa check() y devuelve el jugador ganador, o null.
 */
public abstract class VictoryCondition {

    /**
     * Evalúa si se ha cumplido esta condición de victoria.
     * 
     * @param game el estado actual del juego.
     * @return el jugador ganador, o null si nadie ha ganado aún.
     */
    public abstract Player check(Game game);
}
