package com.titozeio.engine;

import com.titozeio.ui.Screen;
import com.titozeio.victory.VictoryCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase central del juego. Gestiona el estado global, los turnos,
 * las pantallas y la comprobación de condiciones de victoria.
 */
public class Game {

    private Map map;
    private Player p1;
    private Player p2;
    private Player currentPlayer;
    private int turnCounter;
    private Screen currentScreen;
    private List<VictoryCondition> victoryConditions;

    public Game(Player p1, Player p2, Map map) {
        this.p1 = p1;
        this.p2 = p2;
        this.map = map;
        this.currentPlayer = p1;
        this.turnCounter = 1;
        this.victoryConditions = new ArrayList<>();
    }

    /** Inicia el juego. */
    public void start() {
        System.out.println("=== DEVASTATION AI WARS 1 ===");
        System.out.println("Turno " + turnCounter + " - " + currentPlayer.getName());
    }

    /** Pasa al siguiente turno, alternando el jugador activo. */
    public void nextTurn() {
        // Comprobar victoria antes de pasar turno
        Player winner = checkVictory();
        if (winner != null) {
            System.out.println("¡" + winner.getName() + " ha ganado!");
            return;
        }

        // Resetear estados de acción de los robots del jugador actual
        currentPlayer.getUnits().forEach(Robot::resetActions);

        // Alternar jugador
        currentPlayer = (currentPlayer == p1) ? p2 : p1;
        if (currentPlayer == p1) {
            turnCounter++;
        }

        System.out.println("-- Turno " + turnCounter + " - " + currentPlayer.getName() + " --");
    }

    /**
     * Comprueba todas las condiciones de victoria registradas.
     * @return el jugador ganador, o null si nadie ha ganado aún.
     */
    public Player checkVictory() {
        for (VictoryCondition vc : victoryConditions) {
            Player winner = vc.check(this);
            if (winner != null) {
                return winner;
            }
        }
        return null;
    }

    /** Muestra la pantalla indicada. */
    public void displayScreen(Screen screen) {
        this.currentScreen = screen;
        screen.display();
    }

    // --- Getters y Setters ---

    public Map getMap() { return map; }
    public Player getP1() { return p1; }
    public Player getP2() { return p2; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public int getTurnCounter() { return turnCounter; }
    public Screen getCurrentScreen() { return currentScreen; }
    public List<VictoryCondition> getVictoryConditions() { return victoryConditions; }

    public void addVictoryCondition(VictoryCondition vc) {
        victoryConditions.add(vc);
    }
}
