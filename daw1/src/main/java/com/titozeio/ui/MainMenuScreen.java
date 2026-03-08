package com.titozeio.ui;

import com.titozeio.engine.Game;
import com.titozeio.engine.SoundManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Pantalla de inicio del juego.
 * Esta pantalla ahora utiliza FXML para permitir su edición en Scene Builder.
 */
public class MainMenuScreen extends Screen {

    @FXML
    private StackPane settingsOverlay;

    @FXML
    private CheckBox musicToggle;

    @FXML
    private CheckBox sfxToggle;

    private Stage window;
    private Game game;
    private Scene scene;

    public MainMenuScreen() {
        // Constructor vacío para FXMLLoader
    }

    public static MainMenuScreen create(Stage window, Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainMenuScreen.class.getResource("/com/titozeio/ui/MainMenuScreen.fxml"));
            Parent root = loader.load();
            MainMenuScreen controller = loader.getController();
            controller.window = window;
            controller.game = game;
            controller.scene = new Scene(root, 1280, 720);
            controller.applyGlobalStyle(controller.scene);
            controller.enforceUppercaseText(root);
            controller.initButtonSounds(root);
            return controller;
        } catch (IOException e) {
            System.err.println("Error al cargar MainMenuScreen.fxml: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void handlePlayAction() {
        handleInput("PLAY");
    }

    @FXML
    private void initialize() {
        if (musicToggle != null) {
            musicToggle.setSelected(true);
            musicToggle.setText("ON");
        }
        if (sfxToggle != null) {
            sfxToggle.setSelected(true);
            sfxToggle.setText("ON");
        }
    }

    @FXML
    public void handleSettingsOpenAction() {
        settingsOverlay.setVisible(true);
        settingsOverlay.setManaged(true);
    }

    @FXML
    public void handleSettingsCloseAction() {
        settingsOverlay.setVisible(false);
        settingsOverlay.setManaged(false);
    }

    @FXML
    public void handleMusicToggleAction() {
        boolean enabled = musicToggle.isSelected();
        musicToggle.setText(enabled ? "ON" : "OFF");
        game.setMusicEnabled(enabled);
    }

    @FXML
    public void handleSfxToggleAction() {
        boolean enabled = sfxToggle.isSelected();
        sfxToggle.setText(enabled ? "ON" : "OFF");
        SoundManager.setEnabled(enabled);
        game.setSfxEnabled(enabled);
    }

    @Override
    public void display() {
        this.window.setScene(this.scene);
    }

    @Override
    public void handleInput(String input) {
        if (input.equals("PLAY")) {
            System.out.println("Iniciando transicion a RobotSelectionScreen...");
            this.game.displayScreen(RobotSelectionScreen.create(window, game));
        }
    }
}
