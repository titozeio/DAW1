package com.titozeio.ai;

import com.titozeio.commands.AttackCommand;
import com.titozeio.commands.GameCommand;
import com.titozeio.commands.MoveCommand;
import com.titozeio.commands.SkillCommand;
import com.titozeio.engine.Game;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.RobotState;
import com.titozeio.enums.SkillType;
import com.titozeio.enums.TerrainType;
import com.titozeio.match.TurnEngine;
import com.titozeio.skills.FreezeRay;
import com.titozeio.skills.GuidedMissile;
import com.titozeio.skills.JetpackBoost;
import com.titozeio.skills.PlasmaSaberAttack;
import com.titozeio.skills.RepairNanobots;
import com.titozeio.skills.Skill;
import com.titozeio.skills.TurboPropulsion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Estrategia de IA nivel DARK SOULS.
 * Busca secuencias de acciones propias y además castiga la mejor respuesta
 * inmediata del rival. Su simulación interna respeta cooldowns, estados y el
 * tipo real de acción consumida por cada skill.
 */
public class DarkSoulsAiStrategy implements AiStrategy {
    private static final int MAX_TURN_ACTIONS = 5;
    private static final int ROOT_BRANCHES = 5;
    private static final int BRANCHES_PER_STEP = 4;
    private static final int OPPONENT_RESPONSE_BRANCHES = 3;

    private final ActionEvaluator evaluator;

