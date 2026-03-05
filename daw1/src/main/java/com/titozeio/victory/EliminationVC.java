package com.titozeio.victory;

import com.titozeio.engine.Game;
import com.titozeio.engine.Player;

/**
 * Condición de victoria: Eliminar todos los robots del rival.
 * Se cumple cuando todos los robots del jugador contrario están destruidos.
 */
public class EliminationVC extends VictoryCondition {

    @Override
    public Player check(Game game) {
        if (game.getP2().isEliminated())
            return game.getP1();
        if (game.getP1().isEliminated())
            return game.getP2();
        return null;
    }
}
