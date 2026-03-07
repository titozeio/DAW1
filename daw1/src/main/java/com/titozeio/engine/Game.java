package com.titozeio.engine;

import com.titozeio.ui.MainMenuScreen;
import com.titozeio.ui.GameScreen;
import com.titozeio.ui.Screen;
import com.titozeio.victory.BaseCaptureVC;
import com.titozeio.victory.EliminationVC;
import com.titozeio.victory.VictoryCondition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.stage.Stage;
import java.util.List;
import java.util.ArrayList;

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

    public Game(Stage stage) {
        this.stage = stage;
        this.victoryConditions = new ArrayList<>();
        this.turnCounter = 1;

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

    public void start() {
        displayScreen(MainMenuScreen.create(stage, this));
    }

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

    public void displayScreen(Screen screen) {
        updateMusicForScreen(screen);
        this.currentScreen = screen;
        this.currentScreen.display();
    }

    private void updateMusicForScreen(Screen screen) {
        MusicTheme targetTheme = (screen instanceof GameScreen) ? MusicTheme.BATTLE : MusicTheme.MENU;
        if (mediaPlayer != null && currentMusicTheme == targetTheme) {
            return;
        }
        playThemeTrack(targetTheme, 1);
    }

    private void playThemeTrack(MusicTheme theme, int trackNumber) {
        stopCurrentMusic();

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
        } catch (Exception e) {
            System.err.println("Error al reproducir música (" + path + "): " + e.getMessage());
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
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    public void handleInput(Object event) {
        // TODO: Procesar eventos (clics, teclas)
    }

    // Getters y Setters
    public Player getP1() {
        return p1;
    }

    public Player getP2() {
        return p2;
    }

    public void setP1(Player p1) {
        this.p1 = p1;
    }

    public void setP2(Player p2) {
        this.p2 = p2;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getTurnCounter() {
        return turnCounter;
    }

    public Map getMap() {
        return map;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public Stage getStage() {
        return stage;
    }
}
