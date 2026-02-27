package com.titozeio.ui;

import com.titozeio.engine.Player;

/**
 * Pantalla de victoria. Muestra el ganador y vuelve al menú principal.
 */
public class VictoryScreen extends Screen {

    private Player winner;

    public VictoryScreen(Player winner) {
        this.winner = winner;
    }

    @Override
    public void display() {
        System.out.println("=====================================");
        System.out.println("  ¡VICTORIA! — " + winner.getName());
        System.out.println("=====================================");
        System.out.println("[1] Volver al menú principal");
    }

    @Override
    public void handleInput(String input) {
        if ("1".equals(input.trim())) {
            System.out.println("Volviendo al menú principal...");
            new MainMenuScreen().display();
        }
    }

    public Player getWinner() {
        return winner;
    }
}
