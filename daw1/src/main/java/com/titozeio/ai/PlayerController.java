package com.titozeio.ai;

import com.titozeio.engine.Player;

/**
 * Contrato común para control humano o IA.
 */
public interface PlayerController {
    TurnDecision decideNextStep(MatchContext context, Player player);

    default boolean isAiControlled() {
        return false;
    }
}
