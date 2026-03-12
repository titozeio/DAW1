package com.titozeio.telemetry;

import com.titozeio.match.MatchResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agregador de métricas de simulación y exportación básica.
 */
public class SimulationStats {
    private final List<MatchResult> results = new ArrayList<>();
    private final Map<String, Integer> robotWins = new LinkedHashMap<>();
    private final Map<String, Integer> robotLosses = new LinkedHashMap<>();
    private final Map<String, Integer> robotTimeouts = new LinkedHashMap<>();
    private final Map<String, Integer> robotSurvivals = new LinkedHashMap<>();
    private final Map<String, Integer> robotDeaths = new LinkedHashMap<>();
    private final Map<String, Integer> robotKills = new LinkedHashMap<>();
    private final Map<String, Integer> victoryTypes = new LinkedHashMap<>();
    private final Map<String, Integer> allyPairAppearances = new LinkedHashMap<>();
    private final Map<String, Integer> allyPairWins = new LinkedHashMap<>();
    private final Map<String, Integer> allyPairLosses = new LinkedHashMap<>();
    private final Map<String, Integer> allyPairTimeouts = new LinkedHashMap<>();
    private final Map<String, Integer> matchupDominance = new LinkedHashMap<>();

    public void record(MatchResult result) {
        results.add(result);
        victoryTypes.merge(result.victoryType(), 1, Integer::sum);

        boolean isTimeoutOrDraw = "Empate".equals(result.winner()) || "timeout".equalsIgnoreCase(result.victoryType());
        recordAllyPairs(result.teamP1(), "Jugador 1".equals(result.winner()), "Jugador 2".equals(result.winner()), isTimeoutOrDraw);
        recordAllyPairs(result.teamP2(), "Jugador 2".equals(result.winner()), "Jugador 1".equals(result.winner()), isTimeoutOrDraw);
        if (!isTimeoutOrDraw) {
            if ("Jugador 1".equals(result.winner())) {
                recordMatchupDominance(result.teamP1(), result.teamP2());
            } else if ("Jugador 2".equals(result.winner())) {
                recordMatchupDominance(result.teamP2(), result.teamP1());
            }
        }

        for (String robot : result.teamP1()) {
            if (isTimeoutOrDraw) {
                robotTimeouts.merge(robot, 1, Integer::sum);
            } else if ("Jugador 1".equals(result.winner())) {
                robotWins.merge(robot, 1, Integer::sum);
            } else {
                robotLosses.merge(robot, 1, Integer::sum);
            }
        }
        for (String robot : result.teamP2()) {
            if (isTimeoutOrDraw) {
                robotTimeouts.merge(robot, 1, Integer::sum);
            } else if ("Jugador 2".equals(result.winner())) {
                robotWins.merge(robot, 1, Integer::sum);
            } else {
                robotLosses.merge(robot, 1, Integer::sum);
            }
        }
        for (String robot : result.survivorsP1()) {
            robotSurvivals.merge(robot, 1, Integer::sum);
        }
        for (String robot : result.survivorsP2()) {
            robotSurvivals.merge(robot, 1, Integer::sum);
        }
        for (String robot : result.destroyedP1()) {
            robotDeaths.merge(robot, 1, Integer::sum);
        }
        for (String robot : result.destroyedP2()) {
            robotDeaths.merge(robot, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : result.killsByRobot().entrySet()) {
            robotKills.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
    }

    public List<MatchResult> getResults() {
        return List.copyOf(results);
    }

    public Map<String, Integer> getRobotWins() {
        return Map.copyOf(robotWins);
    }

    public Map<String, Integer> getRobotLosses() {
        return Map.copyOf(robotLosses);
    }

    public Map<String, Integer> getRobotTimeouts() {
        return Map.copyOf(robotTimeouts);
    }

    public Map<String, Integer> getRobotSurvivals() {
        return Map.copyOf(robotSurvivals);
    }

    public Map<String, Integer> getRobotDeaths() {
        return Map.copyOf(robotDeaths);
    }

    public Map<String, Integer> getRobotKills() {
        return Map.copyOf(robotKills);
    }

    public Map<String, Integer> getRobotScores() {
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (String robot : robotWins.keySet()) {
            scores.put(robot, computeRobotScore(robot));
        }
        for (String robot : robotLosses.keySet()) {
            scores.putIfAbsent(robot, computeRobotScore(robot));
        }
        for (String robot : robotTimeouts.keySet()) {
            scores.putIfAbsent(robot, computeRobotScore(robot));
        }
        for (String robot : robotSurvivals.keySet()) {
            scores.putIfAbsent(robot, computeRobotScore(robot));
        }
        for (String robot : robotDeaths.keySet()) {
            scores.putIfAbsent(robot, computeRobotScore(robot));
        }
        for (String robot : robotKills.keySet()) {
            scores.putIfAbsent(robot, computeRobotScore(robot));
        }
        return Map.copyOf(scores);
    }

    public Map<String, Integer> getVictoryTypes() {
        return Map.copyOf(victoryTypes);
    }

    public List<PairStat> getTopAllyPairs(int limit) {
        return allyPairAppearances.keySet().stream()
                .map(this::buildPairStat)
                .sorted(Comparator
                        .comparingDouble(PairStat::winRate).reversed()
                        .thenComparingInt(PairStat::wins).reversed()
                        .thenComparingInt(PairStat::appearances).reversed()
                        .thenComparing(PairStat::pairName))
                .limit(limit)
                .toList();
    }

    public List<Map.Entry<String, Integer>> getTopMatchupDominance(int limit) {
        return matchupDominance.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(limit)
                .toList();
    }

    public void exportCsv(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("matchId,seed,difficultyP1,difficultyP2,winner,victoryType,turnCount,teamP1,teamP2,survivorsP1,survivorsP2,destroyedP1,destroyedP2");
        for (MatchResult result : results) {
            lines.add(String.join(",",
                    String.valueOf(result.matchId()),
                    String.valueOf(result.seed()),
                    sanitize(result.difficultyP1()),
                    sanitize(result.difficultyP2()),
                    sanitize(result.winner()),
                    sanitize(result.victoryType()),
                    String.valueOf(result.turnCount()),
                    sanitize(String.join("|", result.teamP1())),
                    sanitize(String.join("|", result.teamP2())),
                    sanitize(String.join("|", result.survivorsP1())),
                    sanitize(String.join("|", result.survivorsP2())),
                    sanitize(String.join("|", result.destroyedP1())),
                    sanitize(String.join("|", result.destroyedP2()))));
        }
        Files.write(path, lines);
    }

    public void exportJson(Path path) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"matches\": ").append(results.size()).append(",\n");
        json.append("  \"robotWins\": ").append(toJsonMap(robotWins)).append(",\n");
        json.append("  \"robotLosses\": ").append(toJsonMap(robotLosses)).append(",\n");
        json.append("  \"robotTimeouts\": ").append(toJsonMap(robotTimeouts)).append(",\n");
        json.append("  \"robotSurvivals\": ").append(toJsonMap(robotSurvivals)).append(",\n");
        json.append("  \"robotDeaths\": ").append(toJsonMap(robotDeaths)).append(",\n");
        json.append("  \"robotKills\": ").append(toJsonMap(robotKills)).append(",\n");
        json.append("  \"robotScores\": ").append(toJsonMap(getRobotScores())).append(",\n");
        json.append("  \"victoryTypes\": ").append(toJsonMap(victoryTypes)).append(",\n");
        json.append("  \"allyPairs\": ").append(toJsonPairStats()).append(",\n");
        json.append("  \"matchupDominance\": ").append(toJsonMap(matchupDominance)).append("\n");
        json.append("}\n");
        Files.writeString(path, json.toString());
    }

