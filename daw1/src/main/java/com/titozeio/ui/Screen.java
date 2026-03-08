package com.titozeio.ui;

import com.titozeio.engine.SoundManager;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Clase base abstracta para todas las pantallas del juego.
 * Cada pantalla gestiona su propia renderización y entrada.
 */
public abstract class Screen {

    private static boolean animationsEnabled = true;

    public static void setAnimationsEnabled(boolean enabled) {
        animationsEnabled = enabled;
    }

    public static boolean areAnimationsEnabled() {
        return animationsEnabled;
    }

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

    /**
     * Adjunta SFX de hover, click y error a todos los botones del árbol dado.
     * Llama a este método desde {@code initialize()} de cada pantalla pasando
     * el nodo raíz cargado por FXML.
     */
    protected void initButtonSounds(Parent root) {
        SoundManager.attachButtonSoundsToAll(root);
    }

    /**
     * Fuerza mayúsculas en toda la UI para nodos Labeled/Text actuales y futuros.
     */
    protected void enforceUppercaseText(Parent root) {
        root.lookupAll("*").forEach(node -> {
            if (node instanceof Labeled labeled) {
                String t = labeled.getText();
                if (t != null) {
                    labeled.setText(t.toUpperCase());
                }
                labeled.textProperty().addListener((obs, oldV, newV) -> {
                    if (newV != null) {
                        String up = newV.toUpperCase();
                        if (!up.equals(newV)) {
                            labeled.setText(up);
                        }
                    }
                });
            } else if (node instanceof Text textNode) {
                String t = textNode.getText();
                if (t != null) {
                    textNode.setText(t.toUpperCase());
                }
                textNode.textProperty().addListener((obs, oldV, newV) -> {
                    if (newV != null) {
                        String up = newV.toUpperCase();
                        if (!up.equals(newV)) {
                            textNode.setText(up);
                        }
                    }
                });
            }
        });
    }

    /**
     * Muestra {@code node} con una animación de fade-in.
     * Pone el nodo visible (y managed) antes de empezar la animación.
     *
     * @param node   El nodo JavaFX a mostrar (normalmente un StackPane overlay).
     * @param millis Duración del fade en milisegundos.
     */
    protected static void fadeInNode(Node node, double millis) {
        if (node == null)
            return;
        if (!animationsEnabled) {
            node.setOpacity(1.0);
            node.setVisible(true);
            node.setManaged(true);
            return;
        }
        node.setOpacity(0.0);
        node.setVisible(true);
        node.setManaged(true);
        FadeTransition ft = new FadeTransition(Duration.millis(millis), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    /**
     * Oculta {@code node} con una animación de fade-out.
     * Al terminar, pone el nodo invisible y no managed.
     *
     * @param node   El nodo JavaFX a ocultar.
     * @param millis Duración del fade en milisegundos.
     */
    protected static void fadeOutNode(Node node, double millis) {
        if (node == null)
            return;
        if (!animationsEnabled) {
            node.setOpacity(0.0);
            node.setVisible(false);
            node.setManaged(false);
            return;
        }
        FadeTransition ft = new FadeTransition(Duration.millis(millis), node);
        ft.setFromValue(node.getOpacity());
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            node.setVisible(false);
            node.setManaged(false);
        });
        ft.play();
    }
}
