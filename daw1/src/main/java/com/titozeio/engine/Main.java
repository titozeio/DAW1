package com.titozeio.engine;

// Main.java
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import java.io.File;
import java.net.MalformedURLException;

public class Main extends Application {

    private Game game;

    @Override
    public void start(Stage primaryStage) {
        // Cargar fuente personalizada
        try {
            Font.loadFont(new File("assets/fonts/Exo2-Regular.ttf").toURI().toURL().toString(), 14);
        } catch (MalformedURLException e) {
            System.err.println("No se pudo cargar la fuente: " + e.getMessage());
        }

        primaryStage.setTitle("Devastation AI Wars 1");
        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);

        // Inicializa el juego pasandole la ventana
        this.game = new Game(primaryStage);
        this.game.start();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}