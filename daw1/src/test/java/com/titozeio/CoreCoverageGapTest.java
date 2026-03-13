package com.titozeio;

import static org.junit.jupiter.api.Assertions.*;

import com.titozeio.commands.AttackCommand;
import com.titozeio.commands.MoveCommand;
import com.titozeio.commands.SkillCommand;
import com.titozeio.engine.Game;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.engine.RobotFactory;
import com.titozeio.enums.RobotState;
import com.titozeio.enums.TerrainType;
import com.titozeio.skills.FreezeRay;
import com.titozeio.skills.JetpackBoost;
import com.titozeio.states.ExhaustedState;
import com.titozeio.states.FrozenState;
import com.titozeio.victory.BaseCaptureVC;
import com.titozeio.victory.EliminationVC;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;

class CoreCoverageGapTest {

    @Test
    void robotFactoryHandlesKnownUnknownAndNullTypes() throws Exception {
        Player owner = new Player("Owner");

        Robot bullseye = RobotFactory.createRobot("Bullseye", owner);
        assertEquals("Bullseye", bullseye.getModelName());
        assertSame(owner, bullseye.getOwner());

        Robot fallbackUnknown = RobotFactory.createRobot("no-existe", owner);
        Robot fallbackNull = RobotFactory.createRobot(null, owner);
        assertEquals("Victory Saber", fallbackUnknown.getModelName());
        assertEquals("Victory Saber", fallbackNull.getModelName());

        Constructor<com.titozeio.engine.Launcher> ctor = com.titozeio.engine.Launcher.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }

    @Test
    void stateObjectsAndCommandsCoverRemainingBranches() {
        Player p1 = new Player("P1");
        Player p2 = new Player("P2");
        Game game = new Game(null);

        Hexagon a = TestFixtures.hex(0, 0, TerrainType.NORMAL, 0);
        Hexagon b = TestFixtures.hex(1, 0, TerrainType.NORMAL, 0);
        Hexagon c = TestFixtures.hex(2, 0, TerrainType.NORMAL, 0);

        Robot mover = TestFixtures.robot("Mover", p1, 10, 4, 3, 4, new JetpackBoost());
        Robot freezer = TestFixtures.robot("Freezer", p1, 10, 4, 3, 2, new FreezeRay());
        Robot target = TestFixtures.robot("Target", p2, 4, 4, 3, 2, null);

        a.setOccupant(mover);
        mover.setPosition(a);
        b.setOccupant(freezer);
        freezer.setPosition(b);
        c.setOccupant(target);
        target.setPosition(c);

        MoveCommand firstMove = new MoveCommand(mover, b);
        firstMove.execute();
        assertEquals(b, mover.getPosition());
        firstMove.undo();
        assertEquals(a, mover.getPosition());

        Robot noOrigin = TestFixtures.robot("NoOrigin", p1, 10, 4, 3, 4, null);
        MoveCommand noOriginMove = new MoveCommand(noOrigin, a);
        noOriginMove.execute();
        assertEquals(a, noOrigin.getPosition());
        noOriginMove.undo();
        assertEquals(a, noOrigin.getPosition(), "undo sin previousHex no debe moverlo");

        AttackCommand attack = new AttackCommand(freezer, target, game.getMap());
        attack.execute();
        int dealtAfterFirstExecution = attack.getDamageDealt();
        attack.execute();
        assertEquals(dealtAfterFirstExecution, attack.getDamageDealt(), "execute dos veces no debe duplicar daño");
        assertNotNull(attack.getDescription());
        assertThrows(UnsupportedOperationException.class, attack::undo);

        SkillCommand moveSkill = new SkillCommand(mover, c);
        assertDoesNotThrow(moveSkill::execute);
        assertTrue(moveSkill.getDescription().contains("usa"));
        assertThrows(UnsupportedOperationException.class, moveSkill::undo);

        FrozenState frozenState = new FrozenState();
        ExhaustedState exhaustedState = new ExhaustedState();
        assertFalse(frozenState.canMove(mover));
        assertFalse(frozenState.canAttack(mover));
        assertFalse(frozenState.canUseSkill(mover));
        assertDoesNotThrow(() -> frozenState.onTurnBegin(mover));
        assertFalse(exhaustedState.canMove(mover));
        assertFalse(exhaustedState.canAttack(mover));
        assertFalse(exhaustedState.canUseSkill(mover));
        assertDoesNotThrow(() -> exhaustedState.onTurnBegin(mover));
    }

    @Test
    void victoryConditionsCoverBaseCaptureAndEliminationBranches() {
        Game game = new Game(null);
        Player p1 = game.getP1();
        Player p2 = game.getP2();

        Robot p1Robot = TestFixtures.robot("P1", p1, 10, 4, 3, 2, null);
        Robot p2Robot = TestFixtures.robot("P2", p2, 10, 4, 3, 2, null);
        p1.addUnit(p1Robot);
        p2.addUnit(p2Robot);

        EliminationVC eliminationVC = new EliminationVC();
        assertNull(eliminationVC.check(game));

        p2Robot.takeDamage(20);
        assertSame(p1, eliminationVC.check(game));

        p1Robot.takeDamage(20);
        assertNull(eliminationVC.check(game), "si ambos están eliminados no hay ganador");

        BaseCaptureVC baseCaptureVC = new BaseCaptureVC(2);
        Hexagon p1Base = TestFixtures.hex(0, 0, TerrainType.NORMAL, 0);
        Hexagon p2Base = TestFixtures.hex(1, 0, TerrainType.NORMAL, 0);
        p1.setBaseLocation(p1Base);
        p2.setBaseLocation(p2Base);

        Robot infiltrator = TestFixtures.robot("Infiltrator", p1, 10, 4, 3, 2, null);
        p1.addUnit(infiltrator);
        p2Base.setOccupant(infiltrator);
        infiltrator.setPosition(p2Base);

        assertNull(baseCaptureVC.check(game));
        assertEquals(1, p2.getBaseControlTurns());
        assertSame(p1, baseCaptureVC.check(game));
        assertEquals(2, p2.getBaseControlTurns());

        p2Base.setOccupant(null);
        infiltrator.setPosition(null);
        assertNull(baseCaptureVC.check(game));
        assertEquals(0, p2.getBaseControlTurns());

        Robot frozenRobot = TestFixtures.robot("Frozen", p1, 10, 4, 3, 2, null);
        frozenRobot.applyState(RobotState.FROZEN, 1);
        assertTrue(frozenRobot.hasState(RobotState.FROZEN));
    }
}
