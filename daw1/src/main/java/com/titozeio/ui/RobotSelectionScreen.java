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
import java.util.List;

import com.titozeio.engine.Game;
import com.titozeio.engine.Robot;

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
    private List<Robot> availableRobots;
    private List<Robot> player1Robots;
    private List<Robot> player2Robots;
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

            // Inicializar datos y UI
            controller.initializeData(new ArrayList<>()); // Podrías pasar los robots aquí si los tienes
            controller.setupInitialUI();

            return controller;
        } catch (IOException e) {
            System.err.println("Error al cargar RobotSelectionScreen.fxml: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void setupInitialUI() {
        // Añadir algunos botones de prueba si el panel está vacío
        if (availableRobotsPane.getChildren().isEmpty()) {
            for (int i = 1; i <= 6; i++) {
                Button robotBtn = new Button("Robot " + i);
                robotBtn.setPrefSize(100, 100);
                int finalI = i;
                robotBtn.setOnAction(e -> {
                    System.out.println("Robot " + finalI + " seleccionado");
                });
                availableRobotsPane.getChildren().add(robotBtn);
            }
        }
        updateUI();
    }

    @FXML
    public void handleAcceptAction() {
        System.out.println("Aceptando selección, yendo a GameScreen...");
        this.game.displayScreen(new GameScreen(this.game));
    }

    // Método para inyectar los datos iniciales
    public void initializeData(List<Robot> allRobots) {
        this.availableRobots = new ArrayList<>(allRobots);
        this.player1Robots = new ArrayList<>();
        this.player2Robots = new ArrayList<>();
        this.isPlayer2Turn = true;
    }

    // Acción ejecutada desde la UI al seleccionar un robot
    public void onRobotSelected(Robot selectedRobot) {
        if (!availableRobots.contains(selectedRobot)) {
            return;
        }

        if (isPlayer2Turn) {
            player2Robots.add(selectedRobot);
        } else {
            player1Robots.add(selectedRobot);
        }

        availableRobots.remove(selectedRobot);

        if (isSelectionComplete()) {
            transitionToCombatScreen();
        } else {
            isPlayer2Turn = !isPlayer2Turn;
            updateUI();
        }
    }

    private boolean isSelectionComplete() {
        return player1Robots.size() == MAX_ROBOTS_PER_PLAYER &&
                player2Robots.size() == MAX_ROBOTS_PER_PLAYER;
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