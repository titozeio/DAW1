package com.titozeio.ui;

/**
 * Pantalla de pausa. Ofrece las opciones Reanudar y Rendirse.
 */
public class PauseScreen extends Screen {

    @Override
    public void display() {
        System.out.println("--- PAUSA ---");
        System.out.println("[1] Reanudar");
        System.out.println("[2] Rendirse");
    }

    @Override
    public void handleInput(String input) {
        switch (input.trim()) {
            case "1":
                System.out.println("Reanudando juego...");
                break;
            case "2":
                System.out.println("El jugador se ha rendido. Victoria del oponente.");
                break;
            default:
                System.out.println("Opción no válida.");
        }
    }
}
