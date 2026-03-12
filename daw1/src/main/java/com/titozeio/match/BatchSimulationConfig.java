package com.titozeio.match;

import com.titozeio.ai.AiDifficulty;
import com.titozeio.enums.RobotTemplate;

import java.util.List;

/**
 * Configuración de una tanda de simulaciones.
 */
public record BatchSimulationConfig(
        int matchCount,
        long seed,
        int maxTurns,
        AiDifficulty difficultyP1,
        AiDifficulty difficultyP2,
        List<RobotTemplate> teamP1,
        List<RobotTemplate> teamP2) {
}
