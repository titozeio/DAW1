package com.titozeio.ai;

import com.titozeio.engine.Player;

/**
 * Marcador para jugadores humanos. La UI gestiona sus decisiones.
 */
public class HumanController implements PlayerController {
    @Override
    public TurnDecision decideNextStep(MatchContext context, Player player) {
        return null;
    }
}
