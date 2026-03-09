package com.titozeio.ui;

import com.titozeio.engine.Game;
import com.titozeio.engine.GamePhase;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.MovementCalculator;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.engine.SoundManager;
import com.titozeio.enums.AttackType;
import com.titozeio.enums.SkillType;
import com.titozeio.skills.JetpackBoost;
import com.titozeio.skills.Skill;
import com.titozeio.skills.TurboPropulsion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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
    private VBox infoPanel;
    @FXML
    private Label infoTitleLabel;
    @FXML
    private Label ownerBadgeLabel;
    @FXML
    private ImageView infoPortraitImage;
    @FXML
    private StackPane hpBarContainer;
    @FXML
    private Label hpValueLabel;
    @FXML
    private Label moveValueLabel;
    @FXML
    private Label rangeValueLabel;
    @FXML
    private Label damageValueLabel;
    @FXML
    private Label skillNameInfoLabel;
    @FXML
    private ImageView skillInfoIconImageView;
    @FXML
    private HBox skillCooldownDots;
    @FXML
    private Label skillReadyLabel;
    @FXML
    private Button infoToggleButton;
    @FXML
    private VBox robotInfoBox;
    @FXML
    private VBox skillInfoBox;
    @FXML
    private Label skillTypeInfoLabel;
    @FXML
    private Label skillDetailLine1Label;
    @FXML
    private Label skillDetailLine2Label;
    @FXML
    private Label latestLogLabel;
    @FXML
    private Button logToggleButton;
    @FXML
    private VBox logPanelBox;
    @FXML
    private ScrollPane logScrollPane;
    @FXML
    private VBox combatLogContent;
    @FXML
    private Button skillButton;
    @FXML
    private Button endTurnButton;
    @FXML
    private Button surrenderButton;
    @FXML
    private HBox actionButtonsBox;
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
    private boolean infoSkillMode = false;
    private boolean logExpanded = false;

    // ── Factory ───────────────────────────────────────────────────────────────
    /**
     * Crea una nueva instancia de la clase.
     */
    public GameScreen() {
    }

    /**
     * Ejecuta la logica de create.
     * @param game parametro de entrada.
     * @return valor resultante de la operacion.
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
            ctrl.enforceUppercaseText(root);
            ctrl.initButtonSounds(root);
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
        if (surrenderButton != null) {
            surrenderButton.setDisable(true);
        }
        overlayAcceptButton.setOnAction(e -> fadeOutNode(overlayPane, 180));
        victoryMenuButton.setOnAction(e -> returnToMenu());
        if (logScrollPane != null) {
            logScrollPane.setVisible(false);
            logScrollPane.setManaged(false);
            logScrollPane.setMouseTransparent(true);
        }
        if (logPanelBox != null) {
            logPanelBox.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
            logPanelBox.setPrefHeight(36.0);
            logPanelBox.setMinHeight(36.0);
            logPanelBox.setMaxHeight(36.0);
            logPanelBox.setPickOnBounds(false);
        }
        if (latestLogLabel != null) {
            latestLogLabel.setText("LOG");
            latestLogLabel.setMouseTransparent(true);
        }
        if (combatLogContent != null) {
            combatLogContent.setMouseTransparent(true);
        }
        makePanelPassThrough(infoPanel, infoToggleButton);
        makePanelPassThrough(logPanelBox, logToggleButton);
        makePanelPassThrough(actionButtonsBox, null);
        setMouseTransparentRecursively(skillButton, false);
        setMouseTransparentRecursively(endTurnButton, false);

        startDeployPhase();
    }

    /**
     * Hace que un panel no intercepte hover/clicks del mapa, excepto un botón
     * interactivo opcional.
     */
    private void makePanelPassThrough(Node panelRoot, Node keepInteractive) {
        if (panelRoot == null) {
            return;
        }
        panelRoot.setMouseTransparent(false);
        if (panelRoot instanceof javafx.scene.layout.Region region) {
            region.setPickOnBounds(false);
        }
        if (panelRoot instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                setMouseTransparentRecursively(child, true);
            }
        }
        if (keepInteractive != null) {
            setMouseTransparentRecursively(keepInteractive, false);
            Node current = keepInteractive.getParent();
            while (current != null && current != panelRoot) {
                current.setMouseTransparent(false);
                if (current instanceof javafx.scene.layout.Region region) {
                    region.setPickOnBounds(false);
                }
                current = current.getParent();
            }
        }
    }

    private void setMouseTransparentRecursively(Node node, boolean transparent) {
        if (node == null) {
            return;
        }
        node.setMouseTransparent(transparent);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                setMouseTransparentRecursively(child, transparent);
            }
        }
    }

    private void configureBackground() {
        if (battlefieldBg == null) {
            return;
        }
        battlefieldBg.setImage(loadBackgroundImage());
    }

    private Image loadBackgroundImage() {
        String[] candidates = {
                "/com/titozeio/images/bg1.jpg",
                "/com/titozeio/images/bg2.jpg"
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
        updateRobotInfoPanel(next);
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
        SoundManager.play(SoundManager.UI_CLICK_CONFIRM);
        addLogMessage(deployingPlayer.getName() + " despliega " + robot.getModelName()
                + " en (" + hex.getQ() + "," + hex.getR() + ").");
        updateDeployHud();
        hexRenderer.markLanding(hex);
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
        if (surrenderButton != null) {
            surrenderButton.setDisable(false);
        }
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
                updateRobotInfoPanel(robot);
                if (infoPanel != null) {
                    infoPanel.setOpacity(0.7);
                }
            }
            return;
        }
        clearSelection();
        showTerrainInfo(hex);
    }

    // ── Selección y modos ─────────────────────────────────────────────────────

    private void selectRobot(Robot robot) {
        selectedRobot = robot;
        SoundManager.play(SoundManager.UI_UNIT_SELECT);
        if (infoPanel != null) {
            infoPanel.setOpacity(1.0);
        }
        infoSkillMode = false;
        if (infoToggleButton != null) {
            infoToggleButton.setText("+info");
        }
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

        SoundManager.play(SoundManager.UI_ABILITY_READY);
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
        SoundManager.play(SoundManager.MECH_MOVE);
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

        // Avisar al renderizador para que anime el disparo
        hexRenderer.markAttack(selectedRobot.getPosition(), targetHex);

        // SFX del arma del atacante
        playWeaponSfx(selectedRobot);

        int hpBefore = target.getCurrentHp();
        selectedRobot.attack(target, game.getMap());
        int damage = hpBefore - target.getCurrentHp();

        if (damage > 0) {
            SoundManager.play(SoundManager.MECH_RECEIVE_HIT);
            addLogMessage("💥 " + selectedRobot.getModelName() + " → " + target.getModelName()
                    + ": " + damage + " daño. HP: " + target.getCurrentHp() + "/" + target.getMaxHp());
        } else {
            addLogMessage(selectedRobot.getModelName() + " ataca a " + target.getModelName() + " sin causar daño.");
        }

        if (target.isDestroyed()) {
            targetHex.setOccupant(null);
            addLogMessage("☠ " + target.getModelName() + " ha sido DESTRUIDO.");
        }
        clearSelection();
        checkAndShowVictory("eliminación");
    }

    /** Reproduce el SFX de arma adecuado según el tipo de ataque del robot. */
    private void playWeaponSfx(Robot robot) {
        if (robot == null || robot.getWeapon() == null)
            return;
        AttackType type = robot.getWeapon().getAttackType();
        int range = robot.getWeapon().getRange();
        if (type == AttackType.ENERGY) {
            SoundManager.play(SoundManager.LASER_SHOT);
        } else if (type == AttackType.EXPLOSIVE) {
            SoundManager.play(SoundManager.WEAPON_MISSILES);
        } else if (type == AttackType.KINETIC && range <= 1) {
            // Arma melee cinética (Death Knight – Martillo propulsado)
            SoundManager.play(SoundManager.PLASMA_SABER);
        } else {
            SoundManager.play(SoundManager.LASER_SHOT);
        }
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
        String skillClass = skill.getClass().getSimpleName();

        // SFX de la skill antes de ejecutar
        playSkillSfx(skillClass);

        if (isAttackSkill) {
            // Avisar al renderizador para que anime el disparo/habilidad
            boolean isHeal = skillClass.equals("RepairNanobots");
            hexRenderer.markAttack(selectedRobot.getPosition(), hex, isHeal);

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
                SoundManager.play(SoundManager.MECH_RECEIVE_HIT);
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
            // Skill de movimiento: mueve al robot
            selectedRobot.useSkill(hex);
            addLogMessage("⚡ " + skillName + ": " + selectedRobot.getModelName()
                    + " se teletransporta a (" + hex.getQ() + "," + hex.getR() + ").");
        }

        SoundManager.play(SoundManager.UI_ABILITY_COOLDOWN);
        clearSelection();
        checkAndShowVictory("eliminación");
    }

    /** Reproduce el SFX correcto para cada skill. */
    private void playSkillSfx(String skillClass) {
        switch (skillClass) {
            case "FreezeRay":
                SoundManager.play(SoundManager.WEAPON_FREEZING_RAY);
                break;
            case "GuidedMissile":
                SoundManager.play(SoundManager.MISSILE_LAUNCH);
                break;
            case "RepairNanobots":
                SoundManager.play(SoundManager.REPAIR_NANOBOTS);
                break;
            case "JetpackBoost":
                SoundManager.play(SoundManager.MOBILITY_JUMP_THRUSTERS);
                break;
            case "TurboPropulsion":
                SoundManager.play(SoundManager.MOBILITY_HYPERSPEED_DASH);
                break;
            case "PlasmaSaberAttack":
                SoundManager.play(SoundManager.WEAPON_PLASMA_SABER);
                break;
            default:
                SoundManager.play(SoundManager.UI_ABILITY_READY);
        }
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
            skillButton.setGraphic(null);
            skillButton.setDisable(true);
            return;
        }
        Skill sk = robot.getSkill();
        skillButton.setGraphic(createSkillButtonGraphic(sk));
        skillButton.setContentDisplay(ContentDisplay.LEFT);
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
        if (robot == null) {
            if (infoTitleLabel != null) {
                infoTitleLabel.setText("INFORMACIÓN");
            }
            if (ownerBadgeLabel != null) {
                ownerBadgeLabel.setText("");
            }
            if (skillInfoIconImageView != null) {
                skillInfoIconImageView.setImage(null);
            }
            return;
        }
        Skill sk = robot.getSkill();
        if (infoTitleLabel != null)
            infoTitleLabel.setText(robot.getModelName());
        if (ownerBadgeLabel != null) {
            ownerBadgeLabel.setText(resolveOwnerBadge(robot));
        }
        if (infoPortraitImage != null)
            infoPortraitImage.setImage(loadPortraitForRobot(robot));
        renderSegmentedHpBar(hpBarContainer, robot.getCurrentHp(), robot.getMaxHp());
        if (hpValueLabel != null)
            hpValueLabel.setText(robot.getCurrentHp() + "/" + robot.getMaxHp());
        if (skillNameInfoLabel != null)
            skillNameInfoLabel.setText(sk != null ? sk.getName() : "Sin skill");
        if (skillInfoIconImageView != null)
            skillInfoIconImageView.setImage(loadSkillIcon(sk));
        colorSkillName(sk);
        renderSkillCooldown(sk);

        if (infoSkillMode) {
            if (robotInfoBox != null) {
                robotInfoBox.setVisible(false);
                robotInfoBox.setManaged(false);
            }
            if (skillInfoBox != null) {
                skillInfoBox.setVisible(true);
                skillInfoBox.setManaged(true);
            }
            if (moveValueLabel != null)
                moveValueLabel.setText("-");
            if (rangeValueLabel != null)
                rangeValueLabel.setText("-");
            if (damageValueLabel != null)
                damageValueLabel.setText("-");
            if (sk == null) {
                if (skillTypeInfoLabel != null)
                    skillTypeInfoLabel.setText("Tipo: -");
                if (skillDetailLine1Label != null)
                    skillDetailLine1Label.setText("-");
                if (skillDetailLine2Label != null)
                    skillDetailLine2Label.setText("-");
            } else {
                String type = inferSkillCategory(sk);
                if (skillTypeInfoLabel != null)
                    skillTypeInfoLabel.setText("Tipo: " + type);
                if ("movimiento".equals(type)) {
                    if (skillDetailLine1Label != null)
                        skillDetailLine1Label.setText("Movimiento: " + extractStat(sk, "Movimiento"));
                    if (skillDetailLine2Label != null)
                        skillDetailLine2Label.setText("");
                } else if ("curación".equals(type)) {
                    if (skillDetailLine1Label != null)
                        skillDetailLine1Label.setText("Alcance: " + extractStat(sk, "Alcance"));
                    if (skillDetailLine2Label != null)
                        skillDetailLine2Label.setText("Recuperación: " + extractStat(sk, "Curación"));
                } else {
                    if (skillDetailLine1Label != null)
                        skillDetailLine1Label.setText("Alcance: " + extractStat(sk, "Alcance"));
                    if (skillDetailLine2Label != null)
                        skillDetailLine2Label.setText("Daño: " + extractStat(sk, "Daño"));
                }
            }
        } else {
            if (robotInfoBox != null) {
                robotInfoBox.setVisible(true);
                robotInfoBox.setManaged(true);
            }
            if (skillInfoBox != null) {
                skillInfoBox.setVisible(false);
                skillInfoBox.setManaged(false);
            }
            if (moveValueLabel != null)
                moveValueLabel.setText(String.valueOf(robot.getMovementPoints()));
            if (rangeValueLabel != null)
                rangeValueLabel.setText(String.valueOf(robot.getWeapon().getRange()));
            if (damageValueLabel != null)
                damageValueLabel.setText(String.valueOf(robot.getWeapon().getDamage()));
            infoDetailsLabel.setText("");
        }

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
        if (!infoSkillMode) {
            infoDetailsLabel.setText("");
        }
    }

    @FXML
    /**
     * Ejecuta la logica de handle info toggle action.
     */
    public void handleInfoToggleAction() {
        if (selectedRobot == null) {
            return;
        }
        infoSkillMode = !infoSkillMode;
        if (infoToggleButton != null) {
            infoToggleButton.setText(infoSkillMode ? "-info" : "+info");
        }
        updateRobotInfoPanel(selectedRobot);
    }

    @FXML
    /**
     * Ejecuta la logica de handle log toggle action.
     */
    public void handleLogToggleAction() {
        logExpanded = !logExpanded;
        if (logScrollPane == null || logToggleButton == null) {
            return;
        }
        logScrollPane.setManaged(logExpanded);
        logScrollPane.setVisible(logExpanded);
        logToggleButton.setText(logExpanded ? "-" : "+");
        if (logPanelBox != null) {
            logPanelBox.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
            double h = logExpanded ? 200.0 : 36.0;
            logPanelBox.setPrefHeight(h);
            logPanelBox.setMinHeight(h);
            logPanelBox.setMaxHeight(h);
            logPanelBox.setPickOnBounds(false);
        }
    }

    private void renderSkillCooldown(Skill sk) {
        if (skillCooldownDots == null || skillReadyLabel == null) {
            return;
        }
        skillCooldownDots.getChildren().clear();
        if (sk == null) {
            skillReadyLabel.setVisible(false);
            skillReadyLabel.setManaged(false);
            return;
        }
        int total = Math.max(0, sk.getCooldown());
        int remaining = Math.max(0, sk.getCurrentCooldown());
        if (remaining == 0) {
            skillReadyLabel.setVisible(true);
            skillReadyLabel.setManaged(true);
            return;
        }
        skillReadyLabel.setVisible(false);
        skillReadyLabel.setManaged(false);
        int filled = Math.max(0, total - remaining);
        for (int i = 0; i < total; i++) {
            Circle dot = new Circle(4.0);
            if (i < filled) {
                dot.setFill(Color.web("#7AEBF1"));
                dot.setOpacity(1.0);
            } else {
                dot.setFill(Color.web("#7AEBF1"));
                dot.setOpacity(0.4);
            }
            skillCooldownDots.getChildren().add(dot);
        }
    }

    private String inferSkillCategory(Skill sk) {
        String c = sk.getClass().getSimpleName();
        if ("RepairNanobots".equals(c)) {
            return "curación";
        }
        if (sk.getType() == SkillType.MOVEMENT) {
            return "movimiento";
        }
        return "ataque";
    }

    private String extractStat(Skill sk, String keyStartsWith) {
        for (String[] s : sk.getDisplayStats()) {
            if (s.length >= 2 && s[0].toLowerCase().startsWith(keyStartsWith.toLowerCase())) {
                return s[1];
            }
        }
        return "-";
    }

    private Image loadPortraitForRobot(Robot robot) {
        String model = robot.getModelName().toLowerCase().replace(" ", "");
        String file;
        if (model.contains("saber")) {
            file = "face_saberprime.png";
        } else if (model.contains("bullseye")) {
            file = "face_bullseye.png";
        } else if (model.contains("bulwark")) {
            file = "face_bulwark.png";
        } else if (model.contains("scout")) {
            file = "face_scout.png";
        } else if (model.contains("death")) {
            file = "face_deathknight.png";
        } else if (model.contains("ice")) {
            file = "face_iceage.png";
        } else {
            return null;
        }
        var res = getClass().getResource("/com/titozeio/sprites/" + file);
        return res == null ? null : new Image(res.toExternalForm());
    }

    private Image loadSkillIcon(Skill skill) {
        if (skill == null) {
            return null;
        }
        String fileName;
        switch (skill.getClass().getSimpleName()) {
            case "FreezeRay":
                fileName = "skill_freezing_ray.png";
                break;
            case "GuidedMissile":
                fileName = "skill_guided_missiles.png";
                break;
            case "RepairNanobots":
                fileName = "skill_repair_nanobots.png";
                break;
            case "PlasmaSaberAttack":
                fileName = "skill_plasma_saber.png";
                break;
            case "JetpackBoost":
                fileName = "skill_retro_thrusters.png";
                break;
            case "TurboPropulsion":
                fileName = "skill_turbo_thrusters.png";
                break;
            default:
                return null;
        }
        var res = getClass().getResource("/com/titozeio/ui/" + fileName);
        return res == null ? null : new Image(res.toExternalForm());
    }

    private ImageView createSkillButtonGraphic(Skill skill) {
        Image icon = loadSkillIcon(skill);
        if (icon == null) {
            return null;
        }
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(16.0);
        imageView.setFitHeight(16.0);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private void colorSkillName(Skill sk) {
        if (skillNameInfoLabel == null)
            return;
        if (sk == null) {
            skillNameInfoLabel.setTextFill(Color.web("#7AEBF1"));
            return;
        }
        String type = inferSkillCategory(sk);
        if ("movimiento".equals(type)) {
            skillNameInfoLabel.setTextFill(Color.web("#66c6ff"));
        } else if ("curación".equals(type)) {
            skillNameInfoLabel.setTextFill(Color.web("#70ff70"));
        } else {
            skillNameInfoLabel.setTextFill(Color.web("#ff6b6b"));
        }
    }

    private String resolveOwnerBadge(Robot robot) {
        if (robot == null || robot.getOwner() == null || robot.getOwner().getName() == null) {
            return "";
        }
        String n = robot.getOwner().getName().toLowerCase().replace(" ", "");
        if (n.contains("1") || n.contains("p1"))
            return "P1";
        if (n.contains("2") || n.contains("p2"))
            return "P2";
        return "";
    }

    private void renderSegmentedHpBar(StackPane container, int hp, int maxHp) {
        if (container == null)
            return;
        container.getChildren().clear();
        if (maxHp <= 0)
            return;
        Pane pane = new Pane();
        double w = container.getPrefWidth() > 0 ? container.getPrefWidth() : 130.0;
        double h = container.getPrefHeight() > 0 ? container.getPrefHeight() : 10.0;
        pane.setPrefSize(w, h);
        Rectangle bg = new Rectangle(0, 0, w, h);
        bg.setFill(Color.web("#2d2d2d", 0.8));
        pane.getChildren().add(bg);
        double gap = 1.0;
        double segW = (w - gap * (maxHp + 1)) / maxHp;
        for (int i = 0; i < maxHp; i++) {
            Rectangle seg = new Rectangle(gap + i * (segW + gap), 1, segW, h - 2);
            seg.setFill(Color.web("#44dd44"));
            seg.setOpacity(i < hp ? 1.0 : 0.4);
            pane.getChildren().add(seg);
        }
        container.getChildren().add(pane);
    }

    private void clearAllHighlights() {
        hexRenderer.setMoveHighlightedHexes(new HashSet<>());
        hexRenderer.setAttackHighlightedHexes(new HashSet<>());
        hexRenderer.setSkillHighlightedHexes(new HashSet<>(), false);
        hexRenderer.render(game.getMap());
    }

    private void clearSelection() {
        selectedRobot = null;
        infoSkillMode = false;
        if (infoToggleButton != null)
            infoToggleButton.setText("+info");
        selectionMode = SelectionMode.NONE;
        skillButton.setText("SKILL");
        skillButton.setGraphic(null);
        skillButton.setDisable(true);
        clearAllHighlights();
        infoDetailsLabel.setText("Selecciona un robot tuyo para actuar.");
        if (infoTitleLabel != null)
            infoTitleLabel.setText("INFORMACIÓN");
        if (ownerBadgeLabel != null)
            ownerBadgeLabel.setText("");
        if (infoPanel != null)
            infoPanel.setOpacity(1.0);
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
        if (infoTitleLabel != null)
            infoTitleLabel.setText("INFORMACIÓN");
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
        boolean bySurrender = reason.equals("rendición");
        victoryIcon.setText(bySurrender ? "⚑" : (byElimination ? "🏆" : "🏴"));
        victoryTitle.setText("¡" + winner.getName().toUpperCase() + " GANA!");
        victoryTitle.setStyle("-fx-font-size: 32px; -fx-text-fill: "
                + (isP1 ? "#5599ff" : "#ff5555") + ";");
        if (bySurrender) {
            victoryReason.setText(winner.getName() + " gana porque el rival se ha rendido.");
        } else if (byElimination) {
            victoryReason.setText(winner.getName() + " ha eliminado a todos los robots rivales.");
        } else {
            victoryReason.setText(winner.getName() + " ha conquistado la base enemiga durante 2 turnos.");
        }
        victoryTurnLabel.setText("Partida resuelta en el turno " + game.getTurnCounter());
        fadeInNode(victoryPane, 300);
        endTurnButton.setDisable(true);
        skillButton.setDisable(true);
        if (surrenderButton != null) {
            surrenderButton.setDisable(true);
        }
        addLogMessage("🏆 ¡" + winner.getName() + " gana por " + reason + "!");
    }

    @FXML
    /**
     * Ejecuta la logica de handle surrender action.
     */
    public void handleSurrenderAction() {
        if (game.getPhase() != GamePhase.COMBAT) {
            return;
        }
        Player loser = game.getCurrentPlayer();
        Player winner = game.getOpponent(loser);
        addLogMessage("⚑ " + loser.getName() + " se ha rendido.");
        showVictory(winner, "rendición");
    }

    private void returnToMenu() {
        MainMenuScreen menu = MainMenuScreen.create(game.getStage(), game);
        if (menu != null)
            game.displayScreen(menu);
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    /**
     * Muestra overlay message.
     * @param title parametro de entrada.
     * @param message parametro de entrada.
     */
    public void showOverlayMessage(String title, String message) {
        overlayTitle.setText(title);
        overlayMessage.setText(message);
        fadeInNode(overlayPane, 200);
    }

    /**
     * Agrega log message.
     * @param message parametro de entrada.
     */
    public void addLogMessage(String message) {
        Label logLine = new Label("> " + message);
        logLine.setStyle("-fx-text-fill: #e1e1e1; -fx-font-family: 'Exo 2'; -fx-font-size: 12px;");
        combatLogContent.getChildren().add(logLine);
        logScrollPane.setVvalue(1.0);
        if (latestLogLabel != null) {
            latestLogLabel.setText("> " + message);
        }
    }

    // ── Screen ────────────────────────────────────────────────────────────────

    @Override
    /**
     * Ejecuta la logica de display.
     */
    public void display() {
        System.out.println("Mostrando GameScreen...");
        this.window.setScene(this.scene);
    }

    @Override
    /**
     * Ejecuta la logica de handle input.
     * @param input parametro de entrada.
     */
    public void handleInput(String input) {
        switch (input.trim().toUpperCase()) {
            case "F":
                if (game.getPhase() == GamePhase.COMBAT) {
                    SoundManager.play(SoundManager.UI_TURN_CHANGE);
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
                SoundManager.play(SoundManager.UI_INVALID);
                System.out.println("Acción no reconocida: " + input);
        }
    }
}
