package com.titozeio.engine;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.media.AudioClip;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestor centralizado de efectos de sonido (SFX).
 * Implementado como Singleton con caché interna de {@link AudioClip} para
 * optimizar el rendimiento.
 */
public class SoundManager {

    private static final String BASE = "/com/titozeio/sounds/sfx/";
    private static final double DEFAULT_VOLUME = 0.65;

    // Instancia única (Singleton)
    private static SoundManager instance;

    // Caché de clips para no recargar archivos constantemente
    private final Map<String, AudioClip> clipCache;
    private boolean sfxEnabled = true;

    /**
     * Obtiene la instancia única del gestor de sonidos.
     * 
     * @return La instancia de SoundManager.
     */
    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private SoundManager() {
        this.clipCache = new HashMap<>();
    }

    // ── Métodos de Instancia ──────────────────────────────────────────────────

    /**
     * Reproduce un efecto de sonido cargándolo desde la caché si es posible.
     */
    public void playSfx(String fileName, double volume) {
        if (!sfxEnabled)
            return;

        try {
            AudioClip clip = clipCache.get(fileName);

            if (clip == null) {
                var resource = SoundManager.class.getResource(BASE + fileName);
                if (resource == null) {
                    System.err.println("[SoundManager] SFX no encontrado: " + BASE + fileName);
                    return;
                }
                clip = new AudioClip(resource.toExternalForm());
                clipCache.put(fileName, clip);
            }

            clip.setVolume(Math.max(0.0, Math.min(1.0, volume)));
            clip.play();
        } catch (Throwable t) {
            System.err.println("[SoundManager] Error al reproducir " + fileName + ": " + t.getMessage());
        }
    }

    /**
     * Limpia la memoria liberando los clips cargados.
     */
    public void clearCache() {
        clipCache.clear();
    }

    // ── Métodos Estáticos (Compatibilidad y Utilidad) ─────────────────────────

    /**
     * Reproduce un SFX (atenuador estático para compatibilidad).
     */
    public static void play(String fileName) {
        getInstance().playSfx(fileName, DEFAULT_VOLUME);
    }

    public static void play(String fileName, double volume) {
        getInstance().playSfx(fileName, volume);
    }

    public static void setEnabled(boolean enabled) {
        getInstance().sfxEnabled = enabled;
    }

    public static boolean isEnabled() {
        return getInstance().sfxEnabled;
    }

    /**
     * Adjunta los tres SFX de UI a un botón.
     */
    public static void attachButtonSounds(Button button) {
        if (button == null)
            return;

        button.setOnMouseEntered(e -> {
            play(UI_HOVER, 0.45);
        });

        button.setOnMousePressed(e -> {
            if (!button.isDisabled()) {
                play(UI_CLICK_CONFIRM);
            } else {
                play(UI_ERROR, 0.5);
            }
        });
    }

    /**
     * Aplica sonidos a todos los botones de un contenedor.
     */
    public static void attachButtonSoundsToAll(Parent root) {
        if (root == null)
            return;
        for (Node child : root.getChildrenUnmodifiable()) {
            if (child instanceof Button btn) {
                attachButtonSounds(btn);
            } else if (child instanceof Parent p) {
                attachButtonSoundsToAll(p);
            }
        }
    }

    // ── Constantes de SFX ─────────────────────────────────────────────────────
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

    public static final String UI_UNIT_SELECT = "ui_unit_select.wav";
    public static final String UI_CLICK_CONFIRM = "ui_click_confirm.wav";
    public static final String UI_TURN_CHANGE = "ui_turn_change.wav";
    public static final String UI_ABILITY_READY = "ui_ability_ready.wav";
    public static final String UI_ABILITY_COOLDOWN = "ui_ability_cooldown.wav";
    public static final String UI_ERROR = "ui_error.wav";
    public static final String UI_INVALID = "ui_invalid.wav";
    public static final String UI_HOVER = "ui_hover.wav";
}
