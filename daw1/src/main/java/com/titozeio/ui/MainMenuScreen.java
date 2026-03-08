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
            controller.syncAudioTogglesWithState();
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
        // El estado real se sincroniza en create(), cuando game ya está asignado.
        if (musicToggle != null)
            musicToggle.setText("ON");
        if (sfxToggle != null)
            sfxToggle.setText("ON");
    }

    private void syncAudioTogglesWithState() {
        if (game == null) {
            return;
        }
        if (musicToggle != null) {
            boolean musicOn = game.isMusicEnabled();
            musicToggle.setSelected(musicOn);
            musicToggle.setText(musicOn ? "ON" : "OFF");
        }
        if (sfxToggle != null) {
            boolean sfxOn = game.isSfxEnabled();
            sfxToggle.setSelected(sfxOn);
            sfxToggle.setText(sfxOn ? "ON" : "OFF");
            SoundManager.setEnabled(sfxOn);
        }
    }

    @FXML
    public void handleSettingsOpenAction() {
        fadeInNode(settingsOverlay, 150);
    }

    @FXML
    public void handleSettingsCloseAction() {
        fadeOutNode(settingsOverlay, 150);
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