    public DarkSoulsAiStrategy(ActionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public TurnDecision chooseDecision(MatchContext context, Player player, TurnEngine turnEngine) {
        Game game = context.getGame();
        if (!player.hasActionsLeft()) {
            return new EndTurnDecision();
        }

        List<GameCommand> legal = getTopLegalCommands(game, player, turnEngine, ROOT_BRANCHES);
        if (legal.isEmpty()) {
            return new EndTurnDecision();
        }

        GameCommand bestCommand = null;
        int bestScore = Integer.MIN_VALUE;
        for (GameCommand cmd : legal) {
            GameStateSnapshot snapshot = new GameStateSnapshot(game);
            executeSimulatedCommand(cmd, game);
            int score = searchTurnCombos(game, player, turnEngine, 1);
            snapshot.restore(game);

            if (bestCommand == null || score > bestScore) {
                bestCommand = cmd;
                bestScore = score;
            }
        }

        return bestCommand == null ? new EndTurnDecision() : new ExecuteCommandDecision(bestCommand);
    }

    private int searchTurnCombos(Game game, Player aiPlayer, TurnEngine turnEngine, int depth) {
        if (!aiPlayer.hasActionsLeft() || depth >= MAX_TURN_ACTIONS) {
            return evaluateAfterOpponentResponse(game, aiPlayer, turnEngine);
        }

        List<GameCommand> legal = getTopLegalCommands(game, aiPlayer, turnEngine, BRANCHES_PER_STEP);
        if (legal.isEmpty()) {
            return evaluateAfterOpponentResponse(game, aiPlayer, turnEngine);
        }

        int maxScore = Integer.MIN_VALUE;
        for (GameCommand cmd : legal) {
            GameStateSnapshot snapshot = new GameStateSnapshot(game);
            executeSimulatedCommand(cmd, game);
            int score = searchTurnCombos(game, aiPlayer, turnEngine, depth + 1);
            snapshot.restore(game);
            maxScore = Math.max(maxScore, score);
        }
        return maxScore;
    }

    private int evaluateAfterOpponentResponse(Game game, Player aiPlayer, TurnEngine turnEngine) {
        int currentScore = evaluateBoardDarkSouls(game, aiPlayer);
        Player opponent = game.getOpponent(aiPlayer);
        if (!opponent.hasActionsLeft()) {
            return currentScore + 80;
        }

        List<GameCommand> opponentCommands = getTopLegalCommands(game, opponent, turnEngine, OPPONENT_RESPONSE_BRANCHES);
        if (opponentCommands.isEmpty()) {
            return currentScore + 40;
        }

        int worstReply = Integer.MAX_VALUE;
        for (GameCommand opponentCommand : opponentCommands) {
            GameStateSnapshot snapshot = new GameStateSnapshot(game);
            executeSimulatedCommand(opponentCommand, game);
            worstReply = Math.min(worstReply, evaluateBoardDarkSouls(game, aiPlayer));
            snapshot.restore(game);
        }
        return worstReply;
    }

    private List<GameCommand> getTopLegalCommands(Game game, Player player, TurnEngine turnEngine, int limit) {
        List<ScoredDecision> heuristics = new ArrayList<>();
        for (Robot robot : player.getAliveUnits()) {
            if (robot.isExhausted()) {
                continue;
            }
            for (GameCommand cmd : turnEngine.getLegalCommands(game, robot)) {
                int score = scoreCommandFast(game, robot, cmd);
                if (score > -5000) {
                    heuristics.add(new ScoredDecision(cmd, score));
                }
            }
        }

        heuristics.sort(Comparator.comparingInt(ScoredDecision::score).reversed());
        int actualLimit = Math.min(limit, heuristics.size());
        List<GameCommand> top = new ArrayList<>(actualLimit);
        for (int i = 0; i < actualLimit; i++) {
            top.add(heuristics.get(i).command());
        }
        return top;
    }

    private int scoreCommandFast(Game game, Robot actor, GameCommand cmd) {
        if (cmd instanceof MoveCommand move) {
            return evaluator.scoreMove(game, actor, move.getTargetHex());
        }
        if (cmd instanceof AttackCommand attack) {
            return evaluator.scoreAttack(game, actor, attack.getTarget());
        }
        if (cmd instanceof SkillCommand skill) {
            int score = evaluator.scoreSkill(game, actor, skill.getTarget());
            if (actor.getSkill() instanceof FreezeRay && skill.getTarget() instanceof Robot enemy) {
                score += estimateTurnDenialValue(enemy);
            }
            return score;
        }
        return 0;
    }

    private int estimateTurnDenialValue(Robot target) {
        int value = target.getWeapon().getDamage() * 18;
        value += target.getMovementPoints() * 20;
        value += target.getCurrentHp() <= 4 ? 60 : 0;
        return value;
    }

    private void executeSimulatedCommand(GameCommand cmd, Game game) {
        if (cmd instanceof MoveCommand move) {
            Robot robot = move.getRobot();
            moveRobot(robot, move.getTargetHex());
            robot.setUsedMovement(true);
            return;
        }

        if (cmd instanceof AttackCommand attack) {
            Robot attacker = attack.getAttacker();
            Robot target = attack.getTarget();
            applySimulatedDamage(target, evaluator.estimateBasicAttackDamage(game, attacker, target));
            attacker.setUsedAttack(true);
            return;
        }

        if (!(cmd instanceof SkillCommand skillCommand)) {
            return;
        }

        Robot actor = skillCommand.getRobot();
        Skill skill = actor.getSkill();
        if (skill == null) {
            return;
        }

        if (skill.getType() == SkillType.MOVEMENT) {
            actor.setUsedMovement(true);
        } else {
            actor.setUsedAttack(true);
        }
        skill.resetCooldown();

        Object target = skillCommand.getTarget();
        if (skill instanceof RepairNanobots repair && target instanceof Robot ally) {
            ally.heal(repair.getHealAmount());
        } else if (skill instanceof GuidedMissile missile && target instanceof Robot enemy) {
            enemy.takeDamage(missile.getDamage());
            cleanupIfDestroyed(enemy);
        } else if (skill instanceof FreezeRay freezeRay && target instanceof Robot enemy) {
            enemy.takeDamage(freezeRay.getDamage());
            enemy.applyState(RobotState.FROZEN, freezeRay.getFreezeDuration());
            cleanupIfDestroyed(enemy);
        } else if (skill instanceof PlasmaSaberAttack plasmaSaber && target instanceof Robot enemy) {
            enemy.takeDamage(plasmaSaber.getDamage());
            cleanupIfDestroyed(enemy);
        } else if ((skill instanceof JetpackBoost || skill instanceof TurboPropulsion) && target instanceof Hexagon hex) {
            moveRobot(actor, hex);
        }
    }

    private void moveRobot(Robot robot, Hexagon target) {
        if (robot.getPosition() != null) {
            robot.getPosition().setOccupant(null);
        }
        if (target != null) {
            target.setOccupant(robot);
        }
        robot.setPosition(target);
    }

    private void applySimulatedDamage(Robot target, int damage) {
        target.setCurrentHp(target.getCurrentHp() - damage);
        cleanupIfDestroyed(target);
    }

    private void cleanupIfDestroyed(Robot robot) {
        if (robot.getCurrentHp() <= 0 && robot.getPosition() != null) {
            robot.getPosition().setOccupant(null);
            robot.setPosition(null);
        }
    }

    private int evaluateBoardDarkSouls(Game game, Player aiPlayer) {
        int score = 0;
        Player opponent = game.getOpponent(aiPlayer);

        for (Robot robot : aiPlayer.getUnits()) {
            if (robot.isDestroyed() || robot.getPosition() == null) {
                score -= 900;
                continue;
            }
            score += 750;
            score += robot.getCurrentHp() * 20;
            score += robot.getPosition().getHeight() * 35;
            if (robot.getPosition().getTerrain() == TerrainType.VEGETATION) {
                score += 90;
            }
            if (robot.getPosition().getTerrain() == TerrainType.WATER_SHALLOW) {
                score -= 45;
            }
            if (robot.hasState(RobotState.FROZEN)) {
                score -= 220;
            }
            if (robot.getSkill() != null && robot.getSkill().isReady()) {
                score += 35;
            }
            score += immediateThreatValue(game, robot, opponent) * 16;
            score -= inboundThreatValue(game, robot, opponent) * 10;
            score += basePressure(game, robot, opponent) * 22;
        }

        for (Robot robot : opponent.getUnits()) {
            if (robot.isDestroyed() || robot.getPosition() == null) {
                score += 980;
                continue;
            }
            score -= 820;
            score -= robot.getCurrentHp() * 22;
            if (robot.hasState(RobotState.FROZEN)) {
                score += 260;
            }
            score -= immediateThreatValue(game, robot, aiPlayer) * 8;
            score -= basePressure(game, robot, aiPlayer) * 28;
        }

        return score;
    }

    private int immediateThreatValue(Game game, Robot attacker, Player enemy) {
        int value = 0;
        for (Robot target : enemy.getAliveUnits()) {
            if (target.getPosition() == null) {
                continue;
            }
            if (evaluator.canAttackTarget(game, attacker, target)) {
                int damage = evaluator.estimateBasicAttackDamage(game, attacker, target);
                value = Math.max(value, damage + (target.getCurrentHp() <= damage ? 12 : 0));
            }
        }
        return value;
    }

    private int inboundThreatValue(Game game, Robot defender, Player enemy) {
        int value = 0;
        for (Robot attacker : enemy.getAliveUnits()) {
            if (attacker.getPosition() == null) {
                continue;
            }
            if (evaluator.canAttackTarget(game, attacker, defender)) {
                int damage = evaluator.estimateBasicAttackDamage(game, attacker, defender);
                value += damage;
                if (defender.getCurrentHp() <= damage) {
                    value += 15;
                }
            }
        }
        return value;
    }

    private int basePressure(Game game, Robot robot, Player enemy) {
        Hexagon enemyBase = enemy.getBaseLocation();
        if (enemyBase == null || robot.getPosition() == null) {
            return 0;
        }
        int distance = game.getMap().getDistance(robot.getPosition(), enemyBase);
        return Math.max(0, 5 - distance);
    }

    private record ScoredDecision(GameCommand command, int score) {
    }

    private static class GameStateSnapshot {
        private final List<RobotStateRecord> p1Robots = new ArrayList<>();
        private final List<RobotStateRecord> p2Robots = new ArrayList<>();

        GameStateSnapshot(Game game) {
            for (Robot robot : game.getP1().getUnits()) {
                p1Robots.add(new RobotStateRecord(robot));
            }
            for (Robot robot : game.getP2().getUnits()) {
                p2Robots.add(new RobotStateRecord(robot));
            }
        }

        void restore(Game game) {
            for (RobotStateRecord record : p1Robots) {
                record.clearCurrentPosition();
            }
            for (RobotStateRecord record : p2Robots) {
                record.clearCurrentPosition();
            }
            for (RobotStateRecord record : p1Robots) {
                record.restoreSavedState();
            }
            for (RobotStateRecord record : p2Robots) {
                record.restoreSavedState();
            }
        }
    }

    private static class RobotStateRecord {
        private final Robot robot;
        private final int currentHp;
        private final Hexagon position;
        private final boolean usedMovement;
        private final boolean usedAttack;
        private final Map<RobotState, Integer> activeStates;
        private final int skillCooldown;

        RobotStateRecord(Robot robot) {
            this.robot = robot;
            this.currentHp = robot.getCurrentHp();
            this.position = robot.getCurrentHp() > 0 ? robot.getPosition() : null;
            this.usedMovement = robot.isUsedMovement();
            this.usedAttack = robot.isUsedAttack();
            this.activeStates = robot.snapshotActiveStates();
            this.skillCooldown = robot.getSkill() != null ? robot.getSkill().getCurrentCooldown() : 0;
        }

        void clearCurrentPosition() {
            if (robot.getPosition() != null && robot.getPosition().getOccupant() == robot) {
                robot.getPosition().setOccupant(null);
            }
        }

        void restoreSavedState() {
            robot.setCurrentHp(currentHp);
            robot.setPosition(position);
            if (position != null) {
                position.setOccupant(robot);
            }
            robot.setUsedMovement(usedMovement);
            robot.setUsedAttack(usedAttack);
            robot.restoreActiveStates(activeStates);
            if (robot.getSkill() != null) {
                robot.getSkill().setCurrentCooldownForSimulation(skillCooldown);
            }
        }
    }
}
