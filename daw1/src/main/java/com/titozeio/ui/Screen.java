package com.titozeio.ui;

import javafx.scene.Scene;

/**
 * Clase base abstracta para todas las pantallas del juego.
 * Cada pantalla gestiona su propia renderización y entrada.
 */
public abstract class Screen {

    /** Muestra el contenido de la pantalla en consola/interfaz. */
    public abstract void display();

    /** Procesa una entrada del usuario (por ahora en consola). */
    public abstract void handleInput(String input);

    /** Aplica el estilo CSS global a la escena proporcionada. */
    protected void applyGlobalStyle(Scene scene) {
        String cssPath = "/com/titozeio/ui/style.css";
        var resource = getClass().getResource(cssPath);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        } else {
            System.err.println("No se encontró el archivo CSS global: " + cssPath);
        }
    }
}
