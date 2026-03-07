package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.engine.Game;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.RobotTemplate;
import com.titozeio.enums.TerrainType;
import com.titozeio.skills.GuidedMissile;
import com.titozeio.skills.JetpackBoost;
import com.titozeio.skills.RepairNanobots;
import com.titozeio.ui.GameScreen;
import com.titozeio.ui.HexRenderer;
import com.titozeio.ui.RobotCardController;
import com.titozeio.ui.RobotSelectionScreen;
import com.titozeio.ui.Screen;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.junit.jupiter.api.Test;

class UiCoverageBoostTest {

    static class TestGame extends Game {
        Screen displayed;

        TestGame() {
            super(null);
        }

        @Override
        public void displayScreen(Screen screen) {
            this.displayed = screen;
        }
    }

    @Test
    void hexRendererCoversRenderingBranches() {
        Pane pane = new Pane();
        HexRenderer renderer = new HexRenderer(pane);

        com.titozeio.engine.Map map = new com.titozeio.engine.Map(1, 3);
        Hexagon h1 = TestFixtures.hex(0, 0, TerrainType.VEGETATION, 1);
        Hexagon h2 = TestFixtures.hex(1, 0, TerrainType.WATER_SHALLOW, 0);
        Hexagon h3 = TestFixtures.hex(2, 0, TerrainType.WATER_DEEP, 2);
        h1.setDeployZoneP1(true);
        h2.setBaseP1(true);
        h3.setBaseP2(true);

        Player p1 = new Player("Jugador 1");
        Player p2 = new Player("Jugador 2");
        Robot r1 = TestFixtures.robot("Victory Saber", p1, 12, 3, 2, 3, null);
        Robot r2 = TestFixtures.robot("Ice Age", p2, 10, 3, 2, 3, null);
        r1.setPosition(h1);
        r2.setPosition(h2);
        h1.setOccupant(r1);
        h2.setOccupant(r2);

        map.setHexagon(0, 0, h1);
        map.setHexagon(1, 0, h2);
        map.setHexagon(2, 0, h3);

        Set<Hexagon> hl = new HashSet<>();
        hl.add(h1);
        renderer.setHighlightedHexes(hl);
        renderer.setMoveHighlightedHexes(hl);
        renderer.setAttackHighlightedHexes(hl);
        renderer.setSkillHighlightedHexes(hl, true);

        AtomicReference<Hexagon> clicked = new AtomicReference<>();
        renderer.setOnHexClick(clicked::set);
        renderer.render(map);

        assertTrue(renderer.isMoveHighlighted(h1));
        assertTrue(renderer.isAttackHighlighted(h1));
        assertTrue(renderer.isSkillHighlighted(h1));
        assertTrue(pane.getChildren().size() > 10);

        Polygon p = (Polygon) pane.getChildren().stream().filter(n -> n instanceof Polygon).findFirst().orElseThrow();
        p.getOnMouseClicked().handle(null);
        assertNotNull(clicked.get());

        double[] px = HexRenderer.axialToPixel(1, 1);
        assertEquals(2, px.length);
    }

    @Test
    void robotCardControllerCoversDataAndSelection() throws Exception {
        RobotCardController c = new RobotCardController();
        set(c, "cardRoot", new FlowPane());
        set(c, "robotNameText", new Text());
        set(c, "robotImageView", new ImageView());
        set(c, "hpText", new Text());
        set(c, "weaponNameText", new Text());
        set(c, "weaponRangeText", new Text());
        set(c, "weaponDamageText", new Text());
        set(c, "skillNameText", new Text());
        set(c, "skillCooldownText", new Text());
        set(c, "skillStat1Row", new TextFlow());
        set(c, "skillStat1Label", new Text());
        set(c, "skillStat1Value", new Text());
        set(c, "skillStat2Row", new TextFlow());
        set(c, "skillStat2Label", new Text());
        set(c, "skillStat2Value", new Text());

        c.setData(RobotTemplate.ICE_AGE);
        assertEquals(RobotTemplate.ICE_AGE, c.getTemplate());
        assertNotNull(c.getRoot());

        AtomicReference<RobotTemplate> chosen = new AtomicReference<>();
        c.setOnSelect(chosen::set);
        c.handleClick();
        assertEquals(RobotTemplate.ICE_AGE, chosen.get());

        c.setHighlighted(true);
        assertTrue(c.getRoot().getStyle().contains("4EE2C9"));
        c.setHighlighted(false);
        assertTrue(c.getRoot().getStyle().contains("transparent"));
    }

