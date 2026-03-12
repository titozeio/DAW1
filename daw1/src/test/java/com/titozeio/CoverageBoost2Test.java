package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.engine.Game;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.engine.SoundManager;
import com.titozeio.engine.ActionManager;
import com.titozeio.engine.Map;
import com.titozeio.commands.MoveCommand;
import com.titozeio.commands.SkillCommand;
import com.titozeio.commands.AttackCommand;
import com.titozeio.enums.AttackType;
import com.titozeio.enums.RobotTemplate;
import com.titozeio.enums.TerrainType;
import com.titozeio.model.Weapon;
import com.titozeio.skills.*;
import com.titozeio.ui.GameScreen;
import com.titozeio.ui.MainMenuScreen;
import com.titozeio.ui.RobotSelectionScreen;
import com.titozeio.ui.Screen;
import com.titozeio.ui.SettingsOverlayController;
import com.titozeio.ui.TutorialOverlayController;
import com.titozeio.states.ExhaustedState;
import com.titozeio.states.FrozenState;
import com.titozeio.states.NormalState;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests de cobertura adicionales para SoundManager, Screen, MainMenuScreen,
 * RobotSelectionScreen y GameScreen (métodos no cubiertos por tests
 * anteriores).
 */
class CoverageBoost2Test {

