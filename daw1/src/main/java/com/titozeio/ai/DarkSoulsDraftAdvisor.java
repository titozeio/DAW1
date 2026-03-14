package com.titozeio.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.titozeio.enums.RobotTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Asesor de draft para la IA DARK SOULS.
 * Aprovecha las estadísticas exportadas de simulaciones masivas para priorizar:
 * score global, sinergias de parejas y counters directos contra el equipo rival.
 */
public class DarkSoulsDraftAdvisor {
    private static final Map<RobotTemplate, Integer> FALLBACK_ALLROUND = Map.of(
            RobotTemplate.BULWARK, 67,
            RobotTemplate.DEATH_KNIGHT, 59,
            RobotTemplate.VICTORY_SABER, 56,
            RobotTemplate.BULLSEYE, 53,
            RobotTemplate.ICE_AGE, 50,
            RobotTemplate.SCOUT, 44);

    private final Map<String, Integer> allroundScores;
    private final Map<String, PairStat> allyPairs;
    private final Map<String, Integer> matchupDominance;
    private Path loadedStatsPath;
    private FileTime loadedStatsTimestamp;

    public DarkSoulsDraftAdvisor() {
        this(loadLatestStatsData().orElseGet(LoadedDraftStatsData::fallback));
    }

    DarkSoulsDraftAdvisor(LoadedDraftStatsData data) {
        this.allroundScores = new HashMap<>(data.allroundScores());
        this.allyPairs = new HashMap<>(data.allyPairs());
        this.matchupDominance = new HashMap<>(data.matchupDominance());
        this.loadedStatsPath = data.sourcePath();
        this.loadedStatsTimestamp = data.lastModified();
    }

