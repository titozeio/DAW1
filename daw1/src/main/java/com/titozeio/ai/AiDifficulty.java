package com.titozeio.ai;

/**
 * Niveles de dificultad previstos para la IA.
 */
public enum AiDifficulty {
    EASY("Fácil"),
    MEDIUM("Medio"),
    HARD("Difícil"),
    DARK_SOULS("DARK SOULS");

    private final String displayName;

    AiDifficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
