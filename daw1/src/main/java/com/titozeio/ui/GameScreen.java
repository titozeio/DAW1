package com.titozeio.ui;

import com.titozeio.engine.Game;
import com.titozeio.engine.GamePhase;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.MovementCalculator;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.SkillType;
import com.titozeio.skills.JetpackBoost;
import com.titozeio.skills.Skill;
import com.titozeio.skills.TurboPropulsion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
 * Gestiona despliegue, combate por turnos, uso de skills y condiciones de
 * victoria.
 */
public class GameScreen extends Screen {

    // ── Referencias FXML ──────────────────────────────────────────────────────
    @FXML
    private StackPane rootPane;
    @FXML
    private ImageView battlefieldBg;
    @FXML
    private StackPane mapContainer;
    @FXML
    private Pane hexMapPane;
    @FXML
    private Label turnLabel;
    @FXML
    private Label objectiveShortLabel;
    @FXML
    private Label infoDetailsLabel;
    @FXML
    private ScrollPane logScrollPane;
    @FXML
    private VBox combatLogContent;
    @FXML
    private Button skillButton;
    @FXML
    private Button endTurnButton;
    // Overlay genérico
    @FXML
    private StackPane overlayPane;
    @FXML
    private Label overlayTitle;
    @FXML
    private Label overlayMessage;
    @FXML
    private Button overlayAcceptButton;
    // Overlay de victoria
    @FXML
    private StackPane victoryPane;
    @FXML
    private Label victoryIcon;
    @FXML
    private Label victoryTitle;
    @FXML
    private Label victoryReason;
    @FXML
    private Label victoryTurnLabel;
    @FXML
    private Button victoryMenuButton;

    // ── Estado ────────────────────────────────────────────────────────────────
    private Game game;
    private Stage window;
    private Scene scene;
    private HexRenderer hexRenderer;

    // Despliegue
    private Player deployingPlayer;
    private Queue<Robot> robotsToPlace;

    // Combate
    private Robot selectedRobot;

    private enum SelectionMode {
        NONE, MOVE, ATTACK, SKILL
    }

    private SelectionMode selectionMode = SelectionMode.NONE;

    // ── Factory ───────────────────────────────────────────────────────────────
    public GameScreen() {
    }

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
        configureBackground();
        hexRenderer = new HexRenderer(hexMapPane);
        hexRenderer.setOnHexClick(this::onHexClicked);
        hexRenderer.render(game.getMap());

        endTurnButton.setDisable(true);
        skillButton.setDisable(true);
        overlayAcceptButton.setOnAction(e -> overlayPane.setVisible(false));
        victoryMenuButton.setOnAction(e -> returnToMenu());

