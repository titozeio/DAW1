package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.match.MatchResult;
import com.titozeio.telemetry.SimulationStats;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SimulationStatsCoreTest {

    @Test
    void simulationStatsAggregateRankAndExportResults() throws Exception {
        SimulationStats stats = new SimulationStats();

        MatchResult p1Win = new MatchResult(
                1L,
                111L,
                "EASY",
                "EASY",
                List.of("A", "B", "C"),
                List.of("D", "E", "F"),
                "Jugador 1",
                "eliminación",
                5,
                List.of("A", "B"),
                List.of(),
                List.of("C"),
                List.of("D", "E", "F"),
                Map.of("A", 2, "B", 1),
                Map.of("A", 10, "B", 5),
                Map.of("C", 10, "D", 15));

        MatchResult timeout = new MatchResult(
                2L,
                222L,
                "HARD",
                "MEDIUM",
                List.of("A", "D", "E"),
                List.of("B", "C", "F"),
                "Empate",
                "timeout",
                8,
                List.of("A"),
                List.of("B"),
                List.of("D", "E"),
                List.of("C", "F"),
                Map.of("F", 1),
                Map.of("F", 5),
                Map.of("A", 5));

        stats.record(p1Win);
        stats.record(timeout);

        assertEquals(2, stats.getResults().size());
        assertEquals(1, stats.getRobotWins().get("A"));
        assertEquals(1, stats.getRobotWins().get("B"));
        assertEquals(1, stats.getRobotTimeouts().get("A"));
        assertEquals(1, stats.getRobotTimeouts().get("B"));
        assertEquals(2, stats.getRobotSurvivals().get("A"));
        assertEquals(2, stats.getRobotDeaths().get("C"));
        assertEquals(2, stats.getRobotKills().get("A"));
        assertEquals(1, stats.getRobotKills().get("F"));
        assertEquals(1, stats.getVictoryTypes().get("eliminación"));
        assertEquals(1, stats.getVictoryTypes().get("timeout"));

        Map<String, Integer> scores = stats.getRoleScores("ALLROUND");
        assertTrue(scores.containsKey("A"));
        assertTrue(scores.get("B") > scores.get("C"));

        var topPairs = stats.getTopAllyPairs(5);
        assertFalse(topPairs.isEmpty());
        assertTrue(topPairs.stream().anyMatch(pair -> pair.pairName().contains("A + B")));

        var dominance = stats.getTopMatchupDominance(10);
        assertFalse(dominance.isEmpty());
        assertTrue(dominance.stream().anyMatch(entry -> entry.getKey().equals("A > D")));

        Path csv = Files.createTempFile("daw1-sim-stats", ".csv");
        Path json = Files.createTempFile("daw1-sim-stats", ".json");

        stats.exportCsv(csv);
        stats.exportJson(json);

        String csvContent = Files.readString(csv);
        String jsonContent = Files.readString(json);
        assertTrue(csvContent.contains("matchId,seed,difficultyP1"));
        assertTrue(csvContent.contains("\"Jugador 1\""));
        assertTrue(jsonContent.contains("\"matches\": 2"));
        assertTrue(jsonContent.contains("\"robotWins\""));
        assertTrue(jsonContent.contains("\"allyPairs\""));
        assertTrue(jsonContent.contains("\"matchupDominance\""));
    }
}
