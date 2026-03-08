package com.titozeio.ui;

import com.titozeio.engine.Game;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.RobotTemplate;
import com.titozeio.model.Weapon;
import com.titozeio.skills.Skill;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
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
    private boolean isPlayer2Turn;
    private int currentIndex;

    private Stage stage;
    private Game game;
    private Scene scene;

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
            ctrl.initializeData();
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
        this.isPlayer2Turn = true;
        this.currentIndex = 0;
        if (fixedInstructionsLabel != null) {
            fixedInstructionsLabel.setVisible(false);
            fixedInstructionsLabel.setManaged(false);
        }
        if (turnInstructionsLabel != null) {
            turnInstructionsLabel.setVisible(false);
            turnInstructionsLabel.setManaged(false);
        }
    }

    // Compatibilidad con tests antiguos que invocan initializeData(List) por reflexión
    private void initializeData(List<RobotTemplate> allTemplates) {
        this.availableTemplates = new ArrayList<>(allTemplates);
        this.player1Templates = new ArrayList<>();
        this.player2Templates = new ArrayList<>();
        this.isPlayer2Turn = true;
        this.currentIndex = 0;
    }

    // Compatibilidad con tests antiguos (sin uso en la nueva UI)
    private void refreshRobotCards() {
        refreshView();
        if (descriptionPane != null) {
            descriptionPane.setVisible(false);
        }
    }

    // Compatibilidad con tests antiguos (selección por tarjeta)
    private void onCardClicked(RobotTemplate template, RobotCardController ignored) {
        int idx = availableTemplates.indexOf(template);
        if (idx >= 0) {
            currentIndex = idx;
        }
        if (descriptionPane != null) {
            descriptionPane.setVisible(true);
        }
        if (confirmButton != null) {
            confirmButton.setDisable(false);
            confirmButton.setOpacity(1.0);
        }
        Robot sample = template.createRobot(null);
        Weapon weapon = sample.getWeapon();
        Skill skill = sample.getSkill();
        if (descRobotText != null) {
            descRobotText.setText(sample.getDescription());
        }
        if (descWeaponText != null) {
            descWeaponText.setText(weapon.getName() + ": " + weapon.getDescription());
        }
        if (descSkillText != null) {
            descSkillText.setText(skill != null ? skill.getName() + ": " + skill.getDescription() : "Sin skill");
        }
        refreshView();
    }

    @FXML
    public void handlePrevRobotAction() {
        if (availableTemplates.isEmpty()) {
            return;
        }
        currentIndex = (currentIndex - 1 + availableTemplates.size()) % availableTemplates.size();
        refreshView();
    }

    @FXML
    public void handleNextRobotAction() {
        if (availableTemplates.isEmpty()) {
            return;
        }
        currentIndex = (currentIndex + 1) % availableTemplates.size();
        refreshView();
    }

    @FXML
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
    public void handleDismissTurnOverlay() {
        turnOverlay.setVisible(false);
        turnOverlay.setManaged(false);
    }

    private void refreshView() {
        updateTurnLabel();
        updateArrowsAvailability();
        updateCurrentRobotInfo();
        updateBottomTeams();
    }

    private void updateTurnLabel() {
        if (turnInstructionsLabel == null) {
            return;
        }
        String jugador = isPlayer2Turn ? "Jugador 2" : "Jugador 1";
        int elegidos = isPlayer2Turn ? player2Templates.size() : player1Templates.size();
        turnInstructionsLabel.setText(jugador + ", elige robot (" + elegidos + "/" + MAX_ROBOTS_PER_PLAYER + ")");
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
            if (conceptImageView != null) conceptImageView.setImage(null);
            if (statHpValueLabel != null) statHpValueLabel.setText("-");
            if (statMoveValueLabel != null) statMoveValueLabel.setText("-");
            if (statDamageValueLabel != null) statDamageValueLabel.setText("-");
            if (statRangeValueLabel != null) statRangeValueLabel.setText("-");
            if (skillTitleLabel != null) skillTitleLabel.setText("SKILL");
            if (skillTypeValueLabel != null) skillTypeValueLabel.setText("-");
            if (skillCooldownValueLabel != null) skillCooldownValueLabel.setText("-");
            if (skillStatsValueLabel != null) skillStatsValueLabel.setText("-");
            if (descRobotLabel != null) descRobotLabel.setText("-");
            if (descWeaponLabel != null) descWeaponLabel.setText("-");
            if (descSkillLabel != null) descSkillLabel.setText("-");
            return;
        }
        RobotTemplate template = availableTemplates.get(currentIndex);
        Robot sample = template.createRobot(null);
        Weapon weapon = sample.getWeapon();
        Skill skill = sample.getSkill();

        if (conceptImageView != null) conceptImageView.setImage(loadConceptImage(template));
        if (statHpValueLabel != null) statHpValueLabel.setText(String.valueOf(sample.getMaxHp()));
        if (statMoveValueLabel != null) statMoveValueLabel.setText(String.valueOf(sample.getMovementPoints()));
        if (statDamageValueLabel != null) statDamageValueLabel.setText(String.valueOf(weapon.getDamage()));
        if (statRangeValueLabel != null) statRangeValueLabel.setText(String.valueOf(weapon.getRange()));

        if (skill != null) {
            if (skillTitleLabel != null) skillTitleLabel.setText(skill.getName());
            if (skillTypeValueLabel != null) skillTypeValueLabel.setText(String.valueOf(skill.getType()));
            if (skillCooldownValueLabel != null) skillCooldownValueLabel.setText(String.valueOf(skill.getCooldown()));
            if (skillStatsValueLabel != null) skillStatsValueLabel.setText(formatSkillStats(skill));
            if (descSkillLabel != null) descSkillLabel.setText(skill.getDescription());
        } else {
            if (skillTitleLabel != null) skillTitleLabel.setText("Sin skill");
            if (skillTypeValueLabel != null) skillTypeValueLabel.setText("-");
            if (skillCooldownValueLabel != null) skillCooldownValueLabel.setText("-");
            if (skillStatsValueLabel != null) skillStatsValueLabel.setText("-");
            if (descSkillLabel != null) descSkillLabel.setText("-");
        }

        if (descRobotLabel != null) descRobotLabel.setText(sample.getDescription());
        if (descWeaponLabel != null) descWeaponLabel.setText(weapon.getDescription());
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

    private void setTeamSlot(List<RobotTemplate> team, int index, ImageView image, Label name, StackPane frame, boolean p1) {
        if (index < team.size()) {
            RobotTemplate t = team.get(index);
            Robot sample = t.createRobot(null);
            name.setText(sample.getModelName());
            image.setImage(loadTeamSprite(t, p1));
            image.setOpacity(1.0);
            if (frame != null) {
                frame.setOpacity(1.0);
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
        turnOverlay.setManaged(true);
        turnOverlay.setVisible(true);
        PauseTransition autoHide = new PauseTransition(Duration.seconds(3));
        autoHide.setOnFinished(e -> handleDismissTurnOverlay());
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
            p1.addUnit(t.createRobot(p1));
        }
        for (RobotTemplate t : player2Templates) {
            p2.addUnit(t.createRobot(p2));
        }
        game.displayScreen(GameScreen.create(game));
    }

    @Override
    public void display() {
        this.stage.setScene(this.scene);
    }

    @Override
    public void handleInput(String input) {
        // Sin uso
    }
}
