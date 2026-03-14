package com.titozeio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.titozeio.ai.AiDifficulty;
import com.titozeio.engine.Game;
import java.lang.reflect.Field;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.Test;

class GamePreferencesTest {

    @Test
    void firstRunUsesExpectedDefaultsAndLaterRunsPreserveUserChoices() throws Exception {
        Preferences prefs = gamePreferences();

        String oldMusic = prefs.get("musicEnabled", null);
        String oldSfx = prefs.get("sfxEnabled", null);
        String oldDifficulty = prefs.get("aiDifficulty", null);
        String oldFirstRun = prefs.get("firstRunInitialized", null);

        try {
            prefs.remove("musicEnabled");
            prefs.remove("sfxEnabled");
            prefs.remove("aiDifficulty");
            prefs.remove("firstRunInitialized");

            Game firstRunGame = new Game(null);
            assertTrue(firstRunGame.isMusicEnabled());
            assertTrue(firstRunGame.isSfxEnabled());
            assertEquals(AiDifficulty.EASY, firstRunGame.getAiDifficulty());

            firstRunGame.setMusicEnabled(false);
            firstRunGame.setSfxEnabled(false);
            firstRunGame.setAiDifficulty(AiDifficulty.HARD);

            Game nextRunGame = new Game(null);
            assertFalse(nextRunGame.isMusicEnabled());
            assertFalse(nextRunGame.isSfxEnabled());
            assertEquals(AiDifficulty.HARD, nextRunGame.getAiDifficulty());
        } finally {
            restorePreference(prefs, "musicEnabled", oldMusic);
            restorePreference(prefs, "sfxEnabled", oldSfx);
            restorePreference(prefs, "aiDifficulty", oldDifficulty);
            restorePreference(prefs, "firstRunInitialized", oldFirstRun);
        }
    }

    private static Preferences gamePreferences() throws Exception {
        Field field = Game.class.getDeclaredField("PREFS");
        field.setAccessible(true);
        return (Preferences) field.get(null);
    }

    private static void restorePreference(Preferences prefs, String key, String value) {
        if (value == null) {
            prefs.remove(key);
        } else {
            prefs.put(key, value);
        }
    }
}
