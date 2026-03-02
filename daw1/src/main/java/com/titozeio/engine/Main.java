package com.titozeio.engine;

// Main.java
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.text.Font;

public class Main extends Application {

    private Game game;

    @Override
    public void start(Stage primaryStage) {
        // Cargar fuente personalizada
        Font.loadFont(Main.class.getResourceAsStream("/com/titozeio/fonts/Exo2-Regular.ttf"), 14);

        //remote change directly done in github.com

        primaryStage.setTitle("Devastation AI Wars 1");
        primaryStage.setWidth(1280);
        primaryStage.setHeight(755);

        // Inicializa el juego pasandole la ventana
        this.game = new Game(primaryStage);
        this.game.start();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
