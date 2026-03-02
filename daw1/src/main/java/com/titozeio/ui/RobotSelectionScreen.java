package com.titozeio.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.titozeio.engine.Game;
import com.titozeio.engine.Robot;
import com.titozeio.enums.RobotTemplate;
import com.titozeio.model.Weapon;
import com.titozeio.skills.Skill;

/**
 * Controlador de RobotSelectionScreen.fxml.
 *
 * Gestiona el turno de selección de robots:
 * 1. Carga una tarjeta RobotCard.fxml por cada template disponible.
 * 2. El jugador activo hace clic en una tarjeta → se resalta,
 * aparecen las descripciones y se activa el botón Confirmar.
 * 3. Puede explorar otras tarjetas sin confirmar.
 * 4. Al pulsar Confirmar, se registra la elección y se pasa el turno.
 * 5. Cuando cada jugador tiene MAX_ROBOTS_PER_PLAYER robots → combat.
 */
public class RobotSelectionScreen extends Screen {

    private static final int MAX_ROBOTS_PER_PLAYER = 3;

    // ── Referencias FXML ──────────────────────────────────────────────────────
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
    private Button confirmButton;

    // ── Estado interno ────────────────────────────────────────────────────────
    private List<RobotTemplate> availableTemplates;
    private List<RobotTemplate> player1Templates;
    private List<RobotTemplate> player2Templates;
    private boolean isPlayer2Turn;

    /** Template sobre el que el jugador hizo clic pero aún no confirmó. */
    private RobotTemplate pendingTemplate;
    /** Controlador de la tarjeta actualmente resaltada. */
    private RobotCardController highlightedCardCtrl;

    private Stage stage;
    private Game game;
    private Scene scene;

    // ── Constructor vacío para FXMLLoader ────────────────────────────────────
    public RobotSelectionScreen() {
    }

    // ── Factory ───────────────────────────────────────────────────────────────
    public static RobotSelectionScreen create(Stage window, Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    RobotSelectionScreen.class.getResource(
                            "/com/titozeio/ui/RobotSelectionScreen.fxml"));
            Parent root = loader.load();
            RobotSelectionScreen ctrl = loader.getController();
            ctrl.stage = window;
            ctrl.game = game;
            ctrl.scene = new Scene(root, 1280, 720);
            ctrl.applyGlobalStyle(ctrl.scene);

            ctrl.initializeData(new ArrayList<>(Arrays.asList(RobotTemplate.values())));
            ctrl.refreshRobotCards();
            ctrl.updateTurnLabel();

