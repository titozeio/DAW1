package com.titozeio.victory;

import com.titozeio.engine.Game;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;

/**
 * Condición de victoria: Captura de base rival.
 * Según el GDD: un robot aliado debe sobrevivir 2 turnos posicionado en la base
 * enemiga.
 *
 * La base de P1 está en Hexagon marcado como isBaseP1 = true.
 * La base de P2 está en Hexagon marcado como isBaseP2 = true.
 */
public class BaseCaptureVC extends VictoryCondition {

    /**
     * Número de turnos que un robot debe permanecer en la base enemiga para ganar.
     */
    private final int turnsToCapture;

    public BaseCaptureVC(int turnsToCapture) {
        this.turnsToCapture = turnsToCapture;
    }

    public BaseCaptureVC() {
        this(2); // Valor por defecto del GDD
    }

    @Override
    public Player check(Game game) {
        // Comprobar si P1 ha capturado la base de P2
        if (checkCapture(game.getP1(), game.getP2(), game)) {
            return game.getP1();
        }
        // Comprobar si P2 ha capturado la base de P1
        if (checkCapture(game.getP2(), game.getP1(), game)) {
            return game.getP2();
        }
        return null;
    }

    /**
     * Comprueba si el jugador atacante tiene un robot en la base del defensivo
     * y ha llegado al número de turnos necesario.
     */
    private boolean checkCapture(Player attacker, Player defender, Game game) {
        Hexagon defenderBase = defender.getBaseLocation();
        if (defenderBase == null)
            return false;

        Robot occupant = defenderBase.getOccupant();
        if (occupant != null && occupant.getOwner().equals(attacker) && !occupant.isDestroyed()) {
            defender.incrementBaseControlTurns();
            if (defender.getBaseControlTurns() >= turnsToCapture) {
                return true;
            }
        } else {
            defender.resetBaseControlTurns();
        }
        return false;
    }

    public int getTurnsToCapture() {
        return turnsToCapture;
    }
}
