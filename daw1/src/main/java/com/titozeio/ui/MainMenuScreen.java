package com.titozeio.ui;

/**
 * Pantalla de inicio del juego.
 * Muestra el título "Devastation Ai Wars 1" y el botón de "Jugar".
 */
public class MainMenuScreen extends Screen {

    @Override
    public void display() {
        System.out.println("=====================================");
        System.out.println("   DEVASTATION AI WARS 1");
        System.out.println("=====================================");
        System.out.println("[1] Jugar");
        System.out.println("[0] Salir");
    }

    @Override
    public void handleInput(String input) {
        switch (input.trim()) {
            case "1":
                System.out.println("Iniciando nueva partida...");
                break;
            case "0":
                System.out.println("Saliendo...");
                System.exit(0);
                break;
            default:
                System.out.println("Opción no válida.");
        }
    }
}