        startDeployPhase();
    }

    private void configureBackground() {
        if (battlefieldBg == null) {
            return;
        }
        battlefieldBg.setImage(loadBackgroundImage());
    }

    private Image loadBackgroundImage() {
        String[] candidates = {
                "/com/titozeio/images/bg2.png",
                "/com/titozeio/images/bg2.jpg",
                "/com/titozeio/images/bg1.png"
        };
        for (String path : candidates) {
            var resource = GameScreen.class.getResource(path);
            if (resource != null) {
                return new Image(resource.toExternalForm());
            }
        }
        return null;
    }

    // ── Despliegue ────────────────────────────────────────────────────────────

    private void startDeployPhase() {
        game.setPhase(GamePhase.DEPLOYING);
        deployingPlayer = game.getP1();
        robotsToPlace = new LinkedList<>(game.getP1().getUnits());
        endTurnButton.setDisable(true);
        skillButton.setDisable(true);
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
        objectiveShortLabel.setText("Siguiente: " + next.getModelName() + " – Clic en zona verde");
        infoDetailsLabel.setText("Robots pendientes:\n"
                + robotsToPlace.stream().map(Robot::getModelName).collect(Collectors.joining("\n")));
    }

    private void handleDeployClick(Hexagon hex) {
        boolean isP1 = (deployingPlayer == game.getP1());
        boolean valid = isP1 ? hex.isDeployZoneP1() : hex.isDeployZoneP2();
        if (!valid) {
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
        addLogMessage(deployingPlayer.getName() + " despliega " + robot.getModelName()
                + " en (" + hex.getQ() + "," + hex.getR() + ").");
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

    // ── Combate ───────────────────────────────────────────────────────────────

    private void startCombatPhase() {
        game.setPhase(GamePhase.COMBAT);
        selectedRobot = null;
        selectionMode = SelectionMode.NONE;

        for (Hexagon[] row : game.getMap().getGrid()) {
            for (Hexagon hex : row) {
                if (hex == null)
                    continue;
                if (hex.isBaseP1())
                    game.getP1().setBaseLocation(hex);
                if (hex.isBaseP2())
                    game.getP2().setBaseLocation(hex);
            }
        }

        hexRenderer.setHighlightedHexes(new HashSet<>());
        clearAllHighlights();
        endTurnButton.setDisable(false);
        endTurnButton.setOnAction(e -> handleInput("F"));
        skillButton.setDisable(true);
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
        if (selectedRobot == null)
            infoDetailsLabel.setText("Selecciona un robot tuyo para actuar.");
    }

    // ── Dispatcher ────────────────────────────────────────────────────────────

    private void onHexClicked(Hexagon hex) {
        if (game.getPhase() == GamePhase.DEPLOYING) {
            handleDeployClick(hex);
            return;
        }
        handleCombatClick(hex);
    }

    private void handleCombatClick(Hexagon hex) {
        Player cur = game.getCurrentPlayer();

        // Ejecutar skill si estamos en modo SKILL y la casilla está resaltada
        if (selectionMode == SelectionMode.SKILL && hexRenderer.isSkillHighlighted(hex)) {
            executeSkill(hex);
            return;
        }
        // Ejecutar movimiento normal
        if (selectionMode == SelectionMode.MOVE && hexRenderer.isMoveHighlighted(hex)) {
            executeMove(hex);
            return;
        }
        // Ejecutar ataque normal
        if (selectionMode == SelectionMode.ATTACK && hexRenderer.isAttackHighlighted(hex)) {
            executeAttack(hex);
            return;
        }

        // Cick en casilla ocupada
        if (hex.isOccupied()) {
            Robot robot = hex.getOccupant();
            if (robot.getOwner() == cur) {
                if (robot == selectedRobot)
                    cycleModes();
                else
                    selectRobot(robot);
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
        updateSkillButton(robot);

        if (!robot.isUsedMovement()) {
            enterMoveMode();
        } else if (!robot.isUsedAttack()) {
            enterAttackMode();
        } else {
            selectionMode = SelectionMode.NONE;
            clearAllHighlights();
            addLogMessage(robot.getModelName() + " ya ha agotado sus acciones.");
        }
    }

    /**
     * Cicla modos: MOVE→ATTACK→SKILL→NONE (según acciones disponibles).
     */
    private void cycleModes() {
        switch (selectionMode) {
            case MOVE:
                if (!selectedRobot.isUsedAttack()) {
                    enterAttackMode();
                    return;
                }
                if (canUseSkill(selectedRobot)) {
                    enterSkillMode();
                    return;
                }
                clearSelection();
                break;
            case ATTACK:
                if (canUseSkill(selectedRobot)) {
                    enterSkillMode();
                    return;
                }
                clearSelection();
                break;
            case SKILL:
                clearSelection();
                break;
            default:
                clearSelection();
        }
    }

    private void enterMoveMode() {
        selectionMode = SelectionMode.MOVE;
        Set<Hexagon> reachable = MovementCalculator.getReachable(selectedRobot, game.getMap());
        hexRenderer.setMoveHighlightedHexes(reachable);
        hexRenderer.setAttackHighlightedHexes(new HashSet<>());
        hexRenderer.setSkillHighlightedHexes(new HashSet<>(), false);
        hexRenderer.render(game.getMap());
        addLogMessage(selectedRobot.getModelName() + " – MOVER (" + reachable.size()
                + " casillas). Clic de nuevo → modo ataque.");
    }

    private void enterAttackMode() {
        selectionMode = SelectionMode.ATTACK;
        Set<Hexagon> targets = getAttackableHexes(selectedRobot);
        hexRenderer.setMoveHighlightedHexes(new HashSet<>());
        hexRenderer.setAttackHighlightedHexes(targets);
        hexRenderer.setSkillHighlightedHexes(new HashSet<>(), false);
        hexRenderer.render(game.getMap());
        addLogMessage(targets.isEmpty()
                ? selectedRobot.getModelName() + " – ATACAR. Sin objetivos en rango."
                : selectedRobot.getModelName() + " – ATACAR. " + targets.size()
                        + " objetivo(s) (naranja). Clic de nuevo → modo skill.");
    }

    /**
     * Entra en modo SKILL: resalta casillas/robots válidos para la habilidad del
     * robot.
     * - Skills MOVEMENT (JetpackBoost, TurboPropulsion) → casillas vacías en rango
     * (lima)
     * - Skills ATTACK (FreezeRay, GuidedMissile, RepairNanobots, PlasmaSaberAttack)
     * → objetivos válidos (violeta)
     */
    private void enterSkillMode() {
        Skill skill = selectedRobot.getSkill();
        if (!canUseSkill(selectedRobot)) {
            clearSelection();
            return;
        }

        selectionMode = SelectionMode.SKILL;
        boolean isAttackSkill = (skill.getType() == SkillType.ATTACK);

        Set<Hexagon> targets = isAttackSkill
                ? getSkillAttackTargets(selectedRobot)
                : getSkillMovementTargets(selectedRobot);

        hexRenderer.setMoveHighlightedHexes(new HashSet<>());
        hexRenderer.setAttackHighlightedHexes(new HashSet<>());
        hexRenderer.setSkillHighlightedHexes(targets, isAttackSkill);
        hexRenderer.render(game.getMap());

        String colorHint = isAttackSkill ? "violeta" : "lima";
        addLogMessage("⚡ " + skill.getName() + " activada. "
                + targets.size() + " objetivo(s) " + colorHint + ".");
        if (targets.isEmpty())
            addLogMessage("Sin objetivos en rango para la skill.");
    }

    // ── Ejecución de acciones ─────────────────────────────────────────────────

    private void executeMove(Hexagon hex) {
        String name = selectedRobot.getModelName();
        selectedRobot.move(hex);
        addLogMessage(name + " se mueve a (" + hex.getQ() + "," + hex.getR() + ").");
        if (!selectedRobot.isUsedAttack())
            enterAttackMode();
        else
            clearSelection();
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

        addLogMessage(damage > 0
                ? "💥 " + selectedRobot.getModelName() + " → " + target.getModelName()
                        + ": " + damage + " daño. HP: " + target.getCurrentHp() + "/" + target.getMaxHp()
                : selectedRobot.getModelName() + " ataca a " + target.getModelName() + " sin causar daño.");

        if (target.isDestroyed()) {
            targetHex.setOccupant(null);
            addLogMessage("☠ " + target.getModelName() + " ha sido DESTRUIDO.");
        }
        clearSelection();
        checkAndShowVictory("eliminación");
    }

    /**
     * Ejecuta la skill del robot seleccionado sobre la casilla/robot indicado.
     * - Skills MOVEMENT: target = Hexagon destino
     * - Skills ATTACK: target = Robot objetivo (obtenido del ocupante del hex)
     */
    private void executeSkill(Hexagon hex) {
        Skill skill = selectedRobot.getSkill();
        if (skill == null || !skill.isReady()) {
            clearSelection();
            return;
        }

        boolean isAttackSkill = (skill.getType() == SkillType.ATTACK);
        String skillName = skill.getName();

        if (isAttackSkill) {
            // El hex debe tener un ocupante (robot objetivo)
            Robot target = hex.getOccupant();
            if (target == null) {
                clearSelection();
                return;
            }

            int hpBefore = target.getCurrentHp();
            selectedRobot.useSkill(target); // skill.execute() se encarga de setUsedAttack
            int damage = hpBefore - target.getCurrentHp();

            if (damage > 0) {
                addLogMessage("⚡ " + skillName + ": " + selectedRobot.getModelName()
                        + " → " + target.getModelName() + ": " + damage + " daño. HP: "
                        + target.getCurrentHp() + "/" + target.getMaxHp());
            } else {
                // Puede ser curación (RepairNanobots) → HP del aliado puede haber subido
                addLogMessage("⚡ " + skillName + " aplicada sobre " + target.getModelName()
                        + ". HP: " + target.getCurrentHp() + "/" + target.getMaxHp());
            }

            if (target.isDestroyed()) {
                hex.setOccupant(null);
                addLogMessage("☠ " + target.getModelName() + " ha sido DESTRUIDO.");
            }
        } else {
            // Skill de movimiento: target = Hexagon
            selectedRobot.useSkill(hex); // skill.execute() mueve al robot
            addLogMessage("⚡ " + skillName + ": " + selectedRobot.getModelName()
                    + " se teletransporta a (" + hex.getQ() + "," + hex.getR() + ").");
        }

        clearSelection();
        checkAndShowVictory("eliminación");
    }

    // ── Cálculo de objetivos de skill ─────────────────────────────────────────

    /**
     * Para skills de MOVIMIENTO: casillas vacías a las que el robot puede saltar
     * usando el radio especial de la skill (JetpackBoost=3, TurboPropulsion=7).
     * Las skills de movimiento ignoran restricciones normales de terreno/altura.
     */
    private Set<Hexagon> getSkillMovementTargets(Robot robot) {
        Skill skill = robot.getSkill();
        int skillRange = 3; // por defecto
        if (skill instanceof JetpackBoost)
            skillRange = ((JetpackBoost) skill).getMovementPoints();
        else if (skill instanceof TurboPropulsion)
            skillRange = ((TurboPropulsion) skill).getMovementPoints();

        Set<Hexagon> results = new HashSet<>();
        Hexagon origin = robot.getPosition();
        if (origin == null)
            return results;

        for (Hexagon[] row : game.getMap().getGrid()) {
            for (Hexagon hex : row) {
                if (hex == null || hex.isOccupied() || hex == origin)
                    continue;
                int dist = game.getMap().getDistance(origin, hex);
                if (dist <= skillRange)
                    results.add(hex);
            }
        }
        return results;
    }

    /**
     * Para skills de ATAQUE: robots en rango de la skill.
     * - RepairNanobots (alcance 1) → robots ALIADOS en rango 1
     * - FreezeRay (alcance 3) / GuidedMissile (alcance 5) → robots enemigos
     * (GuidedMissile ignora visibilidad)
     * - PlasmaSaberAttack → robots adyacentes enemigos
     */
    private Set<Hexagon> getSkillAttackTargets(Robot robot) {
        Skill skill = robot.getSkill();
        Set<Hexagon> targets = new HashSet<>();
        Hexagon origin = robot.getPosition();
        if (origin == null || skill == null)
            return targets;

        // Determinar parámetros de la skill
        String skillClass = skill.getClass().getSimpleName();
        int skillRange;
        boolean targetsAllies;
        boolean ignoresVisibility;

        switch (skillClass) {
            case "RepairNanobots":
                skillRange = 1;
                targetsAllies = true;
                ignoresVisibility = false;
                break;
            case "FreezeRay":
                skillRange = 3;
                targetsAllies = false;
                ignoresVisibility = false;
                break;
            case "GuidedMissile":
                skillRange = 5;
                targetsAllies = false;
                ignoresVisibility = true;
                break;
            case "PlasmaSaberAttack":
                skillRange = 1;
                targetsAllies = false;
                ignoresVisibility = false;
                break;
            default:
                skillRange = 3;
                targetsAllies = false;
                ignoresVisibility = false;
        }

        for (Hexagon[] row : game.getMap().getGrid()) {
            for (Hexagon hex : row) {
                if (hex == null || !hex.isOccupied())
                    continue;
                Robot occ = hex.getOccupant();

                boolean correctTeam = targetsAllies
                        ? occ.getOwner() == robot.getOwner() && occ != robot
                        : occ.getOwner() != robot.getOwner();
                if (!correctTeam)
                    continue;

                int dist = game.getMap().getDistance(origin, hex);
                if (dist > skillRange)
                    continue;

                if (!ignoresVisibility && !game.getMap().hasVisibility(origin, hex))
                    continue;
                if (occ.isDestroyed())
                    continue;

                targets.add(hex);
            }
        }
        return targets;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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

    private boolean canUseSkill(Robot robot) {
        return robot != null && robot.getSkill() != null
                && robot.getSkill().isReady()
                && !(robot.isUsedMovement() && robot.getSkill().getType() == SkillType.MOVEMENT)
                && !(robot.isUsedAttack() && robot.getSkill().getType() == SkillType.ATTACK);
    }

    /** Actualiza el texto y estado del botón SKILL según el robot seleccionado. */
    private void updateSkillButton(Robot robot) {
        if (robot == null || robot.getSkill() == null) {
            skillButton.setText("SKILL");
            skillButton.setDisable(true);
            return;
        }
        Skill sk = robot.getSkill();
        if (!sk.isReady()) {
            skillButton.setText("SKILL: " + sk.getName() + " (CD:" + sk.getCurrentCooldown() + ")");
            skillButton.setDisable(true);
        } else if (!canUseSkill(robot)) {
            skillButton.setText("SKILL: " + sk.getName() + " (agotada)");
            skillButton.setDisable(true);
        } else {
            skillButton.setText("⚡ " + sk.getName());
            skillButton.setDisable(false);
            skillButton.setOnAction(e -> enterSkillMode());
        }
    }

    // ── Paneles de información ────────────────────────────────────────────────

    private void updateRobotInfoPanel(Robot robot) {
        Skill sk = robot.getSkill();
        StringBuilder sb = new StringBuilder();
        sb.append(robot.getModelName()).append("\n");
        sb.append("HP: ").append(robot.getCurrentHp()).append("/").append(robot.getMaxHp()).append("\n");
        sb.append("Mov: ").append(robot.getMovementPoints()).append(" MP");
        if (robot.isUsedMovement())
            sb.append(" ✓");
        sb.append("\nArma: ").append(robot.getWeapon().getName())
                .append(" (Alc ").append(robot.getWeapon().getRange())
                .append(", Daño ").append(robot.getWeapon().getDamage()).append(")");
        if (robot.isUsedAttack())
            sb.append(" ✓");
        if (sk != null) {
            sb.append("\nSkill: ").append(sk.getName());
            sb.append(sk.isReady() ? " ✔" : " (CD:" + sk.getCurrentCooldown() + ")");
        }
        infoDetailsLabel.setText(sb.toString());
    }

    private void clearAllHighlights() {
        hexRenderer.setMoveHighlightedHexes(new HashSet<>());
        hexRenderer.setAttackHighlightedHexes(new HashSet<>());
        hexRenderer.setSkillHighlightedHexes(new HashSet<>(), false);
        hexRenderer.render(game.getMap());
    }

    private void clearSelection() {
        selectedRobot = null;
        selectionMode = SelectionMode.NONE;
        skillButton.setText("SKILL");
        skillButton.setDisable(true);
        clearAllHighlights();
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

    // ── Victoria ─────────────────────────────────────────────────────────────

    private void checkAndShowVictory(String reason) {
        Player winner = game.checkVictory();
        if (winner != null)
            showVictory(winner, reason);
    }

    private void showVictory(Player winner, String reason) {
        boolean isP1 = (winner == game.getP1());
        boolean byElimination = reason.equals("eliminación");
        victoryIcon.setText(byElimination ? "🏆" : "🏴");
        victoryTitle.setText("¡" + winner.getName().toUpperCase() + " GANA!");
        victoryTitle.setStyle("-fx-font-size: 32px; -fx-text-fill: "
                + (isP1 ? "#5599ff" : "#ff5555") + ";");
        victoryReason.setText(byElimination
                ? winner.getName() + " ha eliminado a todos los robots rivales."
                : winner.getName() + " ha conquistado la base enemiga durante 2 turnos.");
        victoryTurnLabel.setText("Partida resuelta en el turno " + game.getTurnCounter());
        victoryPane.setVisible(true);
        endTurnButton.setDisable(true);
        skillButton.setDisable(true);
        addLogMessage("🏆 ¡" + winner.getName() + " gana por " + reason + "!");
    }

    private void returnToMenu() {
        MainMenuScreen menu = MainMenuScreen.create(game.getStage(), game);
        if (menu != null)
            game.displayScreen(menu);
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

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
                    for (Robot r : game.getCurrentPlayer().getUnits()) {
                        if (!r.isDestroyed())
                            r.resetActions();
                    }
                    updateCombatHud();
                    checkAndShowVictory("conquista de base");
                }
                break;
            default:
                System.out.println("Acción no reconocida: " + input);
        }
    }
}
