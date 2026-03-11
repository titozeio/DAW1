package com.titozeio.ui;

import com.titozeio.engine.Game;
import com.titozeio.engine.SoundManager;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;

/**
 * Componente reutilizable para mostrar y gestionar el panel de settings.
 */
public class SettingsOverlayController {

    @FXML
    private StackPane settingsOverlay;

    @FXML
    private CheckBox musicToggle;

    @FXML
    private CheckBox sfxToggle;

    private Game game;

    @FXML
    private void initialize() {
        if (musicToggle != null) {
            musicToggle.setText("ON");
        }
        if (sfxToggle != null) {
            sfxToggle.setText("ON");
        }
    }

    public void show(Game game) {
        this.game = game;
        syncAudioTogglesWithState();
        Screen.fadeInNode(settingsOverlay, 150);
    }

    @FXML
    public void handleSettingsCloseAction() {
        Screen.fadeOutNode(settingsOverlay, 150);
    }

    @FXML
    public void handleMusicToggleAction() {
        if (musicToggle == null || game == null) {
            return;
        }
        boolean enabled = musicToggle.isSelected();
        musicToggle.setText(enabled ? "ON" : "OFF");
        game.setMusicEnabled(enabled);
    }

    @FXML
    public void handleSfxToggleAction() {
        if (sfxToggle == null || game == null) {
            return;
        }
        boolean enabled = sfxToggle.isSelected();
        sfxToggle.setText(enabled ? "ON" : "OFF");
        SoundManager.setEnabled(enabled);
        game.setSfxEnabled(enabled);
    }

    private void syncAudioTogglesWithState() {
        if (game == null) {
            return;
        }
        if (musicToggle != null) {
            boolean musicOn = game.isMusicEnabled();
            musicToggle.setSelected(musicOn);
            musicToggle.setText(musicOn ? "ON" : "OFF");
        }
        if (sfxToggle != null) {
            boolean sfxOn = game.isSfxEnabled();
            sfxToggle.setSelected(sfxOn);
            sfxToggle.setText(sfxOn ? "ON" : "OFF");
            SoundManager.setEnabled(sfxOn);
        }
    }
}

