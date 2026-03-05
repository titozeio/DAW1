package com.titozeio.engine;

import com.titozeio.ui.MainMenuScreen;
import com.titozeio.ui.Screen;
import com.titozeio.victory.BaseCaptureVC;
import com.titozeio.victory.EliminationVC;
import com.titozeio.victory.VictoryCondition;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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

    // Referencia a la ventana de JavaFX
    private Stage stage;

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
        startMusic();
        // Carga y muestra la pantalla inicial
        MainMenuScreen mainMenu = MainMenuScreen.create(this.stage, this);
        displayScreen(mainMenu);
    }

    private void startMusic() {
        try {
            var resource = getClass().getResource("/com/titozeio/sounds/song.mp3");
            if (resource != null) {
                Media media = new Media(resource.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.play();
                System.out.println("Música iniciada correctamente.");
            } else {
                System.err.println("No se encontró el archivo de música: /com/titozeio/sounds/song.mp3");
            }
        } catch (Exception e) {
            System.err.println("Error al reproducir música: " + e.getMessage());
        }
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
        this.currentScreen = screen;
        this.currentScreen.display();
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