            return ctrl;
        } catch (IOException e) {
            System.err.println("Error al cargar RobotSelectionScreen.fxml: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ── Inicialización interna ────────────────────────────────────────────────
    private void initializeData(List<RobotTemplate> allTemplates) {
        this.availableTemplates = new ArrayList<>(allTemplates);
        this.player1Templates = new ArrayList<>();
        this.player2Templates = new ArrayList<>();
        this.isPlayer2Turn = true;
        this.pendingTemplate = null;
        this.highlightedCardCtrl = null;
    }

    // ── Generación de tarjetas ────────────────────────────────────────────────

    /**
     * Limpia el panel y carga una tarjeta RobotCard.fxml
     * por cada template disponible, rellenándola con sus datos.
     */
    private void refreshRobotCards() {
        availableRobotsPane.getChildren().clear();
        pendingTemplate = null;
        highlightedCardCtrl = null;
        descriptionPane.setVisible(false);
        confirmButton.setDisable(true);
        confirmButton.setOpacity(0.35);

        for (RobotTemplate template : availableTemplates) {
            try {
                FXMLLoader cardLoader = new FXMLLoader(
                        getClass().getResource("/com/titozeio/ui/RobotCard.fxml"));
                FlowPane cardNode = cardLoader.load();
                RobotCardController cardCtrl = cardLoader.getController();

                // Rellenar con datos reales
                cardCtrl.setData(template);

                // Registrar callback: clic en tarjeta → onCardClicked
                cardCtrl.setOnSelect(t -> onCardClicked(t, cardCtrl));

                // Conectar el clic al nodo raíz de la tarjeta
                cardNode.setOnMouseClicked(e -> cardCtrl.handleClick());

                availableRobotsPane.getChildren().add(cardNode);

            } catch (IOException e) {
                System.err.println("Error al cargar RobotCard.fxml: " + e.getMessage());
            }
        }
    }

    // ── Lógica de selección ───────────────────────────────────────────────────

    /**
     * Se ejecuta cuando el jugador hace clic en una tarjeta.
     * Resalta la tarjeta, muestra descripciones y activa Confirmar.
     * NO registra la elección todavía.
     */
    private void onCardClicked(RobotTemplate template, RobotCardController cardCtrl) {
        // Quitar resalte anterior
        if (highlightedCardCtrl != null) {
            highlightedCardCtrl.setHighlighted(false);
        }

        // Resaltar la nueva tarjeta
        cardCtrl.setHighlighted(true);
        highlightedCardCtrl = cardCtrl;
        pendingTemplate = template;

        // Poblar panel de descripciones con datos reales desde el enum
        Robot sample = template.createRobot(null);
        Weapon weapon = sample.getWeapon();
        Skill skill = sample.getSkill();

        descRobotText.setText(sample.getDescription());
        descWeaponText.setText(
                weapon.getName() + ": " + weapon.getDescription()
                        + " (Alcance " + weapon.getRange()
                        + ", Daño " + weapon.getDamage() + ")");
        descSkillText.setText(
                skill != null
                        ? skill.getName() + ": " + skill.getDescription()
                                + " (Cooldown: " + skill.getCooldown() + " turnos)"
                        : "Sin skill");

        // Mostrar panel y activar botón
        descriptionPane.setVisible(true);
        confirmButton.setDisable(false);
        confirmButton.setOpacity(1.0);
    }

    /**
     * Se ejecuta al pulsar el botón "Confirmar".
     * Registra la selección del jugador activo y pasa el turno.
     */
    @FXML
    public void handleConfirmAction() {
        if (pendingTemplate == null)
            return;

        if (isPlayer2Turn) {
            player2Templates.add(pendingTemplate);
            System.out.println("Jugador 2 elige: " + pendingTemplate);
        } else {
            player1Templates.add(pendingTemplate);
            System.out.println("Jugador 1 elige: " + pendingTemplate);
        }

        availableTemplates.remove(pendingTemplate);

        if (isSelectionComplete()) {
            transitionToCombatScreen();
        } else {
            isPlayer2Turn = !isPlayer2Turn;
            refreshRobotCards();
            updateTurnLabel();
        }
    }

    private boolean isSelectionComplete() {
        return player1Templates.size() == MAX_ROBOTS_PER_PLAYER
                && player2Templates.size() == MAX_ROBOTS_PER_PLAYER;
    }

    private void updateTurnLabel() {
        if (turnInstructionsLabel == null)
            return;
        String jugador = isPlayer2Turn ? "Jugador 2" : "Jugador 1";
        int elegidos = isPlayer2Turn ? player2Templates.size() : player1Templates.size();
        turnInstructionsLabel.setText(
                jugador + ", elige robot (" + elegidos + "/" + MAX_ROBOTS_PER_PLAYER + " elegidos)");
    }

    private void transitionToCombatScreen() {
        System.out.println("Selección completada.");
        System.out.println("J1: " + player1Templates);
        System.out.println("J2: " + player2Templates);
        // TODO: cuando Game tenga Players inicializados, crear los robots reales aquí:
        // Player p1 = game.getP1();
        // Player p2 = game.getP2();
        // List<Robot> robotsP1 = player1Templates.stream().map(t ->
        // t.createRobot(p1)).toList();
        // List<Robot> robotsP2 = player2Templates.stream().map(t ->
        // t.createRobot(p2)).toList();
        // game.getP1().setRobots(robotsP1);
        // game.getP2().setRobots(robotsP2);
        game.displayScreen(new GameScreen(game));
    }

    // ── Screen ────────────────────────────────────────────────────────────────
    @Override
    public void display() {
        System.out.println("Mostrando RobotSelectionScreen...");
        this.stage.setScene(this.scene);
    }

    @Override
    public void handleInput(String input) {
        System.out.println("Procesando entrada en RobotSelectionScreen: " + input);
    }
}