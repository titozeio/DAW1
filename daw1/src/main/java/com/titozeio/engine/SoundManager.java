package com.titozeio.engine;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.media.AudioClip;

/**
 * Gestor centralizado de efectos de sonido (SFX).
 * Usa {@link AudioClip} (reproducción corta, no bloqueante) en lugar de
 * {@link javafx.scene.media.MediaPlayer} para que los efectos sean
 * instantáneos.
 *
 * Todos los métodos son seguros en entornos headless/sin audio: si la
 * plataforma no soporta MEDIA simplemente no hace nada.
 */
public class SoundManager {

    private static final String BASE = "/com/titozeio/sounds/sfx/";
    private static final double DEFAULT_VOLUME = 0.65;

    // ── Utilidades para botones ───────────────────────────────────────────────

    /**
     * Adjunta los tres SFX de UI (hover, click, error si deshabilitado) a un
     * {@link Button} de JavaFX.
     *
     * @param button el botón al que aplicar los sonidos
     */
    public static void attachButtonSounds(Button button) {
        if (button == null)
            return;

        // Hover
        button.setOnMouseEntered(e -> {
            play(UI_HOVER, 0.45);
            if (button.getUserData() instanceof Runnable prev)
                prev.run();
        });

        // Click: confirm si habilitado, error si deshabilitado
        button.setOnMousePressed(e -> {
            if (!button.isDisabled()) {
                play(UI_CLICK_CONFIRM);
            } else {
                play(UI_ERROR, 0.5);
            }
        });
    }

    /**
     * Recorre recursivamente todos los hijos de un {@link Parent} y llama a
     * {@link #attachButtonSounds(Button)} en cada {@link Button} encontrado.
     * Útil para inicializar toda una pantalla de una vez.
     *
     * @param root nodo raíz del que colgar los SFX
     */
    public static void attachButtonSoundsToAll(Parent root) {
        if (root == null)
            return;
        for (Node child : root.getChildrenUnmodifiable()) {
            if (child instanceof Button btn) {
                attachButtonSounds(btn);
            }
            if (child instanceof Parent p) {
                attachButtonSoundsToAll(p);
            }
        }
    }

    // ── SFX de combate ────────────────────────────────────────────────────────
    public static final String MECH_MOVE = "mech_move.wav";
    public static final String MECH_RECEIVE_HIT = "mech_receive_hit.wav";
    public static final String LASER_SHOT = "laser_shot.wav";
    public static final String PLASMA_SABER = "plasma_saber.wav";
    public static final String WEAPON_PLASMA_SABER = "weapon_plasma_saber.wav";
    public static final String WEAPON_FREEZING_RAY = "weapon_freezing_ray.wav";
    public static final String WEAPON_MISSILES = "weapon_missiles.wav";
    public static final String MISSILE_LAUNCH = "missile_launch.wav";
    public static final String REPAIR_NANOBOTS = "repair_nanobots.wav";
    public static final String MOBILITY_JUMP_THRUSTERS = "mobility_jump_thrusters.wav";
    public static final String MOBILITY_HYPERSPEED_DASH = "mobility_hyperspeed_dash.wav";

    // ── SFX de UI ─────────────────────────────────────────────────────────────
    public static final String UI_UNIT_SELECT = "ui_unit_select.wav";
    public static final String UI_CLICK_CONFIRM = "ui_click_confirm.wav";
    public static final String UI_TURN_CHANGE = "ui_turn_change.wav";
    public static final String UI_ABILITY_READY = "ui_ability_ready.wav";
    public static final String UI_ABILITY_COOLDOWN = "ui_ability_cooldown.wav";
    public static final String UI_ERROR = "ui_error.wav";
    public static final String UI_INVALID = "ui_invalid.wav";
    public static final String UI_HOVER = "ui_hover.wav";

    private static boolean sfxEnabled = true;

    /**
     * Activa o desactiva globalmente todos los efectos de sonido.
     * Se llama desde el panel de Settings del menú principal.
     */
    public static void setEnabled(boolean enabled) {
        sfxEnabled = enabled;
    }

    /** Devuelve {@code true} si los SFX están actualmente habilitados. */
    public static boolean isEnabled() {
        return sfxEnabled;
    }

    private SoundManager() {
        /* utilidad estática */ }

    /**
     * Reproduce un SFX por su nombre de archivo (sin ruta).
     *
     * @param fileName nombre del archivo, p.ej. {@code SoundManager.MECH_MOVE}
     */
    public static void play(String fileName) {
        play(fileName, DEFAULT_VOLUME);
    }

    public static void play(String fileName, double volume) {
        if (!sfxEnabled)
            return;
        try {
            var resource = SoundManager.class.getResource(BASE + fileName);
            if (resource == null) {
                System.err.println("[SoundManager] SFX no encontrado: " + BASE + fileName);
                return;
            }
            AudioClip clip = new AudioClip(resource.toExternalForm());
            clip.setVolume(Math.max(0.0, Math.min(1.0, volume)));
            clip.play();
        } catch (Throwable t) {
            // En entornos headless o sin soporte nativo de media (p.ej. CI con Monocle)
            // se puede lanzar NoSuchMethodError u otros Error no recuperables;
            // los ignoramos para no romper los tests.
            System.err.println("[SoundManager] No se pudo reproducir " + fileName + ": " + t.getMessage());
        }
    }
}
