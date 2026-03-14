package com.titozeio.match;

import java.util.List;
import java.util.Map;

/**
 * Resultado resumido de una partida simulada.
 */
public record MatchResult(
                long matchId,
                long seed,
                String difficultyP1,
                String difficultyP2,
                List<String> teamP1,
                List<String> teamP2,
                String winner,
                String victoryType,
                int turnCount,
                List<String> survivorsP1,
                List<String> survivorsP2,
                List<String> destroyedP1,
                List<String> destroyedP2,
                Map<String, Integer> killsByRobot,
                Map<String, Integer> damageDealtByRobot,
                Map<String, Integer> damageReceivedByRobot) {
}
