package com.titozeio.ui;

import com.titozeio.engine.Game;

import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;

/**
 * Pantalla de inicio del juego.
 * Esta pantalla ahora utiliza FXML para permitir su edición en Scene Builder.
 */
public class MainMenuScreen extends Screen {

    private Stage window;
    private Game game;
    private Scene scene;
    private Parent root;

    public MainMenuScreen(Stage window, Game game) {
        this.window = window;
        this.game = game;
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/titozeio/ui/MainMenuScreen.fxml"));
            loader.setController(this);
            this.root = loader.load();
            this.scene = new Scene(this.root, 1280, 720);
            applyGlobalStyle(this.scene);
        } catch (IOException e) {
            System.err.println("Error al cargar MainMenuScreen.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePlayAction() {
        handleInput("PLAY");
    }

    @Override
    public void display() {
        // Coloca esta escena en la ventana principal
        this.window.setScene(this.scene);
    }

    @Override
    public void handleInput(String input) {
        if (input.equals("PLAY")) {
            System.out.println("Iniciando transicion a RobotSelectionScreen...");
            this.game.displayScreen(new RobotSelectionScreen(window, game));
        }
    }
}
