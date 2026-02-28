package com.titozeio.ui;

import com.titozeio.engine.Game;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.File;

/**
 * Pantalla de inicio del juego.
 * Muestra el título "Devastation Ai Wars 1" y el botón de "Jugar".
 */
public class MainMenuScreen extends Screen {

    private Stage window;
    private Game game;
    private Scene scene;

    public MainMenuScreen(Stage window, Game game) {
        this.window = window;
        this.game = game;
        setupUI();
    }

    /**
     * Configura la interfaz de usuario de la pantalla de inicio.
     * Crea un StackPane como contenedor principal y agrega un fondo de imagen.
     * Luego, crea un VBox para el contenido del menu y agrega un Label y un Button.
     * Finalmente, agrega el VBox al StackPane y crea una Scene con el StackPane
     * como raiz.
     */
    private void setupUI() {
        StackPane root = new StackPane();

        // Cargar imagen de fondo
        try {
            File imageFile = new File("art/concepts/Splash concept.png");
            if (imageFile.exists()) {
                String imageUri = imageFile.toURI().toURL().toString();
                Image bgImageFile = new Image(imageUri);

                // Configuramos para que "cubra" toda el área manteniendo proporciones
                BackgroundImage bgImage = new BackgroundImage(
                        bgImageFile,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true));
                root.setBackground(new Background(bgImage));
            } else {
                System.out.println("No se encontró la imagen en: " + imageFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        VBox contentLayout = new VBox(30);
        contentLayout.setAlignment(Pos.CENTER);
        // Caja semi-transparente para el menu para mejorar legibilidad sobre el fondo
        contentLayout.setMaxSize(800, 450);
        contentLayout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4); -fx-background-radius: 15; -fx-padding: 40;");

        // Cargar imagen del título
        try {
            File titleFile = new File("art/concepts/title_transparent.png");
            if (titleFile.exists()) {
                Image titleImg = new Image(titleFile.toURI().toURL().toString());
                ImageView titleView = new ImageView(titleImg);
                titleView.setPreserveRatio(true);
                titleView.setFitWidth(600); // Ajustar el tamaño según convenga
                contentLayout.getChildren().add(titleView);
            } else {
                Label titleLabel = new Label("Devastation Ai Wars 1");
                titleLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white; -fx-font-weight: bold;");
                contentLayout.getChildren().add(titleLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button playButton = new Button("Jugar");
        playButton.setStyle(
                "-fx-font-size: 24px; -fx-padding: 10 50; -fx-base: #2ecc71; -fx-text-fill: white; -fx-cursor: hand;");

        // Asigna la accion al boton
        playButton.setOnAction(e -> handleInput("PLAY"));

        contentLayout.getChildren().add(playButton);
        root.getChildren().add(contentLayout);

        this.scene = new Scene(root, 1280, 720);
    }

    @Override
    public void display() {
        // Coloca esta escena en la ventana principal
        this.window.setScene(this.scene);
    }

    @Override
    public void handleInput(String input) {
        if (input.equals("PLAY")) {
            System.out.println("Iniciando transicion a RobotSelectionScreen...");
            this.game.displayScreen(new RobotSelectionScreen(window, game));
        }
    }
}
