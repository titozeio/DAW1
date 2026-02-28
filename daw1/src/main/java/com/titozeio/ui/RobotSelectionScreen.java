package com.titozeio.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

    // Estado interno
    private List<Robot> availableRobots;
    private List<Robot> player1Robots;
    private List<Robot> player2Robots;
    private boolean isPlayer2Turn;

    private Stage stage;
    private Game game;
    private Scene scene;

    public RobotSelectionScreen(Stage window, Game game) {
        this.stage = window;
        this.game = game;
        setupUI();
    }

    private void setupUI() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Robot Selection Screen");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        fixedInstructionsLabel = new Label();
        turnInstructionsLabel = new Label();
        availableRobotsPane = new FlowPane();
        availableRobotsPane.setAlignment(Pos.CENTER);
        availableRobotsPane.setHgap(10);
        availableRobotsPane.setVgap(10);

        layout.getChildren().addAll(titleLabel, fixedInstructionsLabel, turnInstructionsLabel, availableRobotsPane);

        // Botón Aceptar para ir a la pantalla de juego
        Button acceptButton = new Button("Aceptar");
        acceptButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        acceptButton.setOnAction(e -> {
            System.out.println("Aceptando selección, yendo a GameScreen...");
            this.game.displayScreen(new GameScreen(this.game));
        });
        layout.getChildren().add(acceptButton);

        // Añadir algunos botones de prueba para representar robots
        for (int i = 1; i <= 6; i++) {
            Button robotBtn = new Button("Robot " + i);
            robotBtn.setPrefSize(100, 100);
            int finalI = i;
            robotBtn.setOnAction(e -> {
                System.out.println("Robot " + finalI + " seleccionado");
                // Aquí iría la lógica de selección real
            });
            availableRobotsPane.getChildren().add(robotBtn);
        }

        // Inicializar datos y actualizar texto inicial
        initializeData(new ArrayList<>()); // Empezamos con lista vacía por ahora
        updateUI();

        this.scene = new Scene(layout, 800, 600);
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
            if (isPlayer2Turn) {
                turnInstructionsLabel.setText("Jugador 2, elige robot");
            } else {
                turnInstructionsLabel.setText("Jugador 1, elige robot");
            }
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