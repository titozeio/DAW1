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
    private final Map<String, Integer> robotDamageDealt = new LinkedHashMap<>();
    private final Map<String, Integer> robotDamageReceived = new LinkedHashMap<>();
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
        recordAllyPairs(result.teamP1(), "Jugador 1".equals(result.winner()), "Jugador 2".equals(result.winner()),
                isTimeoutOrDraw);
        recordAllyPairs(result.teamP2(), "Jugador 2".equals(result.winner()), "Jugador 1".equals(result.winner()),
                isTimeoutOrDraw);
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
        for (Map.Entry<String, Integer> entry : result.damageDealtByRobot().entrySet()) {
            robotDamageDealt.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : result.damageReceivedByRobot().entrySet()) {
            robotDamageReceived.merge(entry.getKey(), entry.getValue(), Integer::sum);
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

    public Map<String, Integer> getRobotDamageDealt() {
        return Map.copyOf(robotDamageDealt);
    }

    public Map<String, Integer> getRobotDamageReceived() {
        return Map.copyOf(robotDamageReceived);
    }

    public Map<String, Integer> getRoleScores(String role) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        java.util.Set<String> allRobots = new java.util.HashSet<>();
        allRobots.addAll(robotWins.keySet());
        allRobots.addAll(robotLosses.keySet());
        allRobots.addAll(robotTimeouts.keySet());

        for (String robot : allRobots) {
            int wins = robotWins.getOrDefault(robot, 0);
            int losses = robotLosses.getOrDefault(robot, 0);
            int timeouts = robotTimeouts.getOrDefault(robot, 0);
            int matches = wins + losses + timeouts;

            if (matches == 0)
                continue;

            double winRate = (double) wins / matches;
            double timeoutRate = (double) timeouts / matches;
            double survivals = robotSurvivals.getOrDefault(robot, 0);
            double survivalRate = survivals / matches;
            double deaths = robotDeaths.getOrDefault(robot, 0);
            double deathRate = deaths / matches;

            double killsAvg = (double) robotKills.getOrDefault(robot, 0) / matches;
            double dmgDealtAvg = (double) robotDamageDealt.getOrDefault(robot, 0) / matches;
            double dmgReceivedAvg = (double) robotDamageReceived.getOrDefault(robot, 0) / matches;

            // Scores normalizados por partida.
            // DPS valora daño, remates y conversión en victoria.
            // TANK premia absorber presión sobreviviendo.
            // SUPPORT intenta aproximar contribución táctica y utilidad, así que
            // penaliza perfiles de daño puro.
            // ALLROUND se orienta a balance general: prioriza winrate y consistencia
            // por encima de una mezcla artificial de roles.
            double scoreDps = (killsAvg * 42.0)
                    + (dmgDealtAvg * 5.5)
                    + (winRate * 18.0)
                    - (timeoutRate * 18.0);

            double tankingValue = dmgReceivedAvg * (0.5 + survivalRate);
            double scoreTank = (tankingValue * 4.5)
                    + (survivalRate * 45.0)
                    + (winRate * 12.0)
                    - (deathRate * 14.0)
                    - (timeoutRate * 8.0);

            double scoreSup = (winRate * 12.0)
                    + (survivalRate * 28.0)
                    + (dmgReceivedAvg * 2.8)
                    - (dmgDealtAvg * 1.4)
                    - (killsAvg * 12.0)
                    - (deathRate * 12.0)
                    - (timeoutRate * 10.0);

            double score = 0;
            if ("DPS".equals(role)) {
                score = scoreDps;
            } else if ("TANK".equals(role)) {
                score = scoreTank;
            } else if ("SUPPORT".equals(role)) {
                score = scoreSup;
            } else if ("ALLROUND".equals(role)) {
                score = (winRate * 70.0)
                        + (killsAvg * 12.0)
                        + (dmgDealtAvg * 2.0)
                        + (survivalRate * 10.0)
                        - (timeoutRate * 20.0);
            }
            scores.put(robot, (int) Math.round(score));
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
        lines.add(
                "matchId,seed,difficultyP1,difficultyP2,winner,victoryType,turnCount,teamP1,teamP2,survivorsP1,survivorsP2,destroyedP1,destroyedP2");
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
        json.append("  \"robotDamageDealt\": ").append(toJsonMap(robotDamageDealt)).append(",\n");
        json.append("  \"robotDamageReceived\": ").append(toJsonMap(robotDamageReceived)).append(",\n");
        json.append("  \"robotScoresDPS\": ").append(toJsonMap(getRoleScores("DPS"))).append(",\n");
        json.append("  \"robotScoresTANK\": ").append(toJsonMap(getRoleScores("TANK"))).append(",\n");
        json.append("  \"robotScoresSUP\": ").append(toJsonMap(getRoleScores("SUPPORT"))).append(",\n");
        json.append("  \"robotScoresALL\": ").append(toJsonMap(getRoleScores("ALLROUND"))).append(",\n");
        json.append("  \"victoryTypes\": ").append(toJsonMap(victoryTypes)).append(",\n");
        json.append("  \"allyPairs\": ").append(toJsonPairStats()).append(",\n");
        json.append("  \"matchupDominance\": ").append(toJsonMap(matchupDominance)).append("\n");
        json.append("}\n");
        Files.writeString(path, json.toString());
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
