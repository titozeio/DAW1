package com.titozeio.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.titozeio.engine.Game;
import com.titozeio.enums.RobotTemplate;

/**
 * Pantalla de selección de robots.
 * Muestra los robots disponibles y permite seleccionar 3 robots para cada
 * jugador.
 */
public class RobotSelectionScreen extends Screen {

    // Constante para el máximo de robots permitidos
    private static final int MAX_ROBOTS_PER_PLAYER = 3;

    // Enlaces con los elementos de la interfaz FXML
    @FXML
    private Label fixedInstructionsLabel;
    @FXML
    private Label turnInstructionsLabel;
    @FXML
    private FlowPane availableRobotsPane;
    @FXML
    private Button playButton;

    // Estado interno
    private List<RobotTemplate> availableTemplates;
    private List<RobotTemplate> player1Templates;
    private List<RobotTemplate> player2Templates;
    private boolean isPlayer2Turn;

    private Stage stage;
    private Game game;
    private Scene scene;

    public RobotSelectionScreen() {
        // Constructor vacío para FXMLLoader
    }

    public static RobotSelectionScreen create(Stage window, Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    RobotSelectionScreen.class.getResource("/com/titozeio/ui/RobotSelectionScreen.fxml"));
            Parent root = loader.load();
            RobotSelectionScreen controller = loader.getController();
            controller.stage = window;
            controller.game = game;
            controller.scene = new Scene(root, 1280, 720);
            controller.applyGlobalStyle(controller.scene);

            // Inicializar datos: pasar todos los RobotTemplate del enum
            controller.initializeData(new ArrayList<>(Arrays.asList(RobotTemplate.values())));
            controller.setupInitialUI();

            return controller;
        } catch (IOException e) {
            System.err.println("Error al cargar RobotSelectionScreen.fxml: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void setupInitialUI() {
        refreshRobotButtons();
        updateUI();
    }

    /**
     * Reconstruye los botones del panel con los templates todavía disponibles.
     * Se llama al inicio y tras cada selección para reflejar el estado actual.
     */
    private void refreshRobotButtons() {
        availableRobotsPane.getChildren().clear();
        for (RobotTemplate template : availableTemplates) {
            // Obtenemos el nombre usando createRobot(null) sería incorrecto;
            // usamos el nombre del enum formateado
            String displayName = formatName(template.name());
            Button robotBtn = new Button(displayName);
            robotBtn.setPrefSize(120, 120);
            robotBtn.setOnAction(e -> onTemplateSelected(template));
            availableRobotsPane.getChildren().add(robotBtn);
        }
    }

    /** Convierte "VICTORY_SABER" -> "Victory Saber" */
    private String formatName(String enumName) {
        String[] parts = enumName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty())
                sb.append(" ");
            sb.append(part.charAt(0)).append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    @FXML
    public void handleAcceptAction() {
        System.out.println("Aceptando selección, yendo a GameScreen...");
        this.game.displayScreen(new GameScreen(this.game));
    }

    // Método para inyectar los datos iniciales
    public void initializeData(List<RobotTemplate> allTemplates) {
        this.availableTemplates = new ArrayList<>(allTemplates);
        this.player1Templates = new ArrayList<>();
        this.player2Templates = new ArrayList<>();
        this.isPlayer2Turn = true;
    }

    // Acción ejecutada desde la UI al seleccionar un template de robot
    public void onTemplateSelected(RobotTemplate selected) {
        if (!availableTemplates.contains(selected)) {
            return;
        }

        if (isPlayer2Turn) {
            player2Templates.add(selected);
        } else {
            player1Templates.add(selected);
        }

        availableTemplates.remove(selected);

        if (isSelectionComplete()) {
            transitionToCombatScreen();
        } else {
            isPlayer2Turn = !isPlayer2Turn;
            refreshRobotButtons();
            updateUI();
        }
    }

    private boolean isSelectionComplete() {
        return player1Templates.size() == MAX_ROBOTS_PER_PLAYER &&
                player2Templates.size() == MAX_ROBOTS_PER_PLAYER;
    }

    private void updateUI() {
        if (fixedInstructionsLabel != null) {
            fixedInstructionsLabel.setText(
                    "Los jugadores eligen robots por turnos. Empieza J2. J1 realizará el primer turno de la partida");
        }
        if (turnInstructionsLabel != null) {
            String text = isPlayer2Turn ? "Jugador 2, elige robot" : "Jugador 1, elige robot";
            turnInstructionsLabel.setText(text);
        }
    }

    @Override
    public void display() {
        System.out.println("Mostrando RobotSelectionScreen...");
        this.stage.setScene(this.scene);
    }

    @Override
    public void handleInput(String input) {
        System.out.println("Procesando entrada en RobotSelectionScreen: " + input);
    }

    private void transitionToCombatScreen() {
        System.out.println("Transición a la pantalla de combate...");
    }
}