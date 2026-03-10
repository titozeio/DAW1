package com.titozeio.engine;

import com.titozeio.ui.MainMenuScreen;
import com.titozeio.ui.GameScreen;
import com.titozeio.ui.Screen;
import com.titozeio.victory.BaseCaptureVC;
import com.titozeio.victory.EliminationVC;
import com.titozeio.victory.VictoryCondition;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.stage.Stage;
import java.util.List;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class Game {

    private Map map;
    private Player p1;
    private Player p2;
    private Player currentPlayer;
    private int turnCounter;
    private GamePhase phase;
    private Screen currentScreen;
    private List<VictoryCondition> victoryConditions;
    private MediaPlayer mediaPlayer;
    private Timeline volumeFadeTimeline;
    private PauseTransition fadeOutTrigger;
    private MusicTheme currentMusicTheme;
    private int currentTrackNumber = 1;
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;
    private static final String PREF_MUSIC_ENABLED = "musicEnabled";
    private static final String PREF_SFX_ENABLED = "sfxEnabled";
    private static final Preferences PREFS = Preferences.userNodeForPackage(Game.class);

    // Referencia a la ventana de JavaFX
    private Stage stage;

    private enum MusicTheme {
        MENU("menu"),
        BATTLE("battle");

        private final String prefix;

        MusicTheme(String prefix) {
            this.prefix = prefix;
        }
    }

    /**
     * Inicializa el núcleo del juego, registrando jugadores, condiciones de
     * victoria y el mapa inicial.
     * 
     * @param stage La ventana principal de JavaFX.
     */
    public Game(Stage stage) {
        this.stage = stage;
        this.victoryConditions = new ArrayList<>();
        this.turnCounter = 1;
        this.musicEnabled = PREFS.getBoolean(PREF_MUSIC_ENABLED, true);
        this.sfxEnabled = PREFS.getBoolean(PREF_SFX_ENABLED, true);
        SoundManager.setEnabled(this.sfxEnabled);

        // Crear los jugadores sin robots (los robots se asignan en
        // RobotSelectionScreen)
        this.p1 = new Player("Jugador 1");
        this.p2 = new Player("Jugador 2");
        this.currentPlayer = p1; // J1 tiene el primer turno de combate
        this.phase = GamePhase.DEPLOYING;

        // Registrar condiciones de victoria del GDD
        victoryConditions.add(new EliminationVC());
        victoryConditions.add(new BaseCaptureVC()); // 2 turnos en la base enemiga

        // Crear el mapa inicial
        this.map = MapFactory.createMap1();
    }

    /**
     * Inicia la logica asociada al metodo.
     */
    public void start() {
        displayScreen(MainMenuScreen.create(stage, this));
    }

    /**
     * Resetea el estado de la partida para comenzar una nueva desde cero.
     */
    public void resetForNewMatch() {
        this.turnCounter = 1;
        this.currentPlayer = p1;
        this.phase = GamePhase.DEPLOYING;
        this.map = MapFactory.createMap1();
        if (p1 != null) {
            p1.getUnits().clear();
            p1.setBaseLocation(null);
            p1.resetBaseControlTurns();
        }
        if (p2 != null) {
            p2.getUnits().clear();
            p2.setBaseLocation(null);
            p2.resetBaseControlTurns();
        }
    }

    /**
     * Avanza al siguiente estado de turn.
     */
    public void nextTurn() {
        this.turnCounter++;
        // Alternar jugador activo
        this.currentPlayer = (this.currentPlayer == p1) ? p2 : p1;
    }

    /**
     * Evalúa todas las condiciones de victoria del juego.
     * 
     * @return el jugador ganador si alguna condición se cumple, null en caso
     *         contrario.
     */
    public Player checkVictory() {
        for (VictoryCondition vc : victoryConditions) {
            Player winner = vc.check(this);
            if (winner != null)
                return winner;
        }
        return null;
    }

    /** Devuelve el jugador contrario al indicado. */
    public Player getOpponent(Player player) {
        return (player == p1) ? p2 : p1;
    }

    /**
     * Navega a la pantalla indicada con una animación de transición
     * fade-out → cambio de escena → fade-in.
     * Si el stage no tiene escena activa (p.ej., primer arranque o entorno
     * headless)
     * la transición es inmediata.
     */
    public void displayScreen(Screen screen) {
        updateMusicForScreen(screen);

        // Sin stage disponible → transición inmediata (tests headless)
        if (stage == null) {
            this.currentScreen = screen;
            screen.display();
            return;
        }

        Scene currentScene = stage.getScene();

        // Si no hay escena activa, mostrar directamente (primer inicio)
        if (currentScene == null) {
            this.currentScreen = screen;
            screen.display();
            return;
        }

        // ── Fade-out ──────────────────────────────────────────────────────────
        // Superponemos un Rectangle negro sobre el StackPane raíz de la escena
        // actual y lo animamos a opacidad 1.0 (negro total).
        javafx.scene.Node root = currentScene.getRoot();
        Rectangle overlay = null;

        if (root instanceof StackPane sp) {
            overlay = new Rectangle();
            overlay.setFill(Color.BLACK);
            overlay.setMouseTransparent(true);
            overlay.setOpacity(0.0);
            // Vincular tamaño al StackPane
            overlay.widthProperty().bind(sp.widthProperty());
            overlay.heightProperty().bind(sp.heightProperty());
            sp.getChildren().add(overlay);
        }

        final Rectangle finalOverlay = overlay;

        if (finalOverlay != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), finalOverlay);
            fadeOut.setFromValue(0.0);
            fadeOut.setToValue(1.0);
            fadeOut.setOnFinished(e -> {
                // ── Cambio de escena ────────────────────────────────────────
                this.currentScreen = screen;
                screen.display();
                // ── Fade-in ─────────────────────────────────────────────────
                // El overlay ya no existe en la nueva escena; hacemos fade-in
                // en el nuevo root.
                Scene newScene = stage.getScene();
                if (newScene != null) {
                    javafx.scene.Node newRoot = newScene.getRoot();
                    newRoot.setOpacity(0.0);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(400), newRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                }
            });
            fadeOut.play();
        } else {
            // Raíz no es StackPane → fade-in simple sin fade-out
            this.currentScreen = screen;
            screen.display();
            Scene newScene = stage.getScene();
            if (newScene != null) {
                javafx.scene.Node newRoot = newScene.getRoot();
                newRoot.setOpacity(0.0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), newRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        }
    }

    private void updateMusicForScreen(Screen screen) {
        if (!musicEnabled) {
            stopCurrentMusic();
            return;
        }

        MusicTheme targetTheme = (screen instanceof GameScreen) ? MusicTheme.BATTLE : MusicTheme.MENU;
        if (mediaPlayer != null && currentMusicTheme == targetTheme) {
            return;
        }
        playThemeTrack(targetTheme, 1);
    }

    private void playThemeTrack(MusicTheme theme, int trackNumber) {
        stopCurrentMusic();

        // Sin stage no intentamos reproducir.
        if (stage == null) {
            return;
        }

        String path = getTrackPath(theme, trackNumber);
        var resource = getClass().getResource(path);
        if (resource == null) {
            if (trackNumber != 1) {
                playThemeTrack(theme, 1);
            } else {
                System.err.println("No se encontró música para el tema '" + theme.prefix + "' en: " + path);
            }
            return;
        }

        try {
            Media media = new Media(resource.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            currentMusicTheme = theme;
            currentTrackNumber = trackNumber;

            mediaPlayer.setOnReady(() -> {
                mediaPlayer.setVolume(0.0);
                mediaPlayer.play();
                fadeVolume(0.0, 1.0, Duration.seconds(2));
                scheduleFadeOutBeforeEnd();
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                int next = resolveNextTrackNumber(theme, currentTrackNumber);
                playThemeTrack(theme, next);
            });
        } catch (Throwable t) {
            System.err.println("Error al reproducir música (" + path + "): " + t.getMessage());
        }
    }

    private int resolveNextTrackNumber(MusicTheme theme, int currentNumber) {
        int next = currentNumber + 1;
        String nextPath = getTrackPath(theme, next);
        return getClass().getResource(nextPath) != null ? next : 1;
    }

    private String getTrackPath(MusicTheme theme, int trackNumber) {
        return "/com/titozeio/sounds/" + theme.prefix + trackNumber + ".mp3";
    }

    private void scheduleFadeOutBeforeEnd() {
        if (mediaPlayer == null) {
            return;
        }
        Duration total = mediaPlayer.getTotalDuration();
        Duration fadeDuration = Duration.seconds(2);
        if (total == null || total.isUnknown() || total.lessThanOrEqualTo(fadeDuration)) {
            return;
        }
        fadeOutTrigger = new PauseTransition(total.subtract(fadeDuration));
        fadeOutTrigger.setOnFinished(e -> fadeVolume(mediaPlayer.getVolume(), 0.0, fadeDuration));
        fadeOutTrigger.play();
    }

    private void fadeVolume(double from, double to, Duration duration) {
        if (mediaPlayer == null) {
            return;
        }
        if (volumeFadeTimeline != null) {
            volumeFadeTimeline.stop();
        }
        volumeFadeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(mediaPlayer.volumeProperty(), from)),
                new KeyFrame(duration, new KeyValue(mediaPlayer.volumeProperty(), to)));
        volumeFadeTimeline.play();
    }

    private void stopCurrentMusic() {
        if (fadeOutTrigger != null) {
            fadeOutTrigger.stop();
            fadeOutTrigger = null;
        }
        if (volumeFadeTimeline != null) {
            volumeFadeTimeline.stop();
            volumeFadeTimeline = null;
        }
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }
    }

    // Getters y Setters
    /**
     * Obtiene p1.
     * 
     * @return valor resultante de la operacion.
     */
    public Player getP1() {
        return p1;
    }

    /**
     * Obtiene p2.
     * 
     * @return valor resultante de la operacion.
     */
    public Player getP2() {
        return p2;
    }

    /**
     * Actualiza p1.
     * 
     * @param p1 parametro de entrada.
     */
    public void setP1(Player p1) {
        this.p1 = p1;
    }

    /**
     * Actualiza p2.
     * 
     * @param p2 parametro de entrada.
     */
    public void setP2(Player p2) {
        this.p2 = p2;
    }

    /**
     * Obtiene current player.
     * 
     * @return valor resultante de la operacion.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Obtiene turn counter.
     * 
     * @return valor resultante de la operacion.
     */
    public int getTurnCounter() {
        return turnCounter;
    }

    /**
     * Obtiene map.
     * 
     * @return valor resultante de la operacion.
     */
    public Map getMap() {
        return map;
    }

    /**
     * Obtiene phase.
     * 
     * @return valor resultante de la operacion.
     */
    public GamePhase getPhase() {
        return phase;
    }

    /**
     * Actualiza phase.
     * 
     * @param phase parametro de entrada.
     */
    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    /**
     * Obtiene stage.
     * 
     * @return valor resultante de la operacion.
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Indica si music enabled.
     * 
     * @return true si la condicion evaluada se cumple; false en caso contrario.
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    /**
     * Actualiza music enabled.
     * 
     * @param musicEnabled parametro de entrada.
     */
    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
        PREFS.putBoolean(PREF_MUSIC_ENABLED, musicEnabled);
        if (!musicEnabled) {
            stopCurrentMusic();
            return;
        }
        if (currentScreen != null) {
            updateMusicForScreen(currentScreen);
        }
    }

    /**
     * Indica si sfx enabled.
     * 
     * @return true si la condicion evaluada se cumple; false en caso contrario.
     */
    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    /**
     * Actualiza sfx enabled.
     * 
     * @param sfxEnabled parametro de entrada.
     */
    public void setSfxEnabled(boolean sfxEnabled) {
        this.sfxEnabled = sfxEnabled;
        PREFS.putBoolean(PREF_SFX_ENABLED, sfxEnabled);
        SoundManager.setEnabled(sfxEnabled);
    }
}
