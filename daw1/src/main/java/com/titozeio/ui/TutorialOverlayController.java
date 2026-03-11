package com.titozeio.ui;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

/**
 * Controlador para el componente reutilizable de instrucciones.
 */
public class TutorialOverlayController {

    @FXML
    private StackPane tutorialOverlay;

    /**
     * Muestra el panel con una animación de fade-in.
     */
    public void show() {
        if (tutorialOverlay != null) {
            Screen.fadeInNode(tutorialOverlay, 200);
        }
    }

    /**
     * Oculta el panel con una animación de fade-out.
     */
    @FXML
    public void close() {
        if (tutorialOverlay != null) {
            Screen.fadeOutNode(tutorialOverlay, 200);
        }
    }
}
