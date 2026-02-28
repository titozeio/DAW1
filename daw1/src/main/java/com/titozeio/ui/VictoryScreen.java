package com.titozeio.ui;

import com.titozeio.engine.Game;
import com.titozeio.engine.Player;
import javafx.stage.Stage;

/**
 * Pantalla de victoria. Muestra el ganador y vuelve al menú principal.
 */
public class VictoryScreen extends Screen {

    private Player winner;
    private Stage window;
    private Game game;

    public VictoryScreen(Stage window, Game game, Player winner) {
        this.window = window;
        this.game = game;
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
            game.displayScreen(MainMenuScreen.create(window, game));
        }
    }

    public Player getWinner() {
        return winner;
    }
}
