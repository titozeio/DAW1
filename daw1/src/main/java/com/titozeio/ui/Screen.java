package com.titozeio.ui;

import java.io.File;
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
        try {
            File cssFile = new File("assets/style.css");
            if (cssFile.exists()) {
                scene.getStylesheets().add(cssFile.toURI().toURL().toString());
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el CSS global: " + e.getMessage());
        }
    }
}
