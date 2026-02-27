package com.titozeio.engine;

// Main.java
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private Game game;

    @Override
    public void start(Stage primaryStage) {
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