    private int computeRobotScore(String robot) {
        int wins = robotWins.getOrDefault(robot, 0);
        int losses = robotLosses.getOrDefault(robot, 0);
        int survivals = robotSurvivals.getOrDefault(robot, 0);
        int deaths = robotDeaths.getOrDefault(robot, 0);
        int kills = robotKills.getOrDefault(robot, 0);
        return wins * 4 + survivals * 2 + kills - losses * 3 - deaths * 2;
    }

    private String sanitize(String value) {
        return "\"" + value.replace("\"", "'") + "\"";
    }

    private void recordAllyPairs(List<String> team, boolean win, boolean loss, boolean timeout) {
        for (int i = 0; i < team.size(); i++) {
            for (int j = i + 1; j < team.size(); j++) {
                String pairKey = buildAllyPairKey(team.get(i), team.get(j));
                allyPairAppearances.merge(pairKey, 1, Integer::sum);
                if (timeout) {
                    allyPairTimeouts.merge(pairKey, 1, Integer::sum);
                } else if (win) {
                    allyPairWins.merge(pairKey, 1, Integer::sum);
                } else if (loss) {
                    allyPairLosses.merge(pairKey, 1, Integer::sum);
                }
            }
        }
    }

    private void recordMatchupDominance(List<String> winners, List<String> losers) {
        for (String winner : winners) {
            for (String loser : losers) {
                if (winner.equals(loser)) {
                    continue;
                }
                matchupDominance.merge(winner + " > " + loser, 1, Integer::sum);
            }
        }
    }

    private String buildAllyPairKey(String robotA, String robotB) {
        return robotA.compareTo(robotB) <= 0 ? robotA + " + " + robotB : robotB + " + " + robotA;
    }

    private PairStat buildPairStat(String pairName) {
        int appearances = allyPairAppearances.getOrDefault(pairName, 0);
        int wins = allyPairWins.getOrDefault(pairName, 0);
        int losses = allyPairLosses.getOrDefault(pairName, 0);
        int timeouts = allyPairTimeouts.getOrDefault(pairName, 0);
        double winRate = appearances == 0 ? 0.0 : (double) wins / appearances;
        return new PairStat(pairName, appearances, wins, losses, timeouts, winRate);
    }

    private String toJsonMap(Map<String, Integer> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (!first) {
                json.append(", ");
            }
            json.append("\"").append(entry.getKey().replace("\"", "'")).append("\": ").append(entry.getValue());
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    private String toJsonPairStats() {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (String pairName : allyPairAppearances.keySet()) {
            if (!first) {
                json.append(", ");
            }
            PairStat stat = buildPairStat(pairName);
            json.append("\"").append(pairName.replace("\"", "'")).append("\": {")
                    .append("\"appearances\": ").append(stat.appearances())
                    .append(", \"wins\": ").append(stat.wins())
                    .append(", \"losses\": ").append(stat.losses())
                    .append(", \"timeouts\": ").append(stat.timeouts())
                    .append(", \"winRate\": ").append(String.format(java.util.Locale.US, "%.4f", stat.winRate()))
                    .append("}");
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    public record PairStat(
            String pairName,
            int appearances,
            int wins,
            int losses,
            int timeouts,
            double winRate) {
    }
}