    @Test
    void robotSelectionInternalFlowCoverage() throws Exception {
        RobotSelectionScreen s = new RobotSelectionScreen();
        set(s, "fixedInstructionsLabel", new Label());
        set(s, "turnInstructionsLabel", new Label());
        set(s, "availableRobotsPane", new FlowPane());
        FlowPane desc = new FlowPane();
        desc.setVisible(false);
        set(s, "descriptionPane", desc);
        set(s, "descRobotText", new Text());
        set(s, "descWeaponText", new Text());
        set(s, "descSkillText", new Text());
        set(s, "confirmButton", new Button());

        Method init = RobotSelectionScreen.class.getDeclaredMethod("initializeData", List.class);
        init.setAccessible(true);
        init.invoke(s, new ArrayList<>(List.of(RobotTemplate.values())));

        Method refresh = RobotSelectionScreen.class.getDeclaredMethod("refreshRobotCards");
        refresh.setAccessible(true);
        refresh.invoke(s);

        Method update = RobotSelectionScreen.class.getDeclaredMethod("updateTurnLabel");
        update.setAccessible(true);
        update.invoke(s);

        Method complete = RobotSelectionScreen.class.getDeclaredMethod("isSelectionComplete");
        complete.setAccessible(true);
        assertFalse((Boolean) complete.invoke(s));

        RobotCardController card = new RobotCardController();
        set(card, "cardRoot", new FlowPane());
        Method onCard = RobotSelectionScreen.class.getDeclaredMethod("onCardClicked", RobotTemplate.class,
                RobotCardController.class);
        onCard.setAccessible(true);
        onCard.invoke(s, RobotTemplate.BULLSEYE, card);

        assertTrue(((FlowPane) get(s, "descriptionPane")).isVisible());
        Button confirm = (Button) get(s, "confirmButton");
        assertFalse(confirm.isDisable());

        s.handleConfirmAction();
        assertDoesNotThrow(() -> s.handleInput("test"));

        TestGame tg = new TestGame();
        RobotSelectionScreen s2 = new RobotSelectionScreen();
        set(s2, "fixedInstructionsLabel", new Label());
        set(s2, "turnInstructionsLabel", new Label());
        set(s2, "availableRobotsPane", new FlowPane());
        set(s2, "descriptionPane", new FlowPane());
        set(s2, "descRobotText", new Text());
        set(s2, "descWeaponText", new Text());
        set(s2, "descSkillText", new Text());
        set(s2, "confirmButton", new Button());
        set(s2, "game", tg);
        set(s2, "player1Templates", new ArrayList<>(List.of(
                RobotTemplate.BULWARK, RobotTemplate.BULLSEYE, RobotTemplate.SCOUT)));
        set(s2, "player2Templates", new ArrayList<>(List.of(
                RobotTemplate.ICE_AGE, RobotTemplate.DEATH_KNIGHT, RobotTemplate.VICTORY_SABER)));

        Method transition = RobotSelectionScreen.class.getDeclaredMethod("transitionToCombatScreen");
        transition.setAccessible(true);
        transition.invoke(s2);
        assertNotNull(tg.displayed);

    }

