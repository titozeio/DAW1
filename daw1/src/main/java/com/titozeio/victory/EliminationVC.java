package com.titozeio.victory;

import com.titozeio.engine.Game;
import com.titozeio.engine.Player;

/**
 * Condición de victoria: Eliminar todos los robots del rival.
 * Se cumple cuando todos los robots del jugador contrario están destruidos.
 */
public class EliminationVC extends VictoryCondition {

    @Override
    /**
     * Ejecuta la logica de check.
     * @param game parametro de entrada.
     * @return valor resultante de la operacion.
     */
    public Player check(Game game) {
        boolean p1Eliminated = game.getP1().isEliminated();
        boolean p2Eliminated = game.getP2().isEliminated();

        // Si ambos están eliminados, no hay ganador claro
        if (p1Eliminated && p2Eliminated) {
            return null;
        }

        if (p2Eliminated)
            return game.getP1();
        if (p1Eliminated)
            return game.getP2();
        return null;
    }
}
