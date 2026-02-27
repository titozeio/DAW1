package com.titozeio.ui;

/**
 * Clase base abstracta para todas las pantallas del juego.
 * Cada pantalla gestiona su propia renderización y entrada.
 */
public abstract class Screen {

    /** Muestra el contenido de la pantalla en consola/interfaz. */
    public abstract void display();

    /** Procesa una entrada del usuario (por ahora en consola). */
    public abstract void handleInput(String input);
}