    @Test
    void gameScreenInternalMethodsCoverage() throws Exception {
        GameScreen s = new GameScreen();
        Game g = new Game(null);

        Player p1 = g.getP1();
        Player p2 = g.getP2();
        Robot rMove = TestFixtures.robot("Scout", p1, 10, 5, 3, 2, new JetpackBoost());
        Robot rAtk1 = TestFixtures.robot("Ice", p1, 10, 4, 3, 2, new com.titozeio.skills.FreezeRay());
        Robot rAtk2 = TestFixtures.robot("Bull", p1, 10, 4, 3, 2, new GuidedMissile());
        Robot rAtk3 = TestFixtures.robot("Heal", p1, 10, 4, 3, 2, new RepairNanobots());
        Robot enemy = TestFixtures.robot("Enemy", p2, 10, 4, 3, 2, null);

        Hexagon a = g.getMap().getHexagon(0, 1);
        Hexagon b = g.getMap().getHexagon(1, 1);
        Hexagon c = g.getMap().getHexagon(2, 1);
        Hexagon d = g.getMap().getHexagon(3, 1);
        rMove.setPosition(a);
        rAtk1.setPosition(a);
        rAtk2.setPosition(a);
        rAtk3.setPosition(a);
        enemy.setPosition(c);
        c.setOccupant(enemy);

        set(s, "game", g);
        set(s, "window", null);
        set(s, "scene", null);
        set(s, "hexRenderer", new HexRenderer(new Pane()));
        set(s, "hexMapPane", new Pane());
        set(s, "turnLabel", new Label());
        set(s, "objectiveShortLabel", new Label());
        set(s, "infoDetailsLabel", new Label());
        set(s, "logScrollPane", new ScrollPane());
        set(s, "combatLogContent", new VBox());
        set(s, "skillButton", new Button());
        set(s, "endTurnButton", new Button());
        StackPane overlayPane = new StackPane();
        overlayPane.setVisible(false);
        set(s, "overlayPane", overlayPane);
        set(s, "overlayTitle", new Label());
        set(s, "overlayMessage", new Label());
        set(s, "overlayAcceptButton", new Button());
        StackPane victoryPane = new StackPane();
        victoryPane.setVisible(false);
        set(s, "victoryPane", victoryPane);
        set(s, "victoryIcon", new Label());
        set(s, "victoryTitle", new Label());
        set(s, "victoryReason", new Label());
        set(s, "victoryTurnLabel", new Label());
        set(s, "victoryMenuButton", new Button());
        set(s, "selectedRobot", rMove);
        set(s, "deployingPlayer", g.getP1());
        set(s, "robotsToPlace", new java.util.LinkedList<Robot>(List.of(rMove)));

        Method canUseSkill = GameScreen.class.getDeclaredMethod("canUseSkill", Robot.class);
        canUseSkill.setAccessible(true);
        assertTrue((Boolean) canUseSkill.invoke(s, rMove));

        Method moveTargets = GameScreen.class.getDeclaredMethod("getSkillMovementTargets", Robot.class);
        moveTargets.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<Hexagon> mt = (Set<Hexagon>) moveTargets.invoke(s, rMove);
        assertNotNull(mt);

        Method atkTargets = GameScreen.class.getDeclaredMethod("getSkillAttackTargets", Robot.class);
        atkTargets.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<Hexagon> at1 = (Set<Hexagon>) atkTargets.invoke(s, rAtk1);
        @SuppressWarnings("unchecked")
        Set<Hexagon> at2 = (Set<Hexagon>) atkTargets.invoke(s, rAtk2);
        @SuppressWarnings("unchecked")
        Set<Hexagon> at3 = (Set<Hexagon>) atkTargets.invoke(s, rAtk3);
        assertNotNull(at1);
        assertNotNull(at2);
        assertNotNull(at3);

        Method attackables = GameScreen.class.getDeclaredMethod("getAttackableHexes", Robot.class);
        attackables.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<Hexagon> normals = (Set<Hexagon>) attackables.invoke(s, rAtk2);
        assertNotNull(normals);

        Method terrain = GameScreen.class.getDeclaredMethod("terrainName", Hexagon.class);
        terrain.setAccessible(true);
        assertEquals("Vegetación", terrain.invoke(null, TestFixtures.hex(0, 0, TerrainType.VEGETATION, 0)));
        assertEquals("Agua poco profunda", terrain.invoke(null, TestFixtures.hex(0, 0, TerrainType.WATER_SHALLOW, 0)));
        assertEquals("Agua profunda", terrain.invoke(null, TestFixtures.hex(0, 0, TerrainType.WATER_DEEP, 0)));
        assertEquals("Meseta (h2)", terrain.invoke(null, TestFixtures.hex(0, 0, TerrainType.NORMAL, 2)));

        Method updateSkillBtn = GameScreen.class.getDeclaredMethod("updateSkillButton", Robot.class);
        updateSkillBtn.setAccessible(true);
        updateSkillBtn.invoke(s, rMove);
        rMove.setUsedMovement(true);
        updateSkillBtn.invoke(s, rMove);
        updateSkillBtn.invoke(s, (Object) null);

        Method updateDeployHud = GameScreen.class.getDeclaredMethod("updateDeployHud");
        updateDeployHud.setAccessible(true);
        updateDeployHud.invoke(s);

        Method highlightDeployZones = GameScreen.class.getDeclaredMethod("highlightDeployZones");
        highlightDeployZones.setAccessible(true);
        highlightDeployZones.invoke(s);

        Method handleDeployClick = GameScreen.class.getDeclaredMethod("handleDeployClick", Hexagon.class);
        handleDeployClick.setAccessible(true);
        handleDeployClick.invoke(s, a);

        Method updateCombatHud = GameScreen.class.getDeclaredMethod("updateCombatHud");
        updateCombatHud.setAccessible(true);
        g.setPhase(com.titozeio.engine.GamePhase.COMBAT);
        updateCombatHud.invoke(s);

        Method showTerrainInfo = GameScreen.class.getDeclaredMethod("showTerrainInfo", Hexagon.class);
        showTerrainInfo.setAccessible(true);
        showTerrainInfo.invoke(s, d);

        Method checkVictory = GameScreen.class.getDeclaredMethod("checkAndShowVictory", String.class);
        checkVictory.setAccessible(true);
        checkVictory.invoke(s, "eliminación");

        Method clearSelection = GameScreen.class.getDeclaredMethod("clearSelection");
        clearSelection.setAccessible(true);
        clearSelection.invoke(s);

        Method selectRobot = GameScreen.class.getDeclaredMethod("selectRobot", Robot.class);
        selectRobot.setAccessible(true);
        selectRobot.invoke(s, rAtk2);

        Method cycleModes = GameScreen.class.getDeclaredMethod("cycleModes");
        cycleModes.setAccessible(true);
        cycleModes.invoke(s);

        Method enterMove = GameScreen.class.getDeclaredMethod("enterMoveMode");
        enterMove.setAccessible(true);
        set(s, "selectedRobot", rMove);
        enterMove.invoke(s);

        Method executeMove = GameScreen.class.getDeclaredMethod("executeMove", Hexagon.class);
        executeMove.setAccessible(true);
        executeMove.invoke(s, b);

        Method enterAttack = GameScreen.class.getDeclaredMethod("enterAttackMode");
        enterAttack.setAccessible(true);
        set(s, "selectedRobot", rAtk2);
        enterAttack.invoke(s);

        Method executeAttack = GameScreen.class.getDeclaredMethod("executeAttack", Hexagon.class);
        executeAttack.setAccessible(true);
        c.setOccupant(enemy);
        enemy.setPosition(c);
        executeAttack.invoke(s, c);

        Robot enemy2 = TestFixtures.robot("Enemy2", p2, 10, 4, 3, 2, null);
        enemy2.setPosition(d);
        d.setOccupant(enemy2);
        Method executeSkill = GameScreen.class.getDeclaredMethod("executeSkill", Hexagon.class);
        executeSkill.setAccessible(true);
        set(s, "selectedRobot", rAtk1);
        executeSkill.invoke(s, d);

        set(s, "selectedRobot", rMove);
        executeSkill.invoke(s, a);

        Method handleCombatClick = GameScreen.class.getDeclaredMethod("handleCombatClick", Hexagon.class);
        handleCombatClick.setAccessible(true);
        handleCombatClick.invoke(s, d);
        handleCombatClick.invoke(s, TestFixtures.hex(9, 9, TerrainType.NORMAL, 0));

        Method onHexClicked = GameScreen.class.getDeclaredMethod("onHexClicked", Hexagon.class);
        onHexClicked.setAccessible(true);
        g.setPhase(com.titozeio.engine.GamePhase.COMBAT);
        onHexClicked.invoke(s, d);

        assertDoesNotThrow(() -> s.handleInput("F"));

        Method showVictory = GameScreen.class.getDeclaredMethod("showVictory", Player.class, String.class);
        showVictory.setAccessible(true);
        showVictory.invoke(s, p1, "eliminación");
        showVictory.invoke(s, p1, "conquista de base");

        assertDoesNotThrow(() -> s.showOverlayMessage("x", "y"));
        assertDoesNotThrow(() -> s.addLogMessage("m"));
        assertDoesNotThrow(() -> s.handleInput("UNKNOWN"));
    }

    private static void set(Object o, String field, Object value) throws Exception {
        Field f = o.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(o, value);
    }

    private static Object get(Object o, String field) throws Exception {
        Field f = o.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(o);
    }
}
