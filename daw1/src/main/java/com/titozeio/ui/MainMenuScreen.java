package com.titozeio.ui;

import com.titozeio.engine.Game;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Pantalla de inicio del juego.
 * Muestra el título "Devastation Ai Wars 1" y el botón de "Jugar".
 */
public class MainMenuScreen extends Screen {

    private Stage window;
    private Game game;
    private Scene scene;

    public MainMenuScreen(Stage window, Game game) {
        this.window = window;
        this.game = game;
        setupUI();
    }

    private void setupUI() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Devastation Ai Wars 1");
        Button playButton = new Button("Jugar");

        // Asigna la accion al boton
        playButton.setOnAction(e -> handleInput("PLAY"));

        layout.getChildren().addAll(titleLabel, playButton);
        this.scene = new Scene(layout);
    }

    @Override
    public void display() {
        // Coloca esta escena en la ventana principal
        this.window.setScene(this.scene);
    }

    @Override
    public void handleInput(String input) {
        if (input.equals("PLAY")) {
            System.out.println("Iniciando transicion a GameScreen...");
            // Logica futura para cambiar de pantalla:
            // this.game.displayScreen(new GameScreen(window, game));
        }
    }
}
