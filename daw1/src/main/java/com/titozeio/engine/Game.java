package com.titozeio.engine;

import com.titozeio.ui.MainMenuScreen;
import com.titozeio.ui.Screen;
import com.titozeio.victory.VictoryCondition;
import javafx.stage.Stage;
import java.util.List;
import java.util.ArrayList;

public class Game {

    private Map map;
    private Player p1;
    private Player p2;
    private Player currentPlayer;
    private int turnCounter;
    private Screen currentScreen;
    private List<VictoryCondition> victoryConditions;

    // Referencia a la ventana de JavaFX
    private Stage stage;

    public Game(Stage stage) {
        this.stage = stage;
        this.victoryConditions = new ArrayList<>();
        this.turnCounter = 1;

        // TODO: Inicializar mapa y jugadores
    }

    public void start() {
        // Carga y muestra la pantalla inicial
        MainMenuScreen mainMenu = new MainMenuScreen(this.stage, this);
        displayScreen(mainMenu);
    }

    public void nextTurn() {
        this.turnCounter++;
        // TODO: Alternar currentPlayer
    }

    public Player checkVictory() {
        // TODO: Evaluar victoryConditions
        return null;
    }

    public void displayScreen(Screen screen) {
        this.currentScreen = screen;
        this.currentScreen.display();
    }

    public void handleInput(Object event) {
        // TODO: Procesar eventos (clics, teclas)
    }

    // Getters
    public Player getP1() {
        return p1;
    }

    public Player getP2() {
        return p2;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getTurnCounter() {
        return turnCounter;
    }

    public Stage getStage() {
        return stage;
    }
}