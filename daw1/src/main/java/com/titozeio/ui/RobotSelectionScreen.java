package com.titozeio.ui;

import com.titozeio.engine.Game;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.engine.RobotFactory;
import com.titozeio.enums.RobotTemplate;
import com.titozeio.model.Weapon;
import com.titozeio.skills.Skill;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RobotSelectionScreen extends Screen {
    private static final int MAX_ROBOTS_PER_PLAYER = 3;

    // Campos legacy conservados para compatibilidad con tests/reflexión
    @FXML
    private Label fixedInstructionsLabel;
    @FXML
    private Label turnInstructionsLabel;
    @FXML
    private FlowPane availableRobotsPane;
    @FXML
    private FlowPane descriptionPane;
    @FXML
    private Text descRobotText;
    @FXML
    private Text descWeaponText;
    @FXML
    private Text descSkillText;

    @FXML
    private ImageView conceptImageView;
    @FXML
    private Label statHpValueLabel;
    @FXML
    private Label statMoveValueLabel;
    @FXML
    private Label statDamageValueLabel;
    @FXML
    private Label statRangeValueLabel;
    @FXML
    private Label skillTitleLabel;
    @FXML
    private ImageView skillIconImageView;
    @FXML
    private Label skillTypeValueLabel;
    @FXML
    private Label skillCooldownValueLabel;
    @FXML
    private Label skillStatsValueLabel;
    @FXML
    private Label descRobotLabel;
    @FXML
    private Label descWeaponLabel;
    @FXML
    private Label descSkillLabel;
    @FXML
    private Button confirmButton;
    @FXML
    private Button prevRobotButton;
    @FXML
    private Button nextRobotButton;
    @FXML
    private ImageView p1Slot1Image;
    @FXML
    private ImageView p1Slot2Image;
    @FXML
    private ImageView p1Slot3Image;
    @FXML
    private ImageView p2Slot1Image;
    @FXML
    private ImageView p2Slot2Image;
    @FXML
    private ImageView p2Slot3Image;
    @FXML
    private Label p1Slot1Name;
    @FXML
    private Label p1Slot2Name;
    @FXML
    private Label p1Slot3Name;
    @FXML
    private Label p2Slot1Name;
    @FXML
    private Label p2Slot2Name;
    @FXML
    private Label p2Slot3Name;
    @FXML
    private StackPane p1Slot1Frame;
    @FXML
    private StackPane p1Slot2Frame;
    @FXML
    private StackPane p1Slot3Frame;
    @FXML
    private StackPane p2Slot1Frame;
    @FXML
    private StackPane p2Slot2Frame;
    @FXML
    private StackPane p2Slot3Frame;
    @FXML
    private StackPane turnOverlay;
    @FXML
    private Label turnOverlayLabel;

    private List<RobotTemplate> availableTemplates;
    private List<RobotTemplate> player1Templates;
    private List<RobotTemplate> player2Templates;
    private boolean isPlayer2Turn = true;
    private int currentIndex;

    private Stage stage;
    private Game game;
    private Scene scene;

    /**
     * Ejecuta la logica de create.
     * 
     * @param window parametro de entrada.
     * @param game   parametro de entrada.
     * @return valor resultante de la operacion.
     */
    public static RobotSelectionScreen create(Stage window, Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    RobotSelectionScreen.class.getResource("/com/titozeio/ui/RobotSelectionScreen.fxml"));
            Parent root = loader.load();
            RobotSelectionScreen ctrl = loader.getController();
            ctrl.stage = window;
            ctrl.game = game;
            ctrl.scene = new Scene(root, 1280, 720);
            ctrl.applyGlobalStyle(ctrl.scene);
            ctrl.enforceUppercaseText(root);
            ctrl.initButtonSounds(root);
            ctrl.initializeData();
            ctrl.isPlayer2Turn = true;
            ctrl.refreshView();
            ctrl.showTurnOverlay();
            return ctrl;
        } catch (IOException e) {
            System.err.println("Error al cargar RobotSelectionScreen.fxml: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void initializeData() {
        this.availableTemplates = new ArrayList<>(Arrays.asList(RobotTemplate.values()));
        this.player1Templates = new ArrayList<>();
        this.player2Templates = new ArrayList<>();
        this.isPlayer2Turn = false;
        this.currentIndex = 0;
    }

    // Compatibilidad con tests antiguos que invocan initializeData(List) por
    // reflexión
    @Deprecated(forRemoval = true)
    private void initializeData(List<RobotTemplate> allTemplates) {
        this.availableTemplates = new ArrayList<>(allTemplates);
        this.player1Templates = new ArrayList<>();
        this.player2Templates = new ArrayList<>();
        this.isPlayer2Turn = false;
        this.currentIndex = 0;
    }

    // Compatibilidad con tests antiguos (sin uso en la nueva UI)
    @Deprecated(forRemoval = true)
    private void refreshRobotCards() {
        refreshView();
    }

    // Compatibilidad con tests antiguos (selección por tarjeta)
    @Deprecated(forRemoval = true)
    private void onCardClicked(RobotTemplate template, RobotCardController ignored) {
        int idx = availableTemplates.indexOf(template);
        if (idx >= 0) {
            currentIndex = idx;
        }
        refreshView();
    }

    @FXML
    /**
     * Ejecuta la logica de handle prev robot action.
     */
    public void handlePrevRobotAction() {
        if (availableTemplates.isEmpty()) {
            return;
        }
        currentIndex = (currentIndex - 1 + availableTemplates.size()) % availableTemplates.size();
        refreshView();
    }

    @FXML
    /**
     * Ejecuta la logica de handle next robot action.
     */
    public void handleNextRobotAction() {
        if (availableTemplates.isEmpty()) {
            return;
        }
        currentIndex = (currentIndex + 1) % availableTemplates.size();
        refreshView();
    }

    @FXML
    /**
     * Ejecuta la logica de handle confirm action.
     */
    public void handleConfirmAction() {
        if (availableTemplates.isEmpty()) {
            return;
        }
        RobotTemplate selected = availableTemplates.get(currentIndex);
        if (isPlayer2Turn) {
            player2Templates.add(selected);
        } else {
            player1Templates.add(selected);
        }
        availableTemplates.remove(selected);

        if (isSelectionComplete()) {
            transitionToCombatScreen();
            return;
        }
        if (currentIndex >= availableTemplates.size()) {
            currentIndex = Math.max(0, availableTemplates.size() - 1);
        }
        isPlayer2Turn = !isPlayer2Turn;
        refreshView();
        showTurnOverlay();
    }

    @FXML
    /**
     * Ejecuta la logica de handle skip action.
     */
    public void handleSkipAction() {
        List<RobotTemplate> pool = new ArrayList<>(availableTemplates);
        Collections.shuffle(pool);
        int p1Missing = MAX_ROBOTS_PER_PLAYER - player1Templates.size();
        int p2Missing = MAX_ROBOTS_PER_PLAYER - player2Templates.size();
        int idx = 0;
        for (int i = 0; i < p1Missing && idx < pool.size(); i++) {
            player1Templates.add(pool.get(idx++));
        }
        for (int i = 0; i < p2Missing && idx < pool.size(); i++) {
            player2Templates.add(pool.get(idx++));
        }
        transitionToCombatScreen();
    }

    @FXML
    /**
     * Ejecuta la logica de handle dismiss turn overlay.
     */
    public void handleDismissTurnOverlay() {
        fadeOutNode(turnOverlay, 200);
    }

    private void refreshView() {
        updateTurnLabel();
        updateArrowsAvailability();
        updateCurrentRobotInfo();
        updateBottomTeams();
    }

    private void updateTurnLabel() {
        // En la nueva UI el turno se muestra principalmente en el overlay
    }

    private void updateArrowsAvailability() {
        boolean disabled = availableTemplates.size() <= 1;
        if (prevRobotButton != null) {
            prevRobotButton.setDisable(disabled);
        }
        if (nextRobotButton != null) {
            nextRobotButton.setDisable(disabled);
        }
        if (confirmButton != null) {
            confirmButton.setDisable(availableTemplates.isEmpty());
            confirmButton.setOpacity(availableTemplates.isEmpty() ? 0.4 : 1.0);
        }
    }

    private void updateCurrentRobotInfo() {
        if (availableTemplates.isEmpty()) {
            if (conceptImageView != null)
                conceptImageView.setImage(null);
            if (statHpValueLabel != null)
                statHpValueLabel.setText("-");
            if (statMoveValueLabel != null)
                statMoveValueLabel.setText("-");
            if (statDamageValueLabel != null)
                statDamageValueLabel.setText("-");
            if (statRangeValueLabel != null)
                statRangeValueLabel.setText("-");
            if (skillTitleLabel != null)
                skillTitleLabel.setText("SKILL");
            if (skillIconImageView != null)
                skillIconImageView.setImage(null);
            updateSkillTitleFont();
            if (skillTypeValueLabel != null)
                skillTypeValueLabel.setText("-");
            if (skillCooldownValueLabel != null)
                skillCooldownValueLabel.setText("-");
            if (skillStatsValueLabel != null)
                skillStatsValueLabel.setText("-");
            if (descRobotLabel != null)
                descRobotLabel.setText("-");
            if (descWeaponLabel != null)
                descWeaponLabel.setText("-");
            if (descSkillLabel != null)
                descSkillLabel.setText("-");
            return;
        }
        RobotTemplate template = availableTemplates.get(currentIndex);
        Robot sample = template.createRobot(null);
        Weapon weapon = sample.getWeapon();
        Skill skill = sample.getSkill();

        if (conceptImageView != null) {
            conceptImageView.setImage(loadConceptImage(template));
            animateHologramPulse(conceptImageView);
        }

        // Animamos los textos de stats como si fueran una terminal recalculando
        animateTextFlicker(statHpValueLabel, String.valueOf(sample.getMaxHp()));
        animateTextFlicker(statMoveValueLabel, String.valueOf(sample.getMovementPoints()));
        animateTextFlicker(statDamageValueLabel, String.valueOf(weapon.getDamage()));
        animateTextFlicker(statRangeValueLabel, String.valueOf(weapon.getRange()));

        if (skill != null) {
            if (skillIconImageView != null) {
                skillIconImageView.setImage(loadSkillIcon(skill));
                fadeInNode(skillIconImageView, 200);
            }
            if (skillTitleLabel != null) {
                skillTitleLabel.setText(skill.getName());
                animateTextFlicker(skillTitleLabel, skill.getName());
            }
            updateSkillTitleFont();
            if (skillTypeValueLabel != null)
                skillTypeValueLabel.setText(String.valueOf(skill.getType()));
            if (skillCooldownValueLabel != null)
                skillCooldownValueLabel.setText(String.valueOf(skill.getCooldown()));
            if (skillStatsValueLabel != null)
                skillStatsValueLabel.setText(formatSkillStats(skill));
            if (descSkillLabel != null)
                descSkillLabel.setText(skill.getDescription());
        } else {
            if (skillIconImageView != null)
                skillIconImageView.setImage(null);
            if (skillTitleLabel != null)
                skillTitleLabel.setText("Sin skill");
            updateSkillTitleFont();
            if (skillTypeValueLabel != null)
                skillTypeValueLabel.setText("-");
            if (skillCooldownValueLabel != null)
                skillCooldownValueLabel.setText("-");
            if (skillStatsValueLabel != null)
                skillStatsValueLabel.setText("-");
            if (descSkillLabel != null)
                descSkillLabel.setText("-");
        }

        if (descRobotLabel != null)
            descRobotLabel.setText(sample.getDescription());
        if (descWeaponLabel != null)
            descWeaponLabel.setText(weapon.getDescription());
    }

    private void updateSkillTitleFont() {
        if (skillTitleLabel == null) {
            return;
        }
        String txt = skillTitleLabel.getText();
        int len = txt == null ? 0 : txt.length();
        double size = 24.0;
        if (len > 22) {
            size = 16.0;
        } else if (len > 16) {
            size = 18.0;
        } else if (len > 12) {
            size = 20.0;
        }
        skillTitleLabel.setFont(Font.font("Exo 2 Regular", size));
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

    private String formatSkillStats(Skill skill) {
        List<String[]> stats = skill.getDisplayStats();
        if (stats.isEmpty()) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (String[] stat : stats) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(stat[0]).append(stat[1]);
        }
        return sb.toString();
    }

    private Image loadConceptImage(RobotTemplate template) {
        String file;
        switch (template) {
            case VICTORY_SABER:
                file = "saberprime_concept.jpg";
                break;
            case BULLSEYE:
                file = "bullseye_concep.jpg";
                break;
            case BULWARK:
                file = "bulwark_concept.jpg";
                break;
            case SCOUT:
                file = "scout_concept.jpg";
                break;
            case DEATH_KNIGHT:
                file = "deathknight_concept.jpg";
                break;
            case ICE_AGE:
                file = "iceage_concept.jpg";
                break;
            default:
                return null;
        }
        var res = getClass().getResource("/com/titozeio/images/" + file);
        return res == null ? null : new Image(res.toExternalForm());
    }

    private Image loadTeamSprite(RobotTemplate template, boolean p1) {
        String file;
        switch (template) {
            case VICTORY_SABER:
                file = "face_saberprime.png";
                break;
            case BULLSEYE:
                file = "face_bullseye.png";
                break;
            case BULWARK:
                file = "face_bulwark.png";
                break;
            case SCOUT:
                file = "face_scout.png";
                break;
            case DEATH_KNIGHT:
                file = "face_deathknight.png";
                break;
            case ICE_AGE:
                file = "face_iceage.png";
                break;
            default:
                return null;
        }
        var res = getClass().getResource("/com/titozeio/sprites/" + file);
        return res == null ? null : new Image(res.toExternalForm());
    }

    private void updateBottomTeams() {
        if (p1Slot1Image == null || p2Slot1Image == null) {
            return;
        }
        setTeamSlot(player1Templates, 0, p1Slot1Image, p1Slot1Name, p1Slot1Frame, true);
        setTeamSlot(player1Templates, 1, p1Slot2Image, p1Slot2Name, p1Slot2Frame, true);
        setTeamSlot(player1Templates, 2, p1Slot3Image, p1Slot3Name, p1Slot3Frame, true);
        setTeamSlot(player2Templates, 0, p2Slot1Image, p2Slot1Name, p2Slot1Frame, false);
        setTeamSlot(player2Templates, 1, p2Slot2Image, p2Slot2Name, p2Slot2Frame, false);
        setTeamSlot(player2Templates, 2, p2Slot3Image, p2Slot3Name, p2Slot3Frame, false);
    }

    private void setTeamSlot(List<RobotTemplate> team, int index, ImageView image, Label name, StackPane frame,
            boolean p1) {
        if (index < team.size()) {
            RobotTemplate t = team.get(index);
            Robot sample = t.createRobot(null);
            name.setText(sample.getModelName());

            // Si el slot estaba vacío (imagen null), animamos la aparición
            if (image.getImage() == null) {
                image.setImage(loadTeamSprite(t, p1));
                image.setOpacity(0.0);
                fadeInNode(image, 400);
                if (frame != null) {
                    frame.setOpacity(0.0);
                    fadeInNode(frame, 400);
                }
            } else {
                image.setImage(loadTeamSprite(t, p1));
                image.setOpacity(1.0);
                if (frame != null) {
                    frame.setOpacity(1.0);
                }
            }
        } else {
            name.setText("---");
            image.setImage(null);
            image.setOpacity(0.4);
            if (frame != null) {
                frame.setOpacity(0.3);
            }
        }
    }

    private void showTurnOverlay() {
        if (turnOverlay == null || turnOverlayLabel == null) {
            return;
        }
        String jugador = isPlayer2Turn ? "P2" : "P1";
        turnOverlayLabel.setText("Turno de elegir robot: " + jugador);

        if (!Screen.areAnimationsEnabled()) {
            turnOverlay.setVisible(true);
            turnOverlay.setManaged(true);
            turnOverlay.setOpacity(1.0);
            turnOverlay.setMouseTransparent(false);
            return;
        }

        PauseTransition delay = new PauseTransition(Duration.millis(120));
        delay.setOnFinished(ev -> showOverlayContent());
        delay.play();
    }

    private void showOverlayContent() {
        if (turnOverlay == null)
            return;
        turnOverlay.setMouseTransparent(true);
        fadeInNode(turnOverlay, 220);

        PauseTransition enableMouse = new PauseTransition(Duration.millis(240));
        enableMouse.setOnFinished(ev -> turnOverlay.setMouseTransparent(false));
        enableMouse.play();

        PauseTransition autoHide = new PauseTransition(Duration.seconds(3));
        autoHide.setOnFinished(ev -> handleDismissTurnOverlay());
        autoHide.play();
    }

    private boolean isSelectionComplete() {
        return player1Templates.size() == MAX_ROBOTS_PER_PLAYER && player2Templates.size() == MAX_ROBOTS_PER_PLAYER;
    }

    private void transitionToCombatScreen() {
        Player p1 = game.getP1();
        Player p2 = game.getP2();
        p1.getUnits().clear();
        p2.getUnits().clear();
        for (RobotTemplate t : player1Templates) {
            p1.addUnit(RobotFactory.createRobot(t.name(), p1));
        }
        for (RobotTemplate t : player2Templates) {
            p2.addUnit(RobotFactory.createRobot(t.name(), p2));
        }
        game.displayScreen(GameScreen.create(game));
    }

    @Override
    /**
     * Ejecuta la logica de display.
     */
    public void display() {
        this.stage.setScene(this.scene);
    }

    /**
     * Crea un pulso de "holograma" más intenso con micro-destellos y glitches.
     */
    private void animateHologramPulse(ImageView iv) {
        if (iv == null)
            return;

        ColorAdjust adj = new ColorAdjust();
        iv.setEffect(adj);

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(adj.brightnessProperty(), 0.0),
                        new KeyValue(adj.contrastProperty(), 0.0),
                        new KeyValue(iv.opacityProperty(), 0.5)),
                new KeyFrame(Duration.millis(80),
                        new KeyValue(adj.brightnessProperty(), 0.9), // Flash fuerte
                        new KeyValue(adj.contrastProperty(), 0.6),
                        new KeyValue(iv.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(150),
                        new KeyValue(adj.brightnessProperty(), 0.2),
                        new KeyValue(iv.opacityProperty(), 0.7) // Glitch de opacidad
                ),
                new KeyFrame(Duration.millis(220),
                        new KeyValue(adj.brightnessProperty(), 0.7), // Segundo micro-destello
                        new KeyValue(adj.contrastProperty(), 0.4),
                        new KeyValue(iv.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(700),
                        new KeyValue(adj.brightnessProperty(), 0.0),
                        new KeyValue(adj.contrastProperty(), 0.0),
                        new KeyValue(iv.opacityProperty(), 1.0)));
        pulse.play();
    }

    /**
     * Simula una terminal de datos "pensando" con una secuencia de carga más larga.
     */
    private void animateTextFlicker(Label label, String finalValue) {
        if (label == null)
            return;

        Timeline flicker = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    label.setOpacity(0.3);
                    label.setText("---");
                }),
                new KeyFrame(Duration.millis(120), e -> {
                    label.setOpacity(0.8);
                    label.setText("###");
                }),
                new KeyFrame(Duration.millis(240), e -> {
                    label.setOpacity(0.5);
                    label.setText("888");
                }),
                new KeyFrame(Duration.millis(360), e -> {
                    label.setOpacity(0.9);
                    label.setText("???");
                }),
                new KeyFrame(Duration.millis(500), e -> {
                    label.setOpacity(1.0);
                    label.setText(finalValue);
                }));
        flicker.play();
    }

    @Override
    /**
     * Ejecuta la logica de handle input.
     * 
     * @param input parametro de entrada.
     */
    public void handleInput(String input) {
        // Sin uso
    }
}
