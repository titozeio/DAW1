package com.titozeio.ai;

import com.titozeio.engine.Player;
import com.titozeio.match.TurnEngine;

/**
 * Controlador de jugador no humano basado en Strategy.
 */
public class AiController implements PlayerController {
    private final AiStrategy strategy;
    private final TurnEngine turnEngine;

    public AiController(AiStrategy strategy, TurnEngine turnEngine) {
        this.strategy = strategy;
        this.turnEngine = turnEngine;
    }

    @Override
    public TurnDecision decideNextStep(MatchContext context, Player player) {
        return strategy.chooseDecision(context, player, turnEngine);
    }

    @Override
    public boolean isAiControlled() {
        return true;
    }
}
