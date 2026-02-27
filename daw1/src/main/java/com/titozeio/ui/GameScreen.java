package com.titozeio.ui;

import com.titozeio.engine.Game;

/**
 * Pantalla principal de combate.
 * Muestra el estado del juego: turno actual, robots, mapa y mensajes.
 */
public class GameScreen extends Screen {

    private Game game;

    public GameScreen(Game game) {
        this.game = game;
    }

    @Override
    public void display() {
        System.out.println("--- Turno: " + game.getTurnCounter()
                + " | Jugador: " + game.getCurrentPlayer().getName() + " ---");
        System.out.println("Robots P1: " + game.getP1().getAliveUnits().size() + " vivos");
        System.out.println("Robots P2: " + game.getP2().getAliveUnits().size() + " vivos");
        System.out.println("[M] Mover | [A] Atacar | [S] Skill | [F] Fin de turno | [P] Pausa");
    }

    @Override
    public void handleInput(String input) {
        switch (input.trim().toUpperCase()) {
            case "M":
                System.out.println("Modo: Selecciona un robot para mover.");
                break;
            case "A":
                System.out.println("Modo: Selecciona un robot para atacar.");
                break;
            case "S":
                System.out.println("Modo: Selecciona un robot para usar su skill.");
                break;
            case "F":
                System.out.println("Fin de turno.");
                game.nextTurn();
                break;
            case "P":
                System.out.println("Juego pausado.");
                break;
            default:
                System.out.println("Acción no reconocida.");
        }
    }
}
