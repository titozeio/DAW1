package com.titozeio.ui;

import com.titozeio.engine.Game;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Pantalla principal de combate.
 * Muestra el estado del juego: turno actual, robots, mapa y mensajes.
 * Carga su diseño desde GameScreen.fxml.
 */
public class GameScreen extends Screen {

    // ── Referencias FXML ──────────────────────────────────────────────────────
    @FXML
    private StackPane rootPane;
    @FXML
    private StackPane mapContainer;
    @FXML
    private Pane hexMapPane;
    @FXML
    private Label turnLabel;
    @FXML
    private Label objectiveShortLabel;
    @FXML
    private Button pauseButton;
    @FXML
    private Label infoDetailsLabel;
    @FXML
    private ScrollPane logScrollPane;
    @FXML
    private VBox combatLogContent;
    @FXML
    private Button endTurnButton;
    @FXML
    private StackPane overlayPane;
    @FXML
    private Label overlayTitle;
    @FXML
    private Label overlayMessage;
    @FXML
    private Button overlayAcceptButton;

    // ── Propiedades de Pantalla ───────────────────────────────────────────────
    private Game game;
    private Stage window;
    private Scene scene;

    /**
     * Constructor vacío requerido por FXMLLoader.
     */
    public GameScreen() {
    }

    /**
     * Método de fábrica para instanciar y cargar la pantalla desde FXML.
     */
    public static GameScreen create(Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(GameScreen.class.getResource("/com/titozeio/ui/GameScreen.fxml"));
            Parent root = loader.load();
            GameScreen ctrl = loader.getController();

            ctrl.game = game;
            ctrl.window = game.getStage();
            ctrl.scene = new Scene(root, 1280, 720);
            ctrl.applyGlobalStyle(ctrl.scene);

            ctrl.initializeUI();

            return ctrl;
        } catch (IOException e) {
            System.err.println("Error al cargar GameScreen.fxml: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Inicializa componentes después de cargar el FXML.
     */
    private void initializeUI() {
        // Configuración inicial de textos
        updateTurnInfo();

        // Acciones básicas
        pauseButton.setOnAction(e -> {
            System.out.println("Pausa activada.");
            // TODO: Mostrar pantalla de pausa (PauseScreen)
        });

        endTurnButton.setOnAction(e -> {
            handleInput("F");
        });

        overlayAcceptButton.setOnAction(e -> {
            overlayPane.setVisible(false);
        });

        // Simular primer mensaje (Objetivos)
        showOverlayMessage("OBJETIVOS DEL MAPA",
                "1. Eliminar a todos los robots enemigos.\n2. Conquistar la base rival permaneciendo en ella 2 turnos.");
    }

    /**
     * Actualiza la etiqueta de turno con los datos del juego.
     */
    public void updateTurnInfo() {
        if (turnLabel != null && game != null) {
            String pName = game.getCurrentPlayer() != null ? game.getCurrentPlayer().getName() : "Sin Jugador";
            turnLabel.setText("TURNO " + game.getTurnCounter() + " - " + pName.toUpperCase());
        }
    }

    /**
     * Muestra el panel de mensajes modal.
     */
    public void showOverlayMessage(String title, String message) {
        overlayTitle.setText(title);
        overlayMessage.setText(message);
        overlayPane.setVisible(true);
    }

    /**
     * Añade una línea al registro de combate.
     */
    public void addLogMessage(String message) {
        Label logLine = new Label("> " + message);
        logLine.setStyle("-fx-text-fill: #e1e1e1; -fx-font-family: 'Exo 2'; -fx-font-size: 12px;");
        combatLogContent.getChildren().add(logLine);

        // Auto-scroll al final
        logScrollPane.setVvalue(1.0);
    }

    @Override
    public void display() {
        System.out.println("Mostrando GameScreen...");
        this.window.setScene(this.scene);
        updateTurnInfo();
    }

    @Override
    public void handleInput(String input) {
        switch (input.trim().toUpperCase()) {
            case "M":
                addLogMessage("Modo: Selecciona un robot para mover.");
                break;
            case "A":
                addLogMessage("Modo: Selecciona un robot para atacar.");
                break;
            case "S":
                addLogMessage("Modo: Selecciona un robot para usar su skill.");
                break;
            case "F":
                addLogMessage("Fin de turno (" + game.getCurrentPlayer().getName() + ").");
                game.nextTurn();
                updateTurnInfo();
                break;
            default:
                System.out.println("Acción no reconocida.");
        }
    }
}
