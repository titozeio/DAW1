package com.titozeio.ui;

import com.titozeio.engine.Game;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Pantalla principal de combate.
 * Muestra el estado del juego: turno actual, robots, mapa y mensajes.
 */
public class GameScreen extends Screen {

    private Game game;
    private Stage window;
    private Scene scene;

    public GameScreen(Game game) {
        this.game = game;
        this.window = game.getStage();
        setupUI();
    }

    private void setupUI() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Pantalla de Juego (WIP)");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Aquí se mostrará el mapa y los robots en el futuro.");

        Button surrenderButton = new Button("Rendirse");
        surrenderButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        // Acción de Rendirse: Volver al menú principal
        surrenderButton.setOnAction(e -> {
            System.out.println("El jugador se ha rendido. Volviendo al menú principal...");
            game.displayScreen(new MainMenuScreen(window, game));
        });

        layout.getChildren().addAll(titleLabel, infoLabel, surrenderButton);
        this.scene = new Scene(layout, 800, 600);
    }

    @Override
    public void display() {
        System.out.println("Mostrando GameScreen...");
        this.window.setScene(this.scene);
        System.out.println("--- Turno: " + game.getTurnCounter()
                + " ---");
        // System.out.println("--- Turno: " + game.getTurnCounter()
        // + " | Jugador: " + game.getCurrentPlayer().getName() + " ---");
        // System.out.println("Robots P1: " + game.getP1().getAliveUnits().size() + "
        // vivos");
        // System.out.println("Robots P2: " + game.getP2().getAliveUnits().size() + "
        // vivos");
        // System.out.println("[M] Mover | [A] Atacar | [S] Skill | [F] Fin de turno |
        // [P] Pausa");
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