    @BeforeAll
    static void initFx() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ignored) {
            // ya inicializado
        }
        Screen.setAnimationsEnabled(false);
    }

    // =========================================================================
    // SoundManager
    // =========================================================================

    @Test
    void soundManagerSetAndGetEnabled() {
        // Por defecto ON
        SoundManager.setEnabled(true);
        assertTrue(SoundManager.isEnabled());

        // Desactivar no lanza excepción
        assertDoesNotThrow(() -> SoundManager.setEnabled(false));
        assertFalse(SoundManager.isEnabled());

        // play() con sfx desactivado no hace nada (no debe lanzar)
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.MECH_MOVE));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.UI_HOVER, 0.5));

        // Reactivar
        SoundManager.setEnabled(true);
        assertTrue(SoundManager.isEnabled());
    }

    @Test
    void soundManagerPlayAllSfxDoesNotThrow() {
        // Con SFX activados — en headless/test simplemente retorna sin reproducir
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.MECH_MOVE));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.MECH_RECEIVE_HIT));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.LASER_SHOT));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.PLASMA_SABER));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.WEAPON_PLASMA_SABER));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.WEAPON_FREEZING_RAY));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.WEAPON_MISSILES));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.MISSILE_LAUNCH));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.REPAIR_NANOBOTS));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.MOBILITY_JUMP_THRUSTERS));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.MOBILITY_HYPERSPEED_DASH));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.UI_UNIT_SELECT));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.UI_CLICK_CONFIRM));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.UI_TURN_CHANGE));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.UI_ABILITY_READY));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.UI_ABILITY_COOLDOWN));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.UI_ERROR));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.UI_INVALID));
        assertDoesNotThrow(() -> SoundManager.play(SoundManager.UI_HOVER));
        // Archivo inexistente — no debe lanzar
        assertDoesNotThrow(() -> SoundManager.play("nonexistent_file.wav"));
    }

    @Test
    void soundManagerAttachButtonSoundsDoesNotThrow() {
        Button btn = new Button("Test");
        assertDoesNotThrow(() -> SoundManager.attachButtonSounds(btn));
        assertDoesNotThrow(() -> SoundManager.attachButtonSounds(null)); // null-safe
    }

    @Test
    void soundManagerAttachButtonSoundsToAllDoesNotThrow() {
        VBox root = new VBox(new Button("A"), new Button("B"), new Label("C"));
        assertDoesNotThrow(() -> SoundManager.attachButtonSoundsToAll(root));
        assertDoesNotThrow(() -> SoundManager.attachButtonSoundsToAll(null)); // null-safe
    }

    // =========================================================================
    // Screen — initButtonSounds / enforceUppercaseText
    // =========================================================================

    @Test
    void screenInitButtonSoundsAndEnforceUppercase() {
        class TestScreen extends Screen {
            @Override
            /**
             * Ejecuta la logica de display.
             */
            public void display() {
            }

            @Override
            /**
             * Ejecuta la logica de handle input.
             * @param i parametro de entrada.
             */
            public void handleInput(String i) {
            }

            /**
             * Ejecuta la logica de call init sounds.
             * @param p parametro de entrada.
             */
            public void callInitSounds(javafx.scene.Parent p) {
                initButtonSounds(p);
            }

            /**
             * Ejecuta la logica de call enforce uppercase.
             * @param p parametro de entrada.
             */
            public void callEnforceUppercase(javafx.scene.Parent p) {
                enforceUppercaseText(p);
            }
        }
        TestScreen s = new TestScreen();
        VBox root = new VBox();
        Button btn = new Button("click me");
        Label lbl = new Label("hello world");
        Text txt = new Text("test text");
        root.getChildren().addAll(btn, lbl, new javafx.scene.layout.StackPane(txt));

        assertDoesNotThrow(() -> s.callInitSounds(root));
        assertDoesNotThrow(() -> s.callEnforceUppercase(root));

        // Los textos deben haberse convertido a mayúsculas
        assertEquals("HELLO WORLD", lbl.getText());
    }

    // =========================================================================
    // MainMenuScreen / SettingsOverlayController — handlers (sin FXML)
    // =========================================================================

    @Test
    void mainMenuScreenHandlersViaCoverage() throws Exception {
        MainMenuScreen m = new MainMenuScreen();
        Game g = new Game(null);

        set(m, "game", g);

        SettingsOverlayController settings = new SettingsOverlayController();
        StackPane settingsOverlay = new StackPane();
        settingsOverlay.setVisible(false);
        set(settings, "settingsOverlay", settingsOverlay);
        set(settings, "musicToggle", new CheckBox());
        set(settings, "sfxToggle", new CheckBox());
        set(m, "settingsOverlayComponentController", settings);

        TutorialOverlayController tutorial = new TutorialOverlayController();
        StackPane tutorialOverlay = new StackPane();
        tutorialOverlay.setVisible(false);
        set(tutorial, "tutorialOverlay", tutorialOverlay);
        set(m, "tutorialOverlayComponentController", tutorial);

        assertDoesNotThrow(m::handleSettingsOpenAction);
        assertTrue(settingsOverlay.isVisible());

        assertDoesNotThrow(m::handleTutorialOpenAction);
        assertTrue(tutorialOverlay.isVisible());

        assertDoesNotThrow(m::handleTutorialCloseAction);
        assertFalse(tutorialOverlay.isVisible());

        assertDoesNotThrow(() -> m.handleInput("OTHER"));
    }

    @Test
    void settingsOverlayControllerHandlersViaCoverage() throws Exception {
        SettingsOverlayController c = new SettingsOverlayController();
        Game g = new Game(null);

        StackPane overlay = new StackPane();
        overlay.setVisible(false);
        set(c, "settingsOverlay", overlay);
        CheckBox musicCb = new CheckBox();
        musicCb.setSelected(true);
        set(c, "musicToggle", musicCb);
        CheckBox sfxCb = new CheckBox();
        sfxCb.setSelected(true);
        set(c, "sfxToggle", sfxCb);

        assertDoesNotThrow(() -> c.show(g));
        assertTrue(overlay.isVisible());

        assertDoesNotThrow(c::handleSettingsCloseAction);
        assertFalse(overlay.isVisible());

        musicCb.setSelected(false);
        assertDoesNotThrow(c::handleMusicToggleAction);
        assertEquals("OFF", musicCb.getText());
        assertFalse(g.isMusicEnabled());

        musicCb.setSelected(true);
        assertDoesNotThrow(c::handleMusicToggleAction);
        assertEquals("ON", musicCb.getText());
        assertTrue(g.isMusicEnabled());

        sfxCb.setSelected(false);
        assertDoesNotThrow(c::handleSfxToggleAction);
        assertFalse(SoundManager.isEnabled());
        assertEquals("OFF", sfxCb.getText());
        assertFalse(g.isSfxEnabled());

        sfxCb.setSelected(true);
        assertDoesNotThrow(c::handleSfxToggleAction);
        assertTrue(SoundManager.isEnabled());
        assertEquals("ON", sfxCb.getText());
        assertTrue(g.isSfxEnabled());
    }

    // =========================================================================
    // RobotSelectionScreen — navegación prev/next, skip, overlay
    // =========================================================================

    @Test
    void robotSelectionNavigationAndSkip() throws Exception {
        RobotSelectionScreen s = new RobotSelectionScreen();

        // Inyectar campos mínimos para que no lance NPE
        set(s, "fixedInstructionsLabel", new Label());
        set(s, "turnInstructionsLabel", new Label());
        set(s, "prevRobotButton", new Button());
        set(s, "nextRobotButton", new Button());
        set(s, "confirmButton", new Button());
        set(s, "conceptImageView", new javafx.scene.image.ImageView());
        set(s, "statHpValueLabel", new Label());
        set(s, "statMoveValueLabel", new Label());
        set(s, "statDamageValueLabel", new Label());
        set(s, "statRangeValueLabel", new Label());
        set(s, "skillTitleLabel", new Label());
        set(s, "skillTypeValueLabel", new Label());
        set(s, "skillCooldownValueLabel", new Label());
        set(s, "skillStatsValueLabel", new Label());
        set(s, "descRobotLabel", new Label());
        set(s, "descWeaponLabel", new Label());
        set(s, "descSkillLabel", new Label());
        set(s, "p1Slot1Image", new javafx.scene.image.ImageView());
        set(s, "p2Slot1Image", null); // fuerza el early return de updateBottomTeams
        set(s, "turnOverlay", null); // fuerza el early return de showTurnOverlay

        // Inicializar datos
        Method init = RobotSelectionScreen.class.getDeclaredMethod("initializeData", List.class);
        init.setAccessible(true);
        init.invoke(s, new ArrayList<>(List.of(RobotTemplate.values())));

        // Navegar prev con un solo elemento disponible (no debe lanzar)
        // con múltiples
        assertDoesNotThrow(s::handlePrevRobotAction);
        assertDoesNotThrow(s::handleNextRobotAction);

        // handleDismissTurnOverlay con overlay null — debe ser null-safe
        // (la condición early-return lo cubre)
        assertDoesNotThrow(s::handleDismissTurnOverlay);
    }

    @Test
    void robotSelectionSkipAction() throws Exception {
        UiCoverageBoostTest.TestGame tg = new UiCoverageBoostTest.TestGame();
        RobotSelectionScreen s = new RobotSelectionScreen();

        set(s, "fixedInstructionsLabel", new Label());
        set(s, "turnInstructionsLabel", new Label());
        set(s, "prevRobotButton", new Button());
        set(s, "nextRobotButton", new Button());
        set(s, "confirmButton", new Button());
        set(s, "conceptImageView", new javafx.scene.image.ImageView());
        set(s, "statHpValueLabel", new Label());
        set(s, "statMoveValueLabel", new Label());
        set(s, "statDamageValueLabel", new Label());
        set(s, "statRangeValueLabel", new Label());
        set(s, "skillTitleLabel", new Label());
        set(s, "skillTypeValueLabel", new Label());
        set(s, "skillCooldownValueLabel", new Label());
        set(s, "skillStatsValueLabel", new Label());
        set(s, "descRobotLabel", new Label());
        set(s, "descWeaponLabel", new Label());
        set(s, "descSkillLabel", new Label());
        set(s, "p1Slot1Image", null);
        set(s, "p2Slot1Image", null);
        set(s, "turnOverlay", null);
        set(s, "game", tg);
        set(s, "player1Templates", new ArrayList<>());
        set(s, "player2Templates", new ArrayList<>());

        Method init = RobotSelectionScreen.class.getDeclaredMethod("initializeData", List.class);
        init.setAccessible(true);
        init.invoke(s, new ArrayList<>(List.of(RobotTemplate.values())));

        // handleSkipAction reparte todos los robots al azar y transiciona
        assertDoesNotThrow(s::handleSkipAction);
        assertNotNull(tg.displayed);
    }

    // =========================================================================
    // GameScreen — métodos no cubiertos previamente
    // =========================================================================

    @Test
    void gameScreenHandleInfoToggleAndLogToggle() throws Exception {
        GameScreen s = buildMinimalGameScreen();

        // handleInfoToggleAction sin robot seleccionado → no debe lanzar
        assertDoesNotThrow(s::handleInfoToggleAction);

        // Con robot seleccionado
        Player p1 = ((Game) get(s, "game")).getP1();
        Robot r = TestFixtures.robot("X", p1, 10, 3, 2, 2, new JetpackBoost());
        set(s, "selectedRobot", r);
        assertDoesNotThrow(s::handleInfoToggleAction); // skill mode ON
        assertDoesNotThrow(s::handleInfoToggleAction); // skill mode OFF

        // handleLogToggleAction — expande y repliega
        assertDoesNotThrow(s::handleLogToggleAction);
        assertDoesNotThrow(s::handleLogToggleAction);
    }

    @Test
    void gameScreenRenderSkillCooldown() throws Exception {
        GameScreen s = buildMinimalGameScreen();

        // Con skill null
        Method m = GameScreen.class.getDeclaredMethod("renderSkillCooldown", com.titozeio.skills.Skill.class);
        m.setAccessible(true);
        assertDoesNotThrow(() -> m.invoke(s, (Object) null));

        // Skill lista (cooldown=0)
        Skill ready = new JetpackBoost();
        assertDoesNotThrow(() -> m.invoke(s, ready));

        // Skill en cooldown — forzamos currentCooldown via reflexión
        Skill cd = new FreezeRay();
        Field cdField = Skill.class.getDeclaredField("currentCooldown");
        cdField.setAccessible(true);
        cdField.setInt(cd, 2);
        assertDoesNotThrow(() -> m.invoke(s, cd));
    }

    @Test
    void gameScreenRenderSegmentedHpBar() throws Exception {
        GameScreen s = buildMinimalGameScreen();
        Method m = GameScreen.class.getDeclaredMethod("renderSegmentedHpBar",
                StackPane.class, int.class, int.class);
        m.setAccessible(true);

        StackPane bar = new StackPane();
        bar.setPrefSize(130, 10);

        assertDoesNotThrow(() -> m.invoke(s, bar, 5, 10)); // normal
        assertDoesNotThrow(() -> m.invoke(s, bar, 0, 10)); // vacío
        assertDoesNotThrow(() -> m.invoke(s, bar, 10, 10)); // lleno
        assertDoesNotThrow(() -> m.invoke(s, bar, 0, 0)); // maxHp=0 → early return
        assertDoesNotThrow(() -> m.invoke(s, (Object) null, 5, 10)); // null → early return
    }

    @Test
    void gameScreenPlayWeaponAndSkillSfx() throws Exception {
        GameScreen s = buildMinimalGameScreen();
        Player p = ((Game) get(s, "game")).getP1();

        Method ws = GameScreen.class.getDeclaredMethod("playWeaponSfx", Robot.class);
        ws.setAccessible(true);

        // null robot
        assertDoesNotThrow(() -> ws.invoke(s, (Object) null));

        // ENERGY
        Robot energy = robotWithWeapon(p, AttackType.ENERGY, 3);
        assertDoesNotThrow(() -> ws.invoke(s, energy));

        // EXPLOSIVE
        Robot explosive = robotWithWeapon(p, AttackType.EXPLOSIVE, 4);
        assertDoesNotThrow(() -> ws.invoke(s, explosive));

        // KINETIC melee (range 1)
        Robot melee = robotWithWeapon(p, AttackType.KINETIC, 1);
        assertDoesNotThrow(() -> ws.invoke(s, melee));

        // KINETIC ranged (range > 1)
        Robot ranged = robotWithWeapon(p, AttackType.KINETIC, 3);
        assertDoesNotThrow(() -> ws.invoke(s, ranged));

        // playSkillSfx — todas las ramas del switch
        Method ss = GameScreen.class.getDeclaredMethod("playSkillSfx", String.class);
        ss.setAccessible(true);
        for (String sc : List.of("FreezeRay", "GuidedMissile", "RepairNanobots",
                "JetpackBoost", "TurboPropulsion", "PlasmaSaberAttack", "Unknown")) {
            assertDoesNotThrow(() -> ss.invoke(s, sc));
        }
    }

    @Test
    void gameScreenResolveOwnerBadgeAndExtractStat() throws Exception {
        GameScreen s = buildMinimalGameScreen();

        Method rob = GameScreen.class.getDeclaredMethod("resolveOwnerBadge", Robot.class);
        rob.setAccessible(true);

        Player p1 = new Player("Jugador 1");
        Player p2 = new Player("Jugador 2");
        Robot r1 = TestFixtures.robot("A", p1, 5, 2, 2, 1, null);
        Robot r2 = TestFixtures.robot("B", p2, 5, 2, 2, 1, null);
        Robot rNull = TestFixtures.robot("C", null, 5, 2, 2, 1, null);

        assertEquals("P1", rob.invoke(s, r1));
        assertEquals("P2", rob.invoke(s, r2));
        assertEquals("", rob.invoke(s, rNull));
        assertEquals("", rob.invoke(s, (Object) null));

        Method es = GameScreen.class.getDeclaredMethod("extractStat", Skill.class, String.class);
        es.setAccessible(true);
        Skill sk = new FreezeRay();
        String alcance = (String) es.invoke(s, sk, "Alcance");
        assertNotNull(alcance);
        // Clave inexistente → "—"
        String missing = (String) es.invoke(s, sk, "XYZ_MISSING");
        assertEquals("-", missing);
    }

    @Test
    void gameScreenUpdateRobotInfoPanelAllBranches() throws Exception {
        GameScreen s = buildMinimalGameScreen();

        // Inyectar campos para infoPanel
        set(s, "infoPanel", new VBox());
        set(s, "infoTitleLabel", new Label());
        set(s, "ownerBadgeLabel", new Label());
        set(s, "infoPortraitImage", new javafx.scene.image.ImageView());
        set(s, "hpBarContainer", new StackPane());
        set(s, "hpValueLabel", new Label());
        set(s, "skillNameInfoLabel", new Label());
        set(s, "skillCooldownDots", new HBox());
        set(s, "skillReadyLabel", new Label());
        set(s, "robotInfoBox", new VBox());
        set(s, "skillInfoBox", new VBox());
        set(s, "skillTypeInfoLabel", new Label());
        set(s, "skillDetailLine1Label", new Label());
        set(s, "skillDetailLine2Label", new Label());
        set(s, "moveValueLabel", new Label());
        set(s, "rangeValueLabel", new Label());
        set(s, "damageValueLabel", new Label());
        set(s, "infoToggleButton", new Button());

        Method upd = GameScreen.class.getDeclaredMethod("updateRobotInfoPanel", Robot.class);
        upd.setAccessible(true);

        Player p1 = ((Game) get(s, "game")).getP1();

        // null
        assertDoesNotThrow(() -> upd.invoke(s, (Object) null));

        // Robot normal con skill ATTACK (FreezeRay)
        Robot rFreeze = TestFixtures.robot("Ice", p1, 10, 4, 3, 3, new FreezeRay());
        assertDoesNotThrow(() -> upd.invoke(s, rFreeze));

        // infoSkillMode=true con skill MOVEMENT
        set(s, "infoSkillMode", true);
        Robot rJet = TestFixtures.robot("Scout", p1, 7, 5, 5, 3, new JetpackBoost());
        assertDoesNotThrow(() -> upd.invoke(s, rJet));

        // infoSkillMode=true con skill de curación (RepairNanobots)
        Robot rRepair = TestFixtures.robot("Bulwark", p1, 15, 4, 3, 1, new RepairNanobots());
        assertDoesNotThrow(() -> upd.invoke(s, rRepair));

        // Robot sin skill
        set(s, "infoSkillMode", false);
        Robot rNoSkill = TestFixtures.robot("Bare", p1, 5, 3, 2, 2, null);
        assertDoesNotThrow(() -> upd.invoke(s, rNoSkill));
    }

    @Test
    void gameScreenHandleCombatClickBranches() throws Exception {
        GameScreen s = buildMinimalGameScreen();
        Game g = (Game) get(s, "game");
        g.setPhase(com.titozeio.engine.GamePhase.COMBAT);

        Player p1 = g.getP1();
        Player p2 = g.getP2();
        Robot own = TestFixtures.robot("Own", p1, 10, 5, 3, 3, new JetpackBoost());
        Robot enemy = TestFixtures.robot("Enemy", p2, 10, 4, 3, 3, null);

        Hexagon ha = g.getMap().getHexagon(0, 0);
        Hexagon hb = g.getMap().getHexagon(1, 0);
        own.setPosition(ha);
        ha.setOccupant(own);
        enemy.setPosition(hb);
        hb.setOccupant(enemy);

        set(s, "selectedRobot", own);

        Method cc = GameScreen.class.getDeclaredMethod("handleCombatClick", Hexagon.class);
        cc.setAccessible(true);

        // Click en propio (mismo robot → cycleModes)
        assertDoesNotThrow(() -> cc.invoke(s, ha));
        // Click en enemigo → updateRobotInfoPanel
        assertDoesNotThrow(() -> cc.invoke(s, hb));
        // Click en vacío
        Hexagon empty = g.getMap().getHexagon(2, 0);
        assertDoesNotThrow(() -> cc.invoke(s, empty));
    }

    @Test
    void gameScreenDeployPhaseP2Transition() throws Exception {
        GameScreen s = buildMinimalGameScreen();
        Game g = (Game) get(s, "game");
        g.setPhase(com.titozeio.engine.GamePhase.DEPLOYING);

        Player p1 = g.getP1();
        Player p2 = g.getP2();

        Robot r1 = RobotTemplate.SCOUT.createRobot(p1);
        Robot r2 = RobotTemplate.BULLSEYE.createRobot(p1);
        Robot r3 = RobotTemplate.BULWARK.createRobot(p1);
        p1.getUnits().clear();
        p1.addUnit(r1);
        p1.addUnit(r2);
        p1.addUnit(r3);

        Robot e1 = RobotTemplate.ICE_AGE.createRobot(p2);
        Robot e2 = RobotTemplate.DEATH_KNIGHT.createRobot(p2);
        Robot e3 = RobotTemplate.VICTORY_SABER.createRobot(p2);
        p2.getUnits().clear();
        p2.addUnit(e1);
        p2.addUnit(e2);
        p2.addUnit(e3);

        set(s, "deployingPlayer", p1);
        java.util.Queue<Robot> q = new java.util.LinkedList<>(p1.getUnits());
        set(s, "robotsToPlace", q);

        Method hdDeploy = GameScreen.class.getDeclaredMethod("handleDeployClick", Hexagon.class);
        hdDeploy.setAccessible(true);

        // Clic en zona no válida
        Hexagon wrongZone = g.getMap().getHexagon(3, 3);
        assertDoesNotThrow(() -> hdDeploy.invoke(s, wrongZone));

        // Colocar un robot en zona P1 válida
        for (Hexagon[] row : g.getMap().getGrid()) {
            for (Hexagon hex : row) {
                if (hex != null && hex.isDeployZoneP1() && !hex.isOccupied()) {
                    assertDoesNotThrow(() -> hdDeploy.invoke(s, hex));
                    return; // una sola colocación es suficiente
                }
            }
        }
    }

    // =========================================================================
    // Game — métodos no cubiertos
    // =========================================================================

    @Test
    void gameNextTurnAlternatesPlayer() {
        Game g = new Game(null);
        Player p1 = g.getP1();
        Player p2 = g.getP2();

        assertEquals(p1, g.getCurrentPlayer());
        assertEquals(1, g.getTurnCounter());

        g.nextTurn();
        assertEquals(p2, g.getCurrentPlayer());
        assertEquals(2, g.getTurnCounter());

        g.nextTurn();
        assertEquals(p1, g.getCurrentPlayer());
        assertEquals(3, g.getTurnCounter());
    }

    @Test
    void gameGetOpponentReturnsCorrectPlayer() {
        Game g = new Game(null);
        assertEquals(g.getP2(), g.getOpponent(g.getP1()));
        assertEquals(g.getP1(), g.getOpponent(g.getP2()));
    }


    @Test
    void gameSetMusicEnabledFalseStopsMusic() {
        Game g = new Game(null);
        assertTrue(g.isMusicEnabled());

        // Desactivar música con mediaPlayer null (stage=null → no reproduce)
        assertDoesNotThrow(() -> g.setMusicEnabled(false));
        assertFalse(g.isMusicEnabled());

        // Reactivar con screen nula (no lanza, simplemente no hace nada)
        assertDoesNotThrow(() -> g.setMusicEnabled(true));
        assertTrue(g.isMusicEnabled());
    }

    @Test
    void gameSetMusicEnabledWithActiveScreenDoesNotThrow() throws Exception {
        Game g = new Game(null);

        // Inyectamos una pantalla "activa" para que se ejecute la rama
        // updateMusicForScreen cuando se reactiva la música
        com.titozeio.ui.PauseScreen fakeScreen = new com.titozeio.ui.PauseScreen();
        Field currentScreenField = Game.class.getDeclaredField("currentScreen");
        currentScreenField.setAccessible(true);
        currentScreenField.set(g, fakeScreen);

        g.setMusicEnabled(false);
        assertDoesNotThrow(() -> g.setMusicEnabled(true)); // rama: currentScreen != null
    }

    @Test
    void gameSettersAndGetters() {
        Game g = new Game(null);

        Player newP1 = new Player("Custom P1");
        Player newP2 = new Player("Custom P2");
        g.setP1(newP1);
        g.setP2(newP2);
        assertEquals(newP1, g.getP1());
        assertEquals(newP2, g.getP2());

        g.setPhase(com.titozeio.engine.GamePhase.COMBAT);
        assertEquals(com.titozeio.engine.GamePhase.COMBAT, g.getPhase());

        assertTrue(g.isSfxEnabled());
        g.setSfxEnabled(false);
        assertFalse(g.isSfxEnabled());
        g.setSfxEnabled(true);

        assertNull(g.getStage()); // construido con null
    }

    @Test
    void gameResolveNextTrackNumberViaReflection() throws Exception {
        Game g = new Game(null);

        // Accedemos al método privado resolveNextTrackNumber
        // MusicTheme es inner private, lo obtenemos por reflexión
        Class<?>[] innerClasses = Game.class.getDeclaredClasses();
        Class<?> musicThemeClass = null;
        for (Class<?> c : innerClasses) {
            if (c.getSimpleName().equals("MusicTheme")) {
                musicThemeClass = c;
                break;
            }
        }
        assertNotNull(musicThemeClass, "MusicTheme inner class debe existir");

        Object menuTheme = musicThemeClass.getEnumConstants()[0]; // MENU

        Method resolve = Game.class.getDeclaredMethod(
                "resolveNextTrackNumber", musicThemeClass, int.class);
        resolve.setAccessible(true);

        // Track 1 → si no existe track 2, vuelve a 1
        int next = (int) resolve.invoke(g, menuTheme, 1);
        assertTrue(next == 1 || next == 2, "Debe ser 1 o 2");

        // Track 99 → casi seguro vuelve a 1
        int backTo1 = (int) resolve.invoke(g, menuTheme, 99);
        assertEquals(1, backTo1);
    }

    @Test
    void gameDisplayScreenWithNullStageDoesNotThrow() {
        // displayScreen sin Stage → la música no intenta reproducirse (stage==null)
        Game g = new Game(null);
        com.titozeio.ui.PauseScreen s = new com.titozeio.ui.PauseScreen();
        assertDoesNotThrow(() -> g.displayScreen(s));
    }

    // =========================================================================
    // Commands / States — subir cobertura global
    // =========================================================================

    @Test
    void actionManagerExecuteUndoAndUnsupportedUndoBranches() {
        ActionManager am = ActionManager.getInstance();
        am.clearHistory();

        AtomicBoolean flag = new AtomicBoolean(false);
        com.titozeio.commands.GameCommand ok = new com.titozeio.commands.GameCommand() {
            @Override
            public void execute() {
                flag.set(true);
            }

            @Override
            public void undo() {
                flag.set(false);
            }

            @Override
            public String getDescription() {
                return "OK";
            }
        };

        assertDoesNotThrow(() -> am.executeCommand(ok));
        assertTrue(flag.get());
        assertDoesNotThrow(am::undoLastCommand);
        assertFalse(flag.get());

        // Rama catch(UnsupportedOperationException)
        Game g = new Game(null);
        Player p = g.getP1();
        Robot r = TestFixtures.robot("Cmd", p, 5, 3, 2, 2, new JetpackBoost());
        Hexagon dest = TestFixtures.hex(0, 0, TerrainType.NORMAL, 0);
        SkillCommand skillCmd = new SkillCommand(r, dest);
        assertDoesNotThrow(() -> am.executeCommand(skillCmd));
        assertDoesNotThrow(am::undoLastCommand);

        // Historia vacía → no hace nada
        am.clearHistory();
        assertDoesNotThrow(am::undoLastCommand);
    }

    @Test
    void moveCommandExecuteUndoAndDescription() {
        Player p = new Player("P1");
        Robot r = TestFixtures.robot("Mover", p, 10, 3, 2, 2, null);

        Hexagon from = TestFixtures.hex(0, 0, TerrainType.NORMAL, 0);
        Hexagon to = TestFixtures.hex(1, 0, TerrainType.NORMAL, 0);
        from.setOccupant(r);
        r.setPosition(from);

        MoveCommand cmd = new MoveCommand(r, to);
        assertDoesNotThrow(cmd::execute);
        assertEquals(to, r.getPosition());
        assertTrue(r.isUsedMovement());

        assertDoesNotThrow(cmd::undo);
        assertEquals(from, r.getPosition());
        assertEquals(r, from.getOccupant());
        assertFalse(r.isUsedMovement());

        assertTrue(cmd.getDescription().contains("Mover"));
    }

    @Test
    void attackCommandUndoThrowsAndDescriptionDoesNotThrow() {
        Game g = new Game(null);
        Map map = g.getMap();

        Player p1 = g.getP1();
        Player p2 = g.getP2();

        Robot attacker = TestFixtures.robot("Att", p1, 10, 3, 10, 20, null);
        Robot target = TestFixtures.robot("Tgt", p2, 1, 3, 1, 1, null);

        // Buscar dos hexágonos adyacentes existentes para evitar suposiciones del mapa
        Hexagon a = null;
        Hexagon b = null;
        int[][] deltas = new int[][] { { 1, 0 }, { 0, 1 }, { -1, 1 }, { -1, 0 }, { 0, -1 }, { 1, -1 } };
        for (Hexagon[] row : map.getGrid()) {
            for (Hexagon h : row) {
                if (h == null) {
                    continue;
                }
                for (int[] d : deltas) {
                    Hexagon n = map.getHexagon(h.getQ() + d[0], h.getR() + d[1]);
                    if (n != null) {
                        a = h;
                        b = n;
                        break;
                    }
                }
                if (a != null) {
                    break;
                }
            }
            if (a != null) {
                break;
            }
        }
        assertNotNull(a, "Debe existir al menos 1 hexágono en el mapa");
        assertNotNull(b, "Debe existir al menos 1 vecino adyacente");
        attacker.setPosition(a);
        a.setOccupant(attacker);
        target.setPosition(b);
        b.setOccupant(target);

        AttackCommand cmd = new AttackCommand(attacker, target, map);
        assertDoesNotThrow(cmd::execute); // no-op en este diseño
        assertNotNull(cmd.getDescription());
        assertTrue(cmd.getDamageDealt() >= 0);
        assertDoesNotThrow(cmd::isFatal);
        assertThrows(UnsupportedOperationException.class, cmd::undo);
    }

    @Test
    void robotStateBehaviorsCoverNullAndNonNullBranches() throws Exception {
        Player p = new Player("P1");
        Skill sk = new FreezeRay();

        // Forzamos cooldown>0 para cubrir isReady=false en NormalState.canUseSkill
        Field cdField = Skill.class.getDeclaredField("currentCooldown");
        cdField.setAccessible(true);
        cdField.setInt(sk, 1);

        Robot r = TestFixtures.robot("StateBot", p, 10, 3, 2, 2, sk);

        NormalState normal = new NormalState();
        assertFalse(normal.canMove(null));
        assertFalse(normal.canAttack(null));
        assertFalse(normal.canUseSkill(null));

        r.setUsedMovement(false);
        assertTrue(normal.canMove(r));
        r.setUsedMovement(true);
        assertFalse(normal.canMove(r));

        r.setUsedAttack(false);
        assertTrue(normal.canAttack(r));
        r.setUsedAttack(true);
        assertFalse(normal.canAttack(r));

        // skill no lista (cooldown>0) → false
        assertFalse(normal.canUseSkill(r));
        cdField.setInt(sk, 0);
        assertTrue(normal.canUseSkill(r));

        // Frozen / Exhausted devuelven siempre false en can*
        FrozenState frozen = new FrozenState();
        ExhaustedState exhausted = new ExhaustedState();
        assertFalse(frozen.canMove(r));
        assertFalse(frozen.canAttack(r));
        assertFalse(frozen.canUseSkill(r));
        assertDoesNotThrow(() -> frozen.onTurnBegin(r));

        assertFalse(exhausted.canMove(r));
        assertFalse(exhausted.canAttack(r));
        assertFalse(exhausted.canUseSkill(r));
        assertDoesNotThrow(() -> exhausted.onTurnBegin(r));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Construye un GameScreen con los campos mínimos para no lanzar NPE. */
    private static GameScreen buildMinimalGameScreen() throws Exception {
        GameScreen s = new GameScreen();
        Game g = new Game(null);
        set(s, "game", g);
        set(s, "window", null);
        set(s, "scene", null);
        set(s, "hexRenderer", new com.titozeio.ui.HexRenderer(new Pane()));
        set(s, "hexMapPane", new Pane());
        set(s, "turnLabel", new Label());
        set(s, "objectiveShortLabel", new Label());
        set(s, "infoDetailsLabel", new Label());
        ScrollPane logSP = new ScrollPane();
        logSP.setContent(new VBox());
        set(s, "logScrollPane", logSP);
        VBox logContent = new VBox();
        set(s, "combatLogContent", logContent);
        set(s, "latestLogLabel", new Label());
        Button skillBtn = new Button();
        set(s, "skillButton", skillBtn);
        Button endBtn = new Button();
        set(s, "endTurnButton", endBtn);
        StackPane overlay = new StackPane();
        overlay.setVisible(false);
        set(s, "overlayPane", overlay);
        set(s, "overlayTitle", new Label());
        set(s, "overlayMessage", new Label());
        set(s, "overlayAcceptButton", new Button());
        StackPane vPane = new StackPane();
        vPane.setVisible(false);
        set(s, "victoryPane", vPane);
        set(s, "victoryIcon", new Label());
        set(s, "victoryTitle", new Label());
        set(s, "victoryReason", new Label());
        set(s, "victoryTurnLabel", new Label());
        set(s, "victoryMenuButton", new Button());
        set(s, "logToggleButton", new Button());
        set(s, "logPanelBox", new VBox());
        set(s, "infoPanel", null);
        set(s, "infoToggleButton", new Button());
        set(s, "skillCooldownDots", new HBox());
        set(s, "skillReadyLabel", new Label());
        set(s, "hpBarContainer", new StackPane());
        set(s, "hpValueLabel", new Label());
        set(s, "skillNameInfoLabel", new Label());
        set(s, "skillTypeInfoLabel", new Label());
        set(s, "skillDetailLine1Label", new Label());
        set(s, "skillDetailLine2Label", new Label());
        set(s, "robotInfoBox", new VBox());
        set(s, "skillInfoBox", new VBox());
        set(s, "moveValueLabel", new Label());
        set(s, "rangeValueLabel", new Label());
        set(s, "damageValueLabel", new Label());
        set(s, "infoTitleLabel", new Label());
        set(s, "ownerBadgeLabel", new Label());
        set(s, "infoPortraitImage", new javafx.scene.image.ImageView());
        set(s, "deployingPlayer", g.getP1());
        set(s, "robotsToPlace", new java.util.LinkedList<>());
        return s;
    }

    private static Robot robotWithWeapon(Player owner, AttackType type, int range) {
        Weapon w = new Weapon("W", range, 3, "desc", type);
        return new Robot("R", "desc", 10, 3, w, null, owner);
    }

    private static void set(Object o, String field, Object value) throws Exception {
        Class<?> cls = o.getClass();
        while (cls != null) {
            try {
                Field f = cls.getDeclaredField(field);
                f.setAccessible(true);
                f.set(o, value);
                return;
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            }
        }
        throw new NoSuchFieldException(field + " not found in " + o.getClass());
    }

    private static Object get(Object o, String field) throws Exception {
        Class<?> cls = o.getClass();
        while (cls != null) {
            try {
                Field f = cls.getDeclaredField(field);
                f.setAccessible(true);
                return f.get(o);
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            }
        }
        throw new NoSuchFieldException(field + " not found in " + o.getClass());
    }
}
