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
 * Gestiona la fase de despliegue y la fase de combate por turnos.
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
    private Player deployingPlayer;
    private Queue<Robot> robotsToPlace;

    // ── Estado de la fase de combate ──────────────────────────────────────────
    private Robot selectedRobot;

    /** Modos de selección durante el combate. */
    private enum SelectionMode {
        NONE, MOVE, ATTACK
    }

    private SelectionMode selectionMode = SelectionMode.NONE;

    // ── Constructor vacío (requerido por FXMLLoader) ──────────────────────────
    public GameScreen() {
    }

    // ── Factory ───────────────────────────────────────────────────────────────
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
        hexRenderer = new HexRenderer(hexMapPane);
        hexRenderer.setOnHexClick(this::onHexClicked);
        hexRenderer.render(game.getMap());

        pauseButton.setOnAction(e -> System.out.println("Pausa activada."));
        endTurnButton.setDisable(true);
        overlayAcceptButton.setOnAction(e -> overlayPane.setVisible(false));

        startDeployPhase();
    }

    // ── Fase de Despliegue ────────────────────────────────────────────────────

    private void startDeployPhase() {
        game.setPhase(GamePhase.DEPLOYING);
        deployingPlayer = game.getP1();
        robotsToPlace = new LinkedList<>(game.getP1().getUnits());
        endTurnButton.setDisable(true);
        updateDeployHud();
        highlightDeployZones();
    }

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

    private void updateDeployHud() {
        turnLabel.setText("DESPLIEGUE – " + deployingPlayer.getName().toUpperCase());
        if (robotsToPlace.isEmpty()) {
            objectiveShortLabel.setText("Todos los robots posicionados.");
            infoDetailsLabel.setText("");
            return;
        }
        Robot next = robotsToPlace.peek();
        objectiveShortLabel.setText("Siguiente: " + next.getModelName()
                + " – Clic en zona verde");
        String pending = robotsToPlace.stream()
                .map(Robot::getModelName).collect(Collectors.joining("\n"));
        infoDetailsLabel.setText("Robots pendientes:\n" + pending);
    }

    private void handleDeployClick(Hexagon hex) {
        boolean isP1 = (deployingPlayer == game.getP1());
        boolean validZone = isP1 ? hex.isDeployZoneP1() : hex.isDeployZoneP2();
        if (!validZone) {
            addLogMessage("Casilla fuera de la zona de despliegue.");
            return;
        }
        if (hex.isOccupied()) {
            addLogMessage("Casilla ya ocupada.");
            return;
        }
        Robot robot = robotsToPlace.poll();
        if (robot == null)
            return;

        hex.setOccupant(robot);
        robot.setPosition(hex);
        addLogMessage(deployingPlayer.getName() + " despliega "
                + robot.getModelName() + " en ("
                + hex.getQ() + "," + hex.getR() + ").");
        updateDeployHud();
        highlightDeployZones();

        if (robotsToPlace.isEmpty()) {
            if (deployingPlayer == game.getP1()) {
                deployingPlayer = game.getP2();
                robotsToPlace = new LinkedList<>(game.getP2().getUnits());
                updateDeployHud();
                highlightDeployZones();
                showOverlayMessage("TURNO DE DESPLIEGUE – J2",
                        "Jugador 2: posiciona tus robots en las zonas rojas.");
            } else {
                startCombatPhase();
            }
        }
    }

    // ── Fase de Combate ───────────────────────────────────────────────────────

    private void startCombatPhase() {
        game.setPhase(GamePhase.COMBAT);
        selectedRobot = null;
        selectionMode = SelectionMode.NONE;
        hexRenderer.setHighlightedHexes(new HashSet<>());
        hexRenderer.setMoveHighlightedHexes(new HashSet<>());
        hexRenderer.setAttackHighlightedHexes(new HashSet<>());
        hexRenderer.render(game.getMap());
        endTurnButton.setDisable(false);
        endTurnButton.setOnAction(e -> handleInput("F"));
        updateCombatHud();
        showOverlayMessage("¡COMBATE INICIADO!",
                "Turno 1 – " + game.getCurrentPlayer().getName()
                        + "\nElimina todos los robots enemigos o conquista su base.");
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

    // ── Dispatcher de clics ───────────────────────────────────────────────────

    private void onHexClicked(Hexagon hex) {
        if (game.getPhase() == GamePhase.DEPLOYING) {
            handleDeployClick(hex);
        } else {
            handleCombatClick(hex);
        }
    }

    /**
     * Máquina de estados de combate:
     * MOVE + clic en casilla azul → moveMove()
     * ATTACK + clic en casilla naranja → executeAttack()
     * Clic en robot propio → seleccionar / ciclar modo
     * Clic en enemigo / vacío → info / deseleccionar
     */
    private void handleCombatClick(Hexagon hex) {
        Player cur = game.getCurrentPlayer();

        if (selectionMode == SelectionMode.MOVE && hexRenderer.isMoveHighlighted(hex)) {
            executeMove(hex);
            return;
        }
        if (selectionMode == SelectionMode.ATTACK && hexRenderer.isAttackHighlighted(hex)) {
            executeAttack(hex);
            return;
        }

        if (hex.isOccupied()) {
            Robot robot = hex.getOccupant();
            if (robot.getOwner() == cur) {
                if (robot == selectedRobot) {
                    cycleModes();
                } else {
                    selectRobot(robot);
                }
            } else {
                clearSelection();
                infoDetailsLabel.setText(robot.getModelName() + " [ENEMIGO]\nHP: "
                        + robot.getCurrentHp() + "/" + robot.getMaxHp()
                        + "\nArma: " + robot.getWeapon().getName()
                        + " (Alc " + robot.getWeapon().getRange() + ")");
            }
            return;
        }

        clearSelection();
        showTerrainInfo(hex);
    }

    // ── Selección y modos ─────────────────────────────────────────────────────

    private void selectRobot(Robot robot) {
        selectedRobot = robot;
        updateRobotInfoPanel(robot);
        if (!robot.isUsedMovement()) {
            enterMoveMode();
        } else if (!robot.isUsedAttack()) {
            enterAttackMode();
        } else {
            selectionMode = SelectionMode.NONE;
            hexRenderer.setMoveHighlightedHexes(new HashSet<>());
            hexRenderer.setAttackHighlightedHexes(new HashSet<>());
            hexRenderer.render(game.getMap());
            addLogMessage(robot.getModelName() + " ya ha agotado sus acciones.");
        }
    }

    private void cycleModes() {
        if (selectionMode == SelectionMode.MOVE && !selectedRobot.isUsedAttack()) {
            enterAttackMode();
        } else {
            clearSelection();
        }
    }

    private void enterMoveMode() {
        selectionMode = SelectionMode.MOVE;
        Set<Hexagon> reachable = MovementCalculator.getReachable(selectedRobot, game.getMap());
        hexRenderer.setMoveHighlightedHexes(reachable);
        hexRenderer.setAttackHighlightedHexes(new HashSet<>());
        hexRenderer.render(game.getMap());
        addLogMessage(selectedRobot.getModelName() + " – MOVER ("
                + reachable.size() + " casillas). Clic de nuevo → modo ataque.");
    }

    private void enterAttackMode() {
        selectionMode = SelectionMode.ATTACK;
        Set<Hexagon> targets = getAttackableHexes(selectedRobot);
        hexRenderer.setMoveHighlightedHexes(new HashSet<>());
        hexRenderer.setAttackHighlightedHexes(targets);
        hexRenderer.render(game.getMap());
        if (targets.isEmpty()) {
            addLogMessage(selectedRobot.getModelName() + " – ATACAR. Sin objetivos en rango.");
        } else {
            addLogMessage(selectedRobot.getModelName() + " – ATACAR. "
                    + targets.size() + " objetivo(s) en rango (naranja). Clic para atacar.");
        }
    }

    // ── Ejecución de acciones ─────────────────────────────────────────────────

    private void executeMove(Hexagon hex) {
        String name = selectedRobot.getModelName();
        selectedRobot.move(hex);
        addLogMessage(name + " se mueve a (" + hex.getQ() + "," + hex.getR() + ").");
        if (!selectedRobot.isUsedAttack()) {
            enterAttackMode();
        } else {
            clearSelection();
        }
    }

    private void executeAttack(Hexagon targetHex) {
        Robot target = targetHex.getOccupant();
        if (target == null) {
            clearSelection();
            return;
        }

        int hpBefore = target.getCurrentHp();
        selectedRobot.attack(target, game.getMap());
        int damage = hpBefore - target.getCurrentHp();

        if (damage > 0) {
            addLogMessage("💥 " + selectedRobot.getModelName()
                    + " → " + target.getModelName()
                    + ": " + damage + " daño. HP: "
                    + target.getCurrentHp() + "/" + target.getMaxHp());
        } else {
            addLogMessage(selectedRobot.getModelName()
                    + " ataca a " + target.getModelName() + " pero no causa daño.");
        }

        if (target.isDestroyed()) {
            targetHex.setOccupant(null);
            addLogMessage("☠ " + target.getModelName() + " ha sido DESTRUIDO.");
        }
        clearSelection();
    }

    private Set<Hexagon> getAttackableHexes(Robot robot) {
        Set<Hexagon> targets = new HashSet<>();
        for (Hexagon[] row : game.getMap().getGrid()) {
            for (Hexagon hex : row) {
                if (hex == null || !hex.isOccupied())
                    continue;
                Robot occ = hex.getOccupant();
                if (occ.getOwner() == robot.getOwner())
                    continue;
                if (robot.canAttack(occ, game.getMap()))
                    targets.add(hex);
            }
        }
        return targets;
    }

    // ── Paneles de información ────────────────────────────────────────────────

    private void updateRobotInfoPanel(Robot robot) {
        StringBuilder sb = new StringBuilder();
        sb.append(robot.getModelName()).append("\n");
        sb.append("HP: ").append(robot.getCurrentHp())
                .append("/").append(robot.getMaxHp()).append("\n");
        sb.append("Mov: ").append(robot.getMovementPoints()).append(" MP");
        if (robot.isUsedMovement())
            sb.append(" ✓");
        sb.append("\nArma: ").append(robot.getWeapon().getName())
                .append(" (Alc ").append(robot.getWeapon().getRange())
                .append(", Daño ").append(robot.getWeapon().getDamage()).append(")");
        if (robot.isUsedAttack())
            sb.append(" ✓");
        infoDetailsLabel.setText(sb.toString());
    }

    private void clearSelection() {
        selectedRobot = null;
        selectionMode = SelectionMode.NONE;
        hexRenderer.setMoveHighlightedHexes(new HashSet<>());
        hexRenderer.setAttackHighlightedHexes(new HashSet<>());
        hexRenderer.render(game.getMap());
        infoDetailsLabel.setText("Selecciona un robot tuyo para actuar.");
    }

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
                    clearSelection();
                    game.nextTurn();
                    // Resetear acciones de los robots del nuevo jugador activo
                    for (Robot r : game.getCurrentPlayer().getUnits()) {
                        if (!r.isDestroyed())
                            r.resetActions();
                    }
                    updateCombatHud();
                }
                break;
            default:
                System.out.println("Acción no reconocida: " + input);
        }
    }
}
