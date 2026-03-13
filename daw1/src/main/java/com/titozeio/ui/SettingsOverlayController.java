package com.titozeio.ui;

import com.titozeio.ai.AiDifficulty;
import com.titozeio.engine.Game;
import com.titozeio.engine.SoundManager;
import com.titozeio.enums.RobotTemplate;
import com.titozeio.match.BatchSimulationConfig;
import com.titozeio.match.BatchSimulationService;
import com.titozeio.match.MatchResult;
import com.titozeio.match.MatchRunner;
import com.titozeio.match.TurnEngine;
import com.titozeio.telemetry.SimulationStats;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @FXML
    private ComboBox<AiDifficulty> aiDifficultyChoice;

    @FXML
    private TextField simulationMatchCountField;

    @FXML
    private Button launchSimulationButton;

    @FXML
    private StackPane simulationOverlay;

    @FXML
    private Label simulationProgressLabel;

    @FXML
    private Label simulationStatusLabel;

    @FXML
    private VBox simulationRobotStatsBox;

    @FXML
    private HBox simulationSortHeader;

    @FXML
    private VBox simulationAllyPairsBox;

    @FXML
    private VBox simulationMatchupsBox;

    @FXML
    private Button cancelSimulationButton;

    @FXML
    private Button exportSimulationButton;

    private Game game;
    private Task<SimulationStats> simulationTask;
    private final AtomicBoolean simulationCancelRequested = new AtomicBoolean(false);
    private SimulationStats currentSimulationStats;
    private final Map<String, RobotStatRow> robotStatRows = new LinkedHashMap<>();
    private static final EnumSet<AiDifficulty> AVAILABLE_DIFFICULTIES = EnumSet.of(AiDifficulty.EASY,
            AiDifficulty.MEDIUM);
    private SortMetric currentSortMetric = SortMetric.SCORE;

    @FXML
    private void initialize() {
        if (musicToggle != null) {
            musicToggle.setText("ON");
        }
        if (sfxToggle != null) {
            sfxToggle.setText("ON");
        }
        if (aiDifficultyChoice != null) {
            aiDifficultyChoice.getItems().setAll(AiDifficulty.values());
            configureAiDifficultyCombo();
        }
        if (simulationMatchCountField != null) {
            simulationMatchCountField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (game != null && !newVal.isEmpty()) {
                    try {
                        game.setSimulationMatchCount(Integer.parseInt(newVal.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            });
        }
        initializeRobotStatsRows();
        initializeSummaryBoxes();
        updateSortHeaderStyles();
        updateSimulationButtons(false);
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

    @FXML
    public void handleAiDifficultyAction() {
        if (aiDifficultyChoice == null || game == null || aiDifficultyChoice.getValue() == null) {
            return;
        }
        game.setAiDifficulty(aiDifficultyChoice.getValue());
    }

    @FXML
    public void handleLaunchSimulationAction() {
        if (game == null || simulationTask != null && simulationTask.isRunning()) {
            return;
        }

        int matchCount = parseMatchCount();
        if (matchCount <= 0) {
            simulationStatusLabel.setText("Número de partidas inválido.");
            return;
        }

        currentSimulationStats = new SimulationStats();
        resetRobotStatsRows();
        initializeSummaryBoxes();
        simulationCancelRequested.set(false);
        updateSimulationProgress(0, matchCount);
        simulationStatusLabel.setText("Lanzando simulación...");
        updateSimulationButtons(true);
        Screen.fadeInNode(simulationOverlay, 150);

        BatchSimulationConfig config = new BatchSimulationConfig(
                matchCount,
                System.currentTimeMillis(),
                60,
                game.getAiDifficulty(),
                game.getAiDifficulty(),
                null,
                null);
        BatchSimulationService simulationService = new BatchSimulationService(new MatchRunner(new TurnEngine()));

        simulationTask = new Task<>() {
            @Override
            protected SimulationStats call() {
                return simulationService.runBatch(
                        config,
                        result -> Platform.runLater(() -> applyLiveMatchResult(result, matchCount)),
                        simulationCancelRequested::get);
            }
        };

        simulationTask.setOnSucceeded(event -> {
            currentSimulationStats = simulationTask.getValue();
            simulationStatusLabel.setText("Simulación completada.");
            updateSimulationButtons(false);
        });
        simulationTask.setOnCancelled(event -> {
            simulationStatusLabel.setText("Simulación cancelada.");
            updateSimulationButtons(false);
        });
        simulationTask.setOnFailed(event -> {
            Throwable error = simulationTask.getException();
            simulationStatusLabel.setText("Error: " + (error != null ? error.getMessage() : "desconocido"));
            updateSimulationButtons(false);
        });

        Thread worker = new Thread(simulationTask, "daw1-simulation-task");
        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    public void handleCancelSimulationAction() {
        simulationCancelRequested.set(true);
        if (simulationTask != null) {
            simulationTask.cancel();
        }
    }

    @FXML
    public void handleExportSimulationAction() {
        if (currentSimulationStats == null || currentSimulationStats.getResults().isEmpty()) {
            simulationStatusLabel.setText("No hay datos que exportar.");
            return;
        }
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path statsDir = Path.of("stats");
        Path csvPath = statsDir.resolve("simulation-stats-" + stamp + ".csv");
        Path jsonPath = statsDir.resolve("simulation-stats-" + stamp + ".json");
        try {
            java.nio.file.Files.createDirectories(statsDir);
            currentSimulationStats.exportCsv(csvPath);
            currentSimulationStats.exportJson(jsonPath);
            simulationStatusLabel.setText("Exportado en: " + csvPath.toAbsolutePath());
        } catch (IOException e) {
            simulationStatusLabel.setText("Error exportando estadísticas.");
        }
    }

    @FXML
    public void handleSimulationCloseAction() {
        Screen.fadeOutNode(simulationOverlay, 150);
    }

    @FXML
    public void handleSimulationSortAction(ActionEvent event) {
        if (!(event.getSource() instanceof Button button)) {
            return;
        }
        Object sortKey = button.getUserData();
        if (!(sortKey instanceof String metricKey)) {
            return;
        }
        currentSortMetric = SortMetric.from(metricKey);
        updateSortHeaderStyles();
        resortRobotStatsRows();
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
        if (aiDifficultyChoice != null) {
            aiDifficultyChoice.setValue(game.getAiDifficulty());
        }
        if (simulationMatchCountField != null) {
            simulationMatchCountField.setText(String.valueOf(game.getSimulationMatchCount()));
        }
    }

    private int parseMatchCount() {
        if (simulationMatchCountField == null) {
            return 0;
        }
        try {
            return Integer.parseInt(simulationMatchCountField.getText().trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void initializeRobotStatsRows() {
        if (simulationRobotStatsBox == null || !robotStatRows.isEmpty()) {
            return;
        }
        for (RobotTemplate template : RobotTemplate.values()) {
            String robotName = template.createRobot(null).getModelName();
            RobotStatRow row = new RobotStatRow(robotName);
            robotStatRows.put(robotName, row);
            simulationRobotStatsBox.getChildren().add(row.container());
        }
    }

    private void resetRobotStatsRows() {
        robotStatRows.values().forEach(RobotStatRow::reset);
        resortRobotStatsRows();
    }

    private void applyLiveMatchResult(MatchResult result, int totalMatches) {
        if (currentSimulationStats == null) {
            currentSimulationStats = new SimulationStats();
        }
        currentSimulationStats.record(result);
        updateSimulationProgress((int) result.matchId(), totalMatches);
        simulationStatusLabel.setText("Último resultado: " + result.winner() + " por " + result.victoryType() + ".");
        for (Map.Entry<String, RobotStatRow> entry : robotStatRows.entrySet()) {
            String robot = entry.getKey();
            RobotStatRow row = entry.getValue();
            row.update(
                    currentSimulationStats.getRobotWins().getOrDefault(robot, 0),
                    currentSimulationStats.getRobotLosses().getOrDefault(robot, 0),
                    currentSimulationStats.getRobotTimeouts().getOrDefault(robot, 0),
                    currentSimulationStats.getRobotSurvivals().getOrDefault(robot, 0),
                    currentSimulationStats.getRobotDeaths().getOrDefault(robot, 0),
                    currentSimulationStats.getRobotKills().getOrDefault(robot, 0),
                    currentSimulationStats.getRobotScores().getOrDefault(robot, 0));
        }
        refreshSimulationSummaries();
        resortRobotStatsRows();
    }

    private void updateSimulationProgress(int current, int total) {
        if (simulationProgressLabel != null) {
            simulationProgressLabel.setText("Partida actual: " + current + " / " + total);
        }
    }

    private void updateSimulationButtons(boolean running) {
        if (launchSimulationButton != null) {
            launchSimulationButton.setDisable(running);
        }
        if (cancelSimulationButton != null) {
            cancelSimulationButton.setDisable(!running);
        }
        if (exportSimulationButton != null) {
            boolean hasResults = currentSimulationStats != null && !currentSimulationStats.getResults().isEmpty();
            exportSimulationButton.setDisable(running || !hasResults);
        }
    }

    private void resortRobotStatsRows() {
        if (simulationRobotStatsBox == null) {
            return;
        }
        java.util.List<RobotStatRow> ordered = new java.util.ArrayList<>(robotStatRows.values());
        ordered.sort(
                java.util.Comparator.comparingInt((RobotStatRow row) -> row.metricValue(currentSortMetric))
                        .thenComparingInt(RobotStatRow::score)
                        .thenComparingInt(RobotStatRow::wins)
                        .thenComparingInt(RobotStatRow::survives)
                        .reversed()
                        .thenComparing(RobotStatRow::robotName));
        simulationRobotStatsBox.getChildren().setAll(
                ordered.stream().map(RobotStatRow::container).toList());
    }

    private void configureAiDifficultyCombo() {
        aiDifficultyChoice.setCellFactory(listView -> new AiDifficultyCell());
        aiDifficultyChoice.setButtonCell(new AiDifficultyCell());
        aiDifficultyChoice.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !AVAILABLE_DIFFICULTIES.contains(newValue)) {
                aiDifficultyChoice.setValue(oldValue != null ? oldValue : AiDifficulty.EASY);
            }
        });
    }

    private void initializeSummaryBoxes() {
        renderSummaryBox(simulationAllyPairsBox, "Esperando simulación...");
        renderSummaryBox(simulationMatchupsBox, "Esperando simulación...");
    }

    private void refreshSimulationSummaries() {
        if (currentSimulationStats == null) {
            return;
        }
        java.util.List<String> pairLines = currentSimulationStats.getTopAllyPairs(5).stream()
                .map(stat -> String.format("%s | W:%d L:%d T:%d | %.0f%%",
                        stat.pairName(),
                        stat.wins(),
                        stat.losses(),
                        stat.timeouts(),
                        stat.winRate() * 100.0))
                .toList();
        java.util.List<String> matchupLines = currentSimulationStats.getTopMatchupDominance(5).stream()
                .map(entry -> entry.getKey() + " | " + entry.getValue())
                .toList();
        renderSummaryBox(simulationAllyPairsBox, pairLines.isEmpty() ? "Sin datos todavía." : null, pairLines);
        renderSummaryBox(simulationMatchupsBox, matchupLines.isEmpty() ? "Sin datos todavía." : null, matchupLines);
    }

    private void renderSummaryBox(VBox container, String placeholder) {
        renderSummaryBox(container, placeholder, java.util.List.of());
    }

    private void renderSummaryBox(VBox container, String placeholder, java.util.List<String> lines) {
        if (container == null) {
            return;
        }
        java.util.List<Label> labels = new java.util.ArrayList<>();
        if (lines.isEmpty()) {
            Label label = new Label(placeholder != null ? placeholder : "Sin datos.");
            label.getStyleClass().add("simulation-summary-placeholder");
            labels.add(label);
        } else {
            for (String line : lines) {
                Label label = new Label(line);
                label.getStyleClass().add("simulation-summary-item");
                labels.add(label);
            }
        }
        container.getChildren().setAll(labels);
    }

    private void updateSortHeaderStyles() {
        if (simulationSortHeader == null) {
            return;
        }
        for (javafx.scene.Node child : simulationSortHeader.getChildren()) {
            if (!(child instanceof Button button)) {
                continue;
            }
            button.getStyleClass().remove("simulation-sort-button-active");
            Object sortKey = button.getUserData();
            if (sortKey instanceof String metricKey && currentSortMetric == SortMetric.from(metricKey)) {
                button.getStyleClass().add("simulation-sort-button-active");
            }
        }
    }

    private enum SortMetric {
        WINS,
        LOSSES,
        TIMEOUTS,
        SURVIVES,
        DEATHS,
        KILLS,
        SCORE;

        static SortMetric from(String key) {
            return switch (key) {
                case "W" -> WINS;
                case "L" -> LOSSES;
                case "T" -> TIMEOUTS;
                case "S" -> SURVIVES;
                case "D" -> DEATHS;
                case "K" -> KILLS;
                default -> SCORE;
            };
        }
    }

    private static class AiDifficultyCell extends ListCell<AiDifficulty> {
        @Override
        protected void updateItem(AiDifficulty item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setDisable(false);
                return;
            }
            boolean available = AVAILABLE_DIFFICULTIES.contains(item);
            setText(available ? item.getDisplayName() : item.getDisplayName() + " (Próximamente)");
            setDisable(!available);
            setOpacity(available ? 1.0 : 0.45);
        }
    }

    private record RobotStatRow(
            String robotName,
            HBox container,
            Label winsLabel,
            Label lossesLabel,
            Label timeoutsLabel,
            Label survivesLabel,
            Label deathsLabel,
            Label killsLabel,
            Label scoreLabel) {

        RobotStatRow(String robotName) {
            this(robotName, createRowContainer(robotName), createValueLabel(), createValueLabel(), createValueLabel(),
                    createValueLabel(),
                    createValueLabel(), createValueLabel(), createValueLabel());
            HBox row = container;
            row.getChildren().addAll(
                    createHeaderLabel(robotName),
                    createSpacer(),
                    createMetricGroup("W", winsLabel),
                    createMetricGroup("L", lossesLabel),
                    createMetricGroup("T", timeoutsLabel),
                    createMetricGroup("S", survivesLabel),
                    createMetricGroup("D", deathsLabel),
                    createMetricGroup("K", killsLabel),
                    createMetricGroup("Score", scoreLabel));
        }

        void reset() {
            update(0, 0, 0, 0, 0, 0, 0);
        }

        void update(int wins, int losses, int timeouts, int survives, int deaths, int kills, int score) {
            winsLabel.setText(String.valueOf(wins));
            lossesLabel.setText(String.valueOf(losses));
            timeoutsLabel.setText(String.valueOf(timeouts));
            survivesLabel.setText(String.valueOf(survives));
            deathsLabel.setText(String.valueOf(deaths));
            killsLabel.setText(String.valueOf(kills));
            scoreLabel.setText(String.valueOf(score));
        }

        int wins() {
            return parseMetric(winsLabel);
        }

        int survives() {
            return parseMetric(survivesLabel);
        }

        int score() {
            return parseMetric(scoreLabel);
        }

        int metricValue(SortMetric metric) {
            return switch (metric) {
                case WINS -> wins();
                case LOSSES -> parseMetric(lossesLabel);
                case TIMEOUTS -> parseMetric(timeoutsLabel);
                case SURVIVES -> survives();
                case DEATHS -> parseMetric(deathsLabel);
                case KILLS -> parseMetric(killsLabel);
                case SCORE -> score();
            };
        }

        private static HBox createRowContainer(String robotName) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("simulation-stat-row");
            return row;
        }

        private static Label createHeaderLabel(String text) {
            Label label = new Label(text);
            label.getStyleClass().add("simulation-robot-name");
            label.setMinWidth(132);
            return label;
        }

        private static Region createSpacer() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            return spacer;
        }

        private static VBox createMetricGroup(String name, Label valueLabel) {
            Label nameLabel = new Label(name);
            nameLabel.getStyleClass().add("simulation-metric-name");
            VBox box = new VBox(2, nameLabel, valueLabel);
            box.setAlignment(Pos.CENTER);
            box.setMinWidth(34);
            return box;
        }

        private static Label createValueLabel() {
            Label label = new Label("0");
            label.getStyleClass().add("simulation-metric-value");
            return label;
        }

        private static int parseMetric(Label label) {
            try {
                return Integer.parseInt(label.getText());
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
    }
}
