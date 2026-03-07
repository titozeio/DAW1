package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.engine.Game;
import com.titozeio.engine.Launcher;
import com.titozeio.engine.Main;
import com.titozeio.engine.Player;
import com.titozeio.ui.PauseScreen;
import com.titozeio.ui.Screen;
import com.titozeio.ui.VictoryScreen;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UiAndMainTest {

    @BeforeAll
    static void initFx() throws Exception {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ignored) {
            // JavaFX toolkit already initialized.
        }
    }

    @Test
    void pauseScreenPrintsAndHandlesOptions() {
        PauseScreen pause = new PauseScreen();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(out));
        try {
            pause.display();
            pause.handleInput("1");
            pause.handleInput("2");
            pause.handleInput("x");
        } finally {
            System.setOut(old);
        }
        String text = out.toString();
        assertTrue(text.contains("PAUSA"));
        assertTrue(text.contains("Reanudando"));
        assertTrue(text.contains("rendido"));
        assertTrue(text.contains("no válida"));
    }

    @Test
    void screenApplyGlobalStyleDoesNotFailWhenCssExistsOrNot() {
        class TestScreen extends Screen {
            @Override
            public void display() {
            }

            @Override
            public void handleInput(String input) {
            }

            public void apply(Scene scene) {
                applyGlobalStyle(scene);
            }
        }

        TestScreen s = new TestScreen();
        Scene scene = new Scene(new Group(), 100, 100);
        assertDoesNotThrow(() -> s.apply(scene));
    }

    @Test
    void victoryScreenWinnerGetterAndInputPath() {
        Player winner = new Player("Winner");
        Game game = new Game(null);
        VictoryScreen screen = new VictoryScreen(null, game, winner);
        assertEquals(winner, screen.getWinner());
        assertDoesNotThrow(screen::display);
        assertDoesNotThrow(() -> screen.handleInput("x"));
    }

    @Test
    void mainStartSetsStageProperties() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] failure = new Throwable[1];

        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                Main main = new Main();
                main.start(stage);
                assertEquals("Devastation AI Wars 1", stage.getTitle());
                assertEquals(1280.0, stage.getWidth(), 1.0);
                assertEquals(755.0, stage.getHeight(), 1.0);
                stage.hide();
            } catch (Throwable t) {
                failure[0] = t;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        if (failure[0] != null) {
            fail(failure[0]);
        }
    }

    @Test
    void launcherCanBeInstantiatedViaReflection() throws Exception {
        // Cubre el constructor implícito de la clase Launcher
        Constructor<Launcher> ctor = Launcher.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Launcher launcher = ctor.newInstance();
        assertNotNull(launcher);
    }

    @Test
    void launcherMainMethodExistsWithCorrectSignature() throws Exception {
        // Verificamos via reflexión que Launcher tiene un método main(String[])
        // público y estático. No lo invocamos porque Application.launch() es
        // bloqueante y colgaría la suite de tests.
        java.lang.reflect.Method mainMethod = Launcher.class.getDeclaredMethod("main", String[].class);
        assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()),
                "main() debe ser public");
        assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()),
                "main() debe ser static");
        assertEquals(void.class, mainMethod.getReturnType(),
                "main() debe retornar void");
    }

}
