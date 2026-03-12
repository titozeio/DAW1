package com.titozeio.ai;

import com.titozeio.engine.Player;
import com.titozeio.match.TurnEngine;

/**
 * Estrategia intercambiable de toma de decisiones para la IA.
 */
public interface AiStrategy {
    TurnDecision chooseDecision(MatchContext context, Player player, TurnEngine turnEngine);
}
