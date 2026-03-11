package com.titozeio.ui;

import com.titozeio.engine.Game;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Pantalla de inicio del juego.
 * Esta pantalla ahora utiliza FXML para permitir su edición en Scene Builder.
 */
public class MainMenuScreen extends Screen {

    @FXML
    private TutorialOverlayController tutorialOverlayComponentController;
    @FXML
    private SettingsOverlayController settingsOverlayComponentController;

    private Stage window;
    private Game game;
    private Scene scene;

    /**
     * Crea una nueva instancia de la clase.
     */
    public MainMenuScreen() {
        // Constructor vacío para FXMLLoader
    }

    /**
     * Ejecuta la logica de create.
     * 
     * @param window parametro de entrada.
     * @param game   parametro de entrada.
     * @return valor resultante de la operacion.
     */
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
    /**
     * Ejecuta la logica de handle play action.
     */
    public void handlePlayAction() {
        handleInput("PLAY");
    }

    @FXML
    private void initialize() {
        // Sin inicialización local: se delega al componente SettingsOverlay.
    }

    @FXML
    /**
     * Ejecuta la logica de handle settings open action.
     */
    public void handleSettingsOpenAction() {
        if (settingsOverlayComponentController != null) {
            settingsOverlayComponentController.show(game);
        }
    }

    @FXML
    public void handleTutorialOpenAction() {
        if (tutorialOverlayComponentController != null) {
            tutorialOverlayComponentController.show();
        }
    }

    @FXML
    public void handleTutorialCloseAction() {
        if (tutorialOverlayComponentController != null) {
            tutorialOverlayComponentController.close();
        }
    }

    @Override
    /**
     * Ejecuta la logica de display.
     */
    public void display() {
        this.window.setScene(this.scene);
    }

    @Override
    /**
     * Ejecuta la logica de handle input.
     * 
     * @param input parametro de entrada.
     */
    public void handleInput(String input) {
        if (input.equals("PLAY")) {
            System.out.println("Iniciando transicion a RobotSelectionScreen...");
            game.resetForNewMatch();
            this.game.displayScreen(RobotSelectionScreen.create(window, game));
        }
    }
}
