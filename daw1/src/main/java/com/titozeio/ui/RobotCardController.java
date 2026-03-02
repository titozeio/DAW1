package com.titozeio.ui;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import com.titozeio.engine.Robot;
import com.titozeio.enums.RobotTemplate;
import com.titozeio.model.Weapon;
import com.titozeio.skills.Skill;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Controlador de RobotCard.fxml.
 *
 * Cada tarjeta de robot en la pantalla de selección es una instancia
 * independiente de este controlador. El método setData() la rellena
 * con los datos del RobotTemplate correspondiente.
 *
 * El callback onSelect se llama cuando el jugador hace clic en la tarjeta.
 */
public class RobotCardController {

    /** Mapa de imágenes por template. Añade aquí nuevos robots. */
    private static final Map<RobotTemplate, String> ROBOT_IMAGES = new EnumMap<>(RobotTemplate.class);
    static {
        ROBOT_IMAGES.put(RobotTemplate.VICTORY_SABER, "SaberPrime1.jpg");
        ROBOT_IMAGES.put(RobotTemplate.BULLSEYE, "Bullseye1.jpg");
        ROBOT_IMAGES.put(RobotTemplate.BULWARK, "Bullwark1.jpg");
        ROBOT_IMAGES.put(RobotTemplate.SCOUT, "Scout1.jpg");
        ROBOT_IMAGES.put(RobotTemplate.DEATH_KNIGHT, "DeathKnight1.jpg");
        ROBOT_IMAGES.put(RobotTemplate.ICE_AGE, "IceAge1.jpg");
    }

    // ── Referencias FXML (inyectadas automáticamente) ─────────────────────────
    @FXML
    private FlowPane cardRoot; // nodo raíz de la tarjeta
    @FXML
    private Text robotNameText;
    @FXML
    private ImageView robotImageView;
    @FXML
    private Text hpText;
    @FXML
    private Text weaponNameText;
    @FXML
    private Text weaponRangeText;
    @FXML
    private Text weaponDamageText;
    @FXML
    private Text skillNameText;
    @FXML
    private Text skillCooldownText;
    @FXML
    private TextFlow skillStat1Row;
    @FXML
    private Text skillStat1Label;
    @FXML
    private Text skillStat1Value;
    @FXML
    private TextFlow skillStat2Row;
    @FXML
    private Text skillStat2Label;
    @FXML
    private Text skillStat2Value;

    // ── Estado interno ────────────────────────────────────────────────────────
    private RobotTemplate template;
    private Consumer<RobotTemplate> onSelect;

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Rellena la tarjeta con los datos del template dado.
     * Debe llamarse justo después de cargar el FXML.
     *
     * @param template el robot a representar
     */
    public void setData(RobotTemplate template) {
        this.template = template;

        // Crear robot de muestra sin owner para leer stats
        Robot sample = template.createRobot(null);
        Weapon weapon = sample.getWeapon();
        Skill skill = sample.getSkill();

        // Nombre
        robotNameText.setText(sample.getModelName());

        // Imagen
        String imgName = ROBOT_IMAGES.get(template);
        if (imgName != null) {
            var res = getClass().getResource("/com/titozeio/images/" + imgName);
            if (res != null) {
                robotImageView.setImage(new Image(res.toExternalForm()));
            }
        }

        // Stats
        hpText.setText(String.valueOf(sample.getMaxHp()));

        weaponNameText.setText(weapon.getName());
        weaponRangeText.setText(String.valueOf(weapon.getRange()));
        weaponDamageText.setText(String.valueOf(weapon.getDamage()));

        if (skill != null) {
            skillNameText.setText(skill.getName());
            skillCooldownText.setText(skill.getCooldown() + " turnos");

            // Rellenar stats específicos (Alcance, Daño, Movimiento...)
            java.util.List<String[]> stats = skill.getDisplayStats();
            if (stats.size() >= 1) {
                skillStat1Label.setText(stats.get(0)[0]);
                skillStat1Value.setText(stats.get(0)[1]);
                skillStat1Row.setVisible(true);
                skillStat1Row.setManaged(true);
            } else {
                skillStat1Row.setVisible(false);
                skillStat1Row.setManaged(false);
            }
            if (stats.size() >= 2) {
                skillStat2Label.setText(stats.get(1)[0]);
                skillStat2Value.setText(stats.get(1)[1]);
                skillStat2Row.setVisible(true);
                skillStat2Row.setManaged(true);
            } else {
                skillStat2Row.setVisible(false);
                skillStat2Row.setManaged(false);
            }
        } else {
            skillNameText.setText("Sin skill");
            skillCooldownText.setText("—");
            skillStat1Row.setVisible(false);
            skillStat1Row.setManaged(false);
            skillStat2Row.setVisible(false);
            skillStat2Row.setManaged(false);
        }
    }

    /**
     * Registra el callback que se ejecutará cuando el jugador haga clic
     * en esta tarjeta. Lo conecta RobotSelectionScreen.
     *
     * @param callback función que recibe el template seleccionado
     */
    public void setOnSelect(Consumer<RobotTemplate> callback) {
        this.onSelect = callback;
    }

    /**
     * Devuelve el nodo raíz (FlowPane) de la tarjeta.
     */
    public FlowPane getRoot() {
        return cardRoot;
    }

    /**
     * Resalta visualmente esta tarjeta (borde verde).
     * Llamado por RobotSelectionScreen cuando el jugador hace clic.
     */
    public void setHighlighted(boolean highlighted) {
        FlowPane root = getRoot();
        String base = "-fx-background-color: rgba(183, 185, 212, 0.8); -fx-background-radius: 6; -fx-cursor: hand;";
        if (highlighted) {
            root.setStyle(base + " -fx-border-color: #2ecc71; -fx-border-width: 3; -fx-border-radius: 6;");
        } else {
            root.setStyle(base);
        }
    }

    // ── Handler FXML ──────────────────────────────────────────────────────────

    /**
     * Se llama cuando el usuario hace clic en la tarjeta.
     * El clic se captura en el FlowPane raíz via onMouseClicked en el FXML,
     * o bien se conecta programáticamente desde RobotSelectionScreen.
     */
    public void handleClick() {
        if (onSelect != null) {
            onSelect.accept(template);
        }
    }

    public RobotTemplate getTemplate() {
        return template;
    }
}
