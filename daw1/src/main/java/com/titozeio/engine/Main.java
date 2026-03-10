package com.titozeio.engine;

// Main.java
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.text.Font;

public class Main extends Application {

    private Game game;

    @Override
    /**
     * Inicia la logica asociada al metodo.
     * @param primaryStage parametro de entrada.
     */
    public void start(Stage primaryStage) {
        // Cargar fuente personalizada
        Font.loadFont(Main.class.getResourceAsStream("/com/titozeio/fonts/Exo2-Regular.ttf"), 14);

        // remote change directly done in github.com

        primaryStage.setTitle("Devastation AI Wars 1");
        addAppIcons(primaryStage);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(755);
        primaryStage.setResizable(false);

        // Inicializa el juego pasandole la ventana
        this.game = new Game(primaryStage);
        this.game.start();

        // Another comment to test a merge, done locally

        try {
            primaryStage.show();
        } catch (Throwable t) {
            // En algunos entornos CI headless (Monocle desalineado) show() puede
            // fallar por incompatibilidades de Glass. No debe impedir arrancar tests.
            System.err.println("[Main] No se pudo mostrar la ventana: " + t.getMessage());
        }
    }

    /**
     * Crea una nueva instancia de la clase.
     * @param args parametro de entrada.
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void addAppIcons(Stage stage) {
        String[] sizes = { "16", "32", "48", "64", "128", "256" };
        for (String s : sizes) {
            var res = Main.class.getResource("/com/titozeio/icons/daw1_" + s + ".png");
            if (res != null) {
                stage.getIcons().add(new javafx.scene.image.Image(res.toExternalForm()));
            }
        }
    }
}
