package com.titozeio.ui;

import com.titozeio.engine.Game;
import com.titozeio.engine.GamePhase;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.MovementCalculator;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pantalla principal de combate.
 * Gestiona la fase de despliegue y la fase de combate.
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

    // ── Propiedades de pantalla ───────────────────────────────────────────────
    private Game game;
    private Stage window;
    private Scene scene;

    /** Renderizador del mapa hexagonal. */
    private HexRenderer hexRenderer;

    // ── Estado de la fase de despliegue ───────────────────────────────────────
    /** Jugador que está desplegando ahora. */
    private Player deployingPlayer;
    /** Cola de robots pendientes de posicionar para deployingPlayer. */
    private Queue<Robot> robotsToPlace;

    // ── Estado de la fase de combate ──────────────────────────────────────────
    /** Robot seleccionado actualmente por el jugador activo (null = ninguno). */
    private Robot selectedRobot;

    // ── Constructor vacío (requerido por FXMLLoader) ──────────────────────────
    public GameScreen() {
    }

    // ── Factory ───────────────────────────────────────────────────────────────
    /**
     * Carga la pantalla desde FXML y la inicializa con los datos del juego.
     */
    public static GameScreen create(Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    GameScreen.class.getResource("/com/titozeio/ui/GameScreen.fxml"));
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

    // ── Inicialización ────────────────────────────────────────────────────────

    private void initializeUI() {
        // Crear renderer y pintar el mapa
        hexRenderer = new HexRenderer(hexMapPane);
        hexRenderer.setOnHexClick(this::onHexClicked);
        hexRenderer.render(game.getMap());

        // Botones generales
        pauseButton.setOnAction(e -> System.out.println("Pausa activada."));
        endTurnButton.setDisable(true); // deshabilitado durante el despliegue

        overlayAcceptButton.setOnAction(e -> overlayPane.setVisible(false));

        // Arrancar fase de despliegue
        startDeployPhase();
    }

    // ── Fase de Despliegue ────────────────────────────────────────────────────

    /**
     * Inicia la fase de despliegue: J1 posiciona sus robots primero.
     */
    private void startDeployPhase() {
        game.setPhase(GamePhase.DEPLOYING);
        deployingPlayer = game.getP1();
        robotsToPlace = new LinkedList<>(game.getP1().getUnits());
        endTurnButton.setDisable(true);
        updateDeployHud();
        highlightDeployZones();
    }

    /**
     * Calcula y resalta las casillas de despliegue vacías del jugador activo.
     */
    private void highlightDeployZones() {
        boolean isP1 = (deployingPlayer == game.getP1());
        Set<Hexagon> zones = new HashSet<>();
        for (Hexagon[] row : game.getMap().getGrid()) {
            for (Hexagon hex : row) {
                if (hex == null || hex.isOccupied())
                    continue;
                if (isP1 && hex.isDeployZoneP1())
                    zones.add(hex);
                if (!isP1 && hex.isDeployZoneP2())
                    zones.add(hex);
            }
        }
        hexRenderer.setHighlightedHexes(zones);
        hexRenderer.render(game.getMap());
    }

    /**
     * Actualiza el HUD durante el despliegue: quién despliega y qué robot es el
     * siguiente.
     */
    private void updateDeployHud() {
        turnLabel.setText("DESPLIEGUE – " + deployingPlayer.getName().toUpperCase());

        if (robotsToPlace.isEmpty()) {
            objectiveShortLabel.setText("Todos los robots posicionados.");
            infoDetailsLabel.setText("");
            return;
        }

        Robot next = robotsToPlace.peek();
        objectiveShortLabel.setText(
                "Siguiente: " + next.getModelName()
                        + " – Clic en una zona verde para posicionarlo");

        String pending = robotsToPlace.stream()
                .map(Robot::getModelName)
                .collect(Collectors.joining("\n"));
        infoDetailsLabel.setText("Robots pendientes:\n" + pending);
    }

    /**
     * Maneja el clic en un hexágono durante la fase de DESPLIEGUE.
     */
    private void handleDeployClick(Hexagon hex) {
        boolean isP1 = (deployingPlayer == game.getP1());
        boolean validZone = isP1 ? hex.isDeployZoneP1() : hex.isDeployZoneP2();

        if (!validZone) {
            addLogMessage("Casilla fuera de la zona de despliegue de " + deployingPlayer.getName() + ".");
            return;
        }
        if (hex.isOccupied()) {
            addLogMessage("Casilla ya ocupada.");
            return;
        }

        // Colocar el siguiente robot en la casilla
        Robot robot = robotsToPlace.poll();
        if (robot == null)
            return;

        hex.setOccupant(robot);
        robot.setPosition(hex); // Robot ya tiene setPosition()

        addLogMessage(deployingPlayer.getName() + " despliega "
                + robot.getModelName() + " en ("
                + hex.getQ() + "," + hex.getR() + ").");

        updateDeployHud();
        highlightDeployZones();

        // Si ya no quedan robots para este jugador
        if (robotsToPlace.isEmpty()) {
            if (deployingPlayer == game.getP1()) {
                // Cambiar a J2
                deployingPlayer = game.getP2();
                robotsToPlace = new LinkedList<>(game.getP2().getUnits());
                updateDeployHud();
                highlightDeployZones();
                showOverlayMessage("TURNO DE DESPLIEGUE – J2",
                        "Jugador 2: posiciona tus robots en las zonas rojas.");
            } else {
                // Ambos jugadores han desplegado → combate
                startCombatPhase();
            }
        }
    }

    // ── Fase de Combate ───────────────────────────────────────────────────────

    private void startCombatPhase() {
        game.setPhase(GamePhase.COMBAT);
        selectedRobot = null;
        hexRenderer.setHighlightedHexes(new HashSet<>());
        hexRenderer.setMoveHighlightedHexes(new HashSet<>());
        hexRenderer.render(game.getMap());
        endTurnButton.setDisable(false);
        endTurnButton.setOnAction(e -> handleInput("F"));
        updateCombatHud();
        showOverlayMessage("¡COMBATE INICIADO!",
                "Turno 1 – " + game.getCurrentPlayer().getName()
                        + "\nElimina a todos los robots enemigos o conquista su base.");
        addLogMessage("¡Comienza el combate!");
    }

    private void updateCombatHud() {
        if (game.getCurrentPlayer() != null) {
            turnLabel.setText("TURNO " + game.getTurnCounter()
                    + " – " + game.getCurrentPlayer().getName().toUpperCase());
        }
        objectiveShortLabel.setText("Objetivo: Eliminar enemigos o conquistar su base.");
        if (selectedRobot == null) {
            infoDetailsLabel.setText("Selecciona un robot tuyo para actuar.");
        }
    }

    // ── Dispatcher de clics en hexágonos ─────────────────────────────────────

    private void onHexClicked(Hexagon hex) {
        System.out.println("[GameScreen] clic en hex (" + hex.getQ() + "," + hex.getR()
                + ") – fase: " + game.getPhase());
        if (game.getPhase() == GamePhase.DEPLOYING) {
            handleDeployClick(hex);
        } else {
            handleCombatClick(hex);
        }
    }

    /**
     * Maneja el clic en un hexágono durante la fase de COMBATE.
     *
     * Flujo:
     * 1. Robot seleccionado + clic en rango de movimiento → mueve el robot
     * 2. Clic en robot ya seleccionado → deselecciona
     * 3. Clic en robot propio → selecciona
     * 4. Clic en robot enemigo / celda vacía → muestra info / deselecciona
     */
    private void handleCombatClick(Hexagon hex) {
        Player currentPlayer = game.getCurrentPlayer();

        // 1. Mover el robot seleccionado a una casilla dentro del rango
        if (selectedRobot != null && hexRenderer.isMoveHighlighted(hex)) {
            selectedRobot.move(hex);
            addLogMessage(selectedRobot.getModelName() + " se mueve a ("
                    + hex.getQ() + "," + hex.getR() + ").");
            clearSelection();
            return;
        }

        // 2-4. Clic en casilla con o sin ocupante
        if (hex.isOccupied()) {
            Robot robot = hex.getOccupant();

            if (robot == selectedRobot) {
                clearSelection(); // Deseleccionar
                return;
            }
            if (robot.getOwner() == currentPlayer) {
                selectRobot(robot); // Robot propio → seleccionar
                return;
            }
            // Robot enemigo → mostrar info
            clearSelection();
            infoDetailsLabel.setText(robot.getModelName() + " [ENEMIGO]\nHP: "
                    + robot.getCurrentHp() + "/" + robot.getMaxHp());
        } else {
            // Celda vacía
            clearSelection();
            showTerrainInfo(hex);
        }
    }

    /** Selecciona el robot y resalta su rango de movimiento en azul. */
    private void selectRobot(Robot robot) {
        selectedRobot = robot;
        StringBuilder sb = new StringBuilder();
        sb.append(robot.getModelName()).append("\n");
        sb.append("HP: ").append(robot.getCurrentHp())
                .append("/").append(robot.getMaxHp()).append("\n");
        sb.append("Movimiento: ").append(robot.getMovementPoints()).append(" MP");
        if (robot.isUsedMovement())
            sb.append(" (agotado)");
        sb.append("\nArma: ").append(robot.getWeapon().getName())
                .append(" (Alcance ").append(robot.getWeapon().getRange())
                .append(", Daño ").append(robot.getWeapon().getDamage()).append(")");
        infoDetailsLabel.setText(sb.toString());

        if (!robot.isUsedMovement()) {
            Set<Hexagon> reachable = MovementCalculator.getReachable(robot, game.getMap());
            hexRenderer.setMoveHighlightedHexes(reachable);
            addLogMessage(robot.getModelName() + " seleccionado – "
                    + reachable.size() + " casilla(s) accesible(s).");
        } else {
            hexRenderer.setMoveHighlightedHexes(new HashSet<>());
            addLogMessage(robot.getModelName() + " seleccionado (movimiento agotado).");
        }
        hexRenderer.render(game.getMap());
    }

    /** Deselecciona el robot activo y limpia los resaltados de movimiento. */
    private void clearSelection() {
        selectedRobot = null;
        hexRenderer.setMoveHighlightedHexes(new HashSet<>());
        hexRenderer.render(game.getMap());
        infoDetailsLabel.setText("Selecciona un robot tuyo para actuar.");
    }

    /** Muestra info del terreno en el panel de información. */
    private void showTerrainInfo(Hexagon hex) {
        StringBuilder sb = new StringBuilder();
        sb.append("Casilla (").append(hex.getQ()).append(", ").append(hex.getR()).append(")\n");
        sb.append("Terreno: ").append(terrainName(hex)).append("\n");
        sb.append("Altura: ").append(hex.getHeight());
        if (hex.isBaseP1())
            sb.append("\n⚑ BASE JUGADOR 1");
        if (hex.isBaseP2())
            sb.append("\n⚑ BASE JUGADOR 2");
        infoDetailsLabel.setText(sb.toString());
    }

    private static String terrainName(Hexagon hex) {
        switch (hex.getTerrain()) {
            case VEGETATION:
                return "Vegetación";
            case WATER_SHALLOW:
                return "Agua poco profunda";
            case WATER_DEEP:
                return "Agua profunda";
            default:
                if (hex.getHeight() >= 2)
                    return "Meseta (h2)";
                if (hex.getHeight() == 1)
                    return "Colina (h1)";
                return "Normal";
        }
    }

    // ── Métodos públicos de HUD ───────────────────────────────────────────────

    public void showOverlayMessage(String title, String message) {
        overlayTitle.setText(title);
        overlayMessage.setText(message);
        overlayPane.setVisible(true);
    }

    public void addLogMessage(String message) {
        Label logLine = new Label("> " + message);
        logLine.setStyle("-fx-text-fill: #e1e1e1; -fx-font-family: 'Exo 2'; -fx-font-size: 12px;");
        combatLogContent.getChildren().add(logLine);
        logScrollPane.setVvalue(1.0);
    }

    // ── Screen ────────────────────────────────────────────────────────────────

    @Override
    public void display() {
        System.out.println("Mostrando GameScreen...");
        this.window.setScene(this.scene);
    }

    @Override
    public void handleInput(String input) {
        switch (input.trim().toUpperCase()) {
            case "F":
                if (game.getPhase() == GamePhase.COMBAT) {
                    addLogMessage("Fin de turno (" + game.getCurrentPlayer().getName() + ").");
                    game.nextTurn();
                    updateCombatHud();
                }
                break;
            default:
                System.out.println("Acción no reconocida: " + input);
        }
    }
}