    public RobotTemplate chooseBestPick(List<RobotTemplate> available, List<RobotTemplate> ownTeam,
            List<RobotTemplate> enemyTeam) {
        refreshStatsIfNeeded();
        if (available == null || available.isEmpty()) {
            return null;
        }

        RobotTemplate best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (RobotTemplate candidate : available) {
            double score = scoreCandidate(candidate, ownTeam, enemyTeam);
            if (best == null || score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    public List<RobotTemplate> chooseTeam(List<RobotTemplate> pool, List<RobotTemplate> enemyTeam, int size, long seed) {
        refreshStatsIfNeeded();
        if (pool == null || pool.isEmpty() || size <= 0) {
            return List.of();
        }

        List<RobotTemplate> available = new ArrayList<>(pool);
        Collections.shuffle(available, new Random(seed));

        List<RobotTemplate> team = new ArrayList<>();
        while (team.size() < size && !available.isEmpty()) {
            RobotTemplate pick = chooseBestPick(available, team, enemyTeam);
            if (pick == null) {
                break;
            }
            team.add(pick);
            available.remove(pick);
        }
        return List.copyOf(team);
    }

    private double scoreCandidate(RobotTemplate candidate, List<RobotTemplate> ownTeam, List<RobotTemplate> enemyTeam) {
        String candidateName = displayName(candidate);
        double score = allroundScores.getOrDefault(candidateName, FALLBACK_ALLROUND.getOrDefault(candidate, 45)) * 10.0;

        for (RobotTemplate ally : ownTeam) {
            PairStat pair = allyPairs.get(pairKey(candidateName, displayName(ally)));
            if (pair != null) {
                score += pair.winRate() * 130.0;
                score += Math.min(20.0, pair.appearances() / 60.0);
                score -= pair.timeoutRate() * 45.0;
            }
        }

        for (RobotTemplate enemy : enemyTeam) {
            String enemyName = displayName(enemy);
            int favorable = matchupDominance.getOrDefault(candidateName + " > " + enemyName, 0);
            int unfavorable = matchupDominance.getOrDefault(enemyName + " > " + candidateName, 0);
            score += (favorable - unfavorable) * 0.18;
        }

        if (ownTeam.isEmpty()) {
            score += openingPriority(candidate);
        }

        return score;
    }

    private double openingPriority(RobotTemplate candidate) {
        return switch (candidate) {
            case BULWARK -> 14.0;
            case BULLSEYE -> 12.0;
            case DEATH_KNIGHT -> 8.0;
            case VICTORY_SABER -> 7.0;
            case ICE_AGE -> 6.0;
            case SCOUT -> 4.0;
        };
    }

    private static String displayName(RobotTemplate template) {
        return template.createRobot(null).getModelName();
    }

    private static String pairKey(String robotA, String robotB) {
        return robotA.compareTo(robotB) <= 0 ? robotA + " + " + robotB : robotB + " + " + robotA;
    }

    private void refreshStatsIfNeeded() {
        Optional<LoadedDraftStatsData> latest = loadLatestStatsData();
        if (latest.isEmpty()) {
            return;
        }
        LoadedDraftStatsData data = latest.get();
        if (data.sourcePath() == null || data.lastModified() == null) {
            return;
        }
        if (data.sourcePath().equals(loadedStatsPath) && data.lastModified().equals(loadedStatsTimestamp)) {
            return;
        }
        allroundScores.clear();
        allroundScores.putAll(data.allroundScores());
        allyPairs.clear();
        allyPairs.putAll(data.allyPairs());
        matchupDominance.clear();
        matchupDominance.putAll(data.matchupDominance());
        loadedStatsPath = data.sourcePath();
        loadedStatsTimestamp = data.lastModified();
    }

    private static Optional<LoadedDraftStatsData> loadLatestStatsData() {
        for (Path baseDir : List.of(Path.of("stats"), Path.of("daw1", "stats"))) {
            if (!Files.isDirectory(baseDir)) {
                continue;
            }
            try {
                Optional<Path> latest = Files.list(baseDir)
                        .filter(path -> path.getFileName().toString().endsWith(".json"))
                        .sorted((left, right) -> {
                            try {
                                return Files.getLastModifiedTime(right).compareTo(Files.getLastModifiedTime(left));
                            } catch (IOException ex) {
                                return 0;
                            }
                        })
                        .findFirst();
                if (latest.isPresent()) {
                    Path latestPath = latest.get();
                    return Optional.of(parseStats(latestPath, Files.getLastModifiedTime(latestPath)));
                }
            } catch (IOException ignored) {
            }
        }
        return Optional.empty();
    }

    private static LoadedDraftStatsData parseStats(Path path, FileTime lastModified) throws IOException {
        JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();

        Map<String, Integer> allround = parseIntMap(root.getAsJsonObject("robotScoresALL"));
        Map<String, Integer> dominance = parseIntMap(root.getAsJsonObject("matchupDominance"));
        Map<String, PairStat> pairs = parsePairStats(root.getAsJsonObject("allyPairs"));

        return new LoadedDraftStatsData(allround, pairs, dominance, path, lastModified);
    }

    private static Map<String, Integer> parseIntMap(JsonObject object) {
        Map<String, Integer> values = new HashMap<>();
        if (object == null) {
            return values;
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            values.put(entry.getKey(), entry.getValue().getAsInt());
        }
        return values;
    }

    private static Map<String, PairStat> parsePairStats(JsonObject object) {
        Map<String, PairStat> pairs = new HashMap<>();
        if (object == null) {
            return pairs;
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonObject pairObject = entry.getValue().getAsJsonObject();
            int appearances = pairObject.get("appearances").getAsInt();
            double winRate = pairObject.get("winRate").getAsDouble();
            int timeouts = pairObject.get("timeouts").getAsInt();
            pairs.put(entry.getKey(), new PairStat(appearances, winRate, appearances == 0 ? 0.0 : (double) timeouts / appearances));
        }
        return pairs;
    }

    record PairStat(int appearances, double winRate, double timeoutRate) {
    }

    record LoadedDraftStatsData(Map<String, Integer> allroundScores, Map<String, PairStat> allyPairs,
            Map<String, Integer> matchupDominance, Path sourcePath, FileTime lastModified) {
        static LoadedDraftStatsData fallback() {
            Map<String, Integer> fallbackScores = new HashMap<>();
            for (Map.Entry<RobotTemplate, Integer> entry : FALLBACK_ALLROUND.entrySet()) {
                fallbackScores.put(displayName(entry.getKey()), entry.getValue());
            }
            return new LoadedDraftStatsData(fallbackScores, Map.of(), Map.of(), null, null);
        }
    }
}
