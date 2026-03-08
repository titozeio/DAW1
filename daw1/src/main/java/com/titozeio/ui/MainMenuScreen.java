package com.titozeio.ui;

import com.titozeio.engine.Game;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Rectangle2D;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.File;

/**
 * Pantalla de inicio del juego.
 * Esta pantalla ahora utiliza FXML para permitir su edición en Scene Builder.
 */
public class MainMenuScreen extends Screen {

    @FXML
    private StackPane settingsOverlay;

    @FXML
    private ToggleButton musicToggle;

    @FXML
    private ToggleButton sfxToggle;

    @FXML
    private Button playButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button closeSettingsButton;

    @FXML
    private VBox settingsPanel;

    private Stage window;
    private Game game;
    private Scene scene;
    private Image atlasImage;

    public MainMenuScreen() {
        // Constructor vacío para FXMLLoader
    }

    public static MainMenuScreen create(Stage window, Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainMenuScreen.class.getResource("/com/titozeio/ui/MainMenuScreen.fxml"));
            Parent root = loader.load();
            MainMenuScreen controller = loader.getController();
            controller.window = window;
            controller.game = game;
            controller.scene = new Scene(root, 1280, 720);
            controller.applyGlobalStyle(controller.scene);
            return controller;
        } catch (IOException e) {
            System.err.println("Error al cargar MainMenuScreen.fxml: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void handlePlayAction() {
        handleInput("PLAY");
    }

    @FXML
    private void initialize() {
        loadAtlasAndApplySprites();

        if (musicToggle != null) {
            musicToggle.setSelected(true);
            musicToggle.setText("");
            musicToggle.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");
        }
        if (sfxToggle != null) {
            sfxToggle.setSelected(true);
            sfxToggle.setText("");
            sfxToggle.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");
        }
        refreshAudioToggleGraphics();
    }

    @FXML
    public void handleSettingsOpenAction() {
        settingsOverlay.setVisible(true);
        settingsOverlay.setManaged(true);
    }

    @FXML
    public void handleSettingsCloseAction() {
        settingsOverlay.setVisible(false);
        settingsOverlay.setManaged(false);
    }

    @FXML
    public void handleMusicToggleAction() {
        boolean enabled = musicToggle.isSelected();
        game.setMusicEnabled(enabled);
        refreshAudioToggleGraphics();
    }

    @FXML
    public void handleSfxToggleAction() {
        boolean enabled = sfxToggle.isSelected();
        game.setSfxEnabled(enabled);
        refreshAudioToggleGraphics();
    }

    private void loadAtlasAndApplySprites() {
        atlasImage = loadAtlasImage();
        if (atlasImage == null) {
            return;
        }

        applyStateSprites(playButton, "Play", 8, 8, 8, 128, 608, 128);
        applyStateSprites(settingsButton, "Settings", 608, 128, 728, 128, 848, 128);
        applyStateSprites(closeSettingsButton, "", 608, 8, 728, 8, 848, 8);

        ImageView popupBg = createSpriteView(8, 8, 112, 112, 180, 180);
        if (popupBg != null) {
            settingsOverlay.getChildren().add(0, popupBg);
        }
        settingsPanel.setStyle("-fx-background-color: rgba(11, 22, 28, 0.92); -fx-border-color: #4EE2C9; -fx-border-width: 1.5; -fx-background-radius: 6; -fx-border-radius: 6;");
    }

    private Image loadAtlasImage() {
        try {
            var cp = MainMenuScreen.class.getResource("/com/titozeio/ui/ui_atlas.png");
            if (cp != null) {
                return new Image(cp.toExternalForm());
            }
            File fs = new File("assets/art/ui/ui_atlas.png");
            if (fs.exists()) {
                return new Image(fs.toURI().toString());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void applyStateSprites(Button button, String text, int nx, int ny, int hx, int hy, int px, int py) {
        if (button == null || atlasImage == null) {
            return;
        }
        button.setText(text);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");
        if (!text.isEmpty()) {
            button.setContentDisplay(ContentDisplay.LEFT);
            button.setGraphicTextGap(12);
            button.setGraphic(createSpriteView(nx, ny, 112, 112, 40, 40));
            button.setOnMouseEntered(e -> button.setGraphic(createSpriteView(hx, hy, 112, 112, 40, 40)));
            button.setOnMouseExited(e -> button.setGraphic(createSpriteView(nx, ny, 112, 112, 40, 40)));
            button.setOnMousePressed(e -> button.setGraphic(createSpriteView(px, py, 112, 112, 40, 40)));
        } else {
            button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            button.setGraphic(createSpriteView(nx, ny, 112, 112, 24, 24));
            button.setOnMouseEntered(e -> button.setGraphic(createSpriteView(hx, hy, 112, 112, 24, 24)));
            button.setOnMouseExited(e -> button.setGraphic(createSpriteView(nx, ny, 112, 112, 24, 24)));
            button.setOnMousePressed(e -> button.setGraphic(createSpriteView(px, py, 112, 112, 24, 24)));
        }
        button.setOnMouseReleased(e -> {
            boolean hover = button.isHover();
            int tx = hover ? hx : nx;
            int ty = hover ? hy : ny;
            if (!text.isEmpty()) {
                button.setGraphic(createSpriteView(tx, ty, 112, 112, 40, 40));
            } else {
                button.setGraphic(createSpriteView(tx, ty, 112, 112, 24, 24));
            }
        });
    }

    private void refreshAudioToggleGraphics() {
        if (atlasImage == null) {
            return;
        }
        if (musicToggle != null) {
            if (musicToggle.isSelected()) {
                musicToggle.setGraphic(createSpriteView(128, 8, 112, 112, 34, 34));
            } else {
                musicToggle.setGraphic(createSpriteView(248, 8, 112, 112, 34, 34));
            }
            musicToggle.setPrefSize(52, 52);
        }
        if (sfxToggle != null) {
            if (sfxToggle.isSelected()) {
                sfxToggle.setGraphic(createSpriteView(368, 8, 112, 112, 34, 34));
            } else {
                sfxToggle.setGraphic(createSpriteView(488, 8, 112, 112, 34, 34));
            }
            sfxToggle.setPrefSize(52, 52);
        }
    }

    private ImageView createSpriteView(int x, int y, int w, int h, double fitW, double fitH) {
        if (atlasImage == null) {
            return null;
        }
        ImageView view = new ImageView(atlasImage);
        view.setViewport(new Rectangle2D(x, y, w, h));
        view.setFitWidth(fitW);
        view.setFitHeight(fitH);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setMouseTransparent(true);
        return view;
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
            this.game.displayScreen(RobotSelectionScreen.create(window, game));
        }
    }
}
