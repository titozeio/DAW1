package com.titozeio.ai;

import com.titozeio.engine.Game;

/**
 * Contexto mínimo compartido entre controladores y estrategias.
 */
public class MatchContext {
    private final Game game;

    public MatchContext(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
}
