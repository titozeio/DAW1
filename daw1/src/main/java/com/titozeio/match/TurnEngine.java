package com.titozeio.match;

import com.titozeio.commands.AttackCommand;
import com.titozeio.commands.GameCommand;
import com.titozeio.commands.MoveCommand;
import com.titozeio.commands.SkillCommand;
import com.titozeio.engine.ActionManager;
import com.titozeio.engine.Game;
import com.titozeio.engine.Hexagon;
import com.titozeio.engine.MovementCalculator;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.SkillType;
import com.titozeio.enums.TerrainType;
import com.titozeio.skills.FreezeRay;
import com.titozeio.skills.GuidedMissile;
import com.titozeio.skills.JetpackBoost;
import com.titozeio.skills.PlasmaSaberAttack;
import com.titozeio.skills.RepairNanobots;
import com.titozeio.skills.Skill;
import com.titozeio.skills.TurboPropulsion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicio de dominio para cálculo y resolución de turnos.
 */
public class TurnEngine {
    private static final int[][] AXIAL_DIRS = {
            { 1, 0 }, { -1, 0 },
            { 0, 1 }, { 0, -1 },
            { 1, -1 }, { -1, 1 }
    };

    public List<GameCommand> getLegalCommands(Game game, Robot robot) {
        List<GameCommand> commands = new ArrayList<>();
        if (robot == null || robot.isDestroyed() || robot.getPosition() == null) {
            return commands;
        }

        if (!robot.isUsedMovement() && robot.canMove()) {
            for (Hexagon hex : getMoveTargets(game, robot)) {
                commands.add(new MoveCommand(robot, hex));
            }
        }

        if (!robot.isUsedAttack() && robot.canAttack()) {
            for (Hexagon hex : getAttackTargets(game, robot)) {
                commands.add(new AttackCommand(robot, hex.getOccupant(), game.getMap()));
            }
        }

        if (canUseSkill(robot)) {
            Skill skill = robot.getSkill();
            if (skill.getType() == SkillType.ATTACK) {
                for (Hexagon hex : getSkillAttackTargets(game, robot)) {
                    commands.add(new SkillCommand(robot, hex.getOccupant()));
                }
            } else {
                for (Hexagon hex : getSkillMovementTargets(game, robot)) {
                    commands.add(new SkillCommand(robot, hex));
                }
            }
        }

        return commands;
    }

    public Set<Hexagon> getDeployHexes(Game game, Player player) {
        Set<Hexagon> zones = new LinkedHashSet<>();
        boolean isP1 = player == game.getP1();
        for (Hexagon[] row : game.getMap().getGrid()) {
            for (Hexagon hex : row) {
                if (hex == null || hex.isOccupied()) {
                    continue;
                }
                if (isP1 && hex.isDeployZoneP1()) {
                    zones.add(hex);
                } else if (!isP1 && hex.isDeployZoneP2()) {
                    zones.add(hex);
                }
            }
        }
        return zones;
    }

    public Set<Hexagon> getMoveTargets(Game game, Robot robot) {
        return MovementCalculator.getReachable(robot, game.getMap());
    }

    public Set<Hexagon> getAttackTargets(Game game, Robot robot) {
        Set<Hexagon> targets = new HashSet<>();
        for (Hexagon[] row : game.getMap().getGrid()) {
            for (Hexagon hex : row) {
                if (hex == null || !hex.isOccupied()) {
                    continue;
                }
                Robot occupant = hex.getOccupant();
                if (occupant.getOwner() == robot.getOwner()) {
                    continue;
                }
                if (robot.canAttack(occupant, game.getMap())) {
                    targets.add(hex);
                }
            }
        }
        return targets;
    }

    public Set<Hexagon> getSkillMovementTargets(Game game, Robot robot) {
        Skill skill = robot.getSkill();
        int skillRange = 3;
        if (skill instanceof JetpackBoost jetpackBoost) {
            skillRange = jetpackBoost.getMovementPoints();
        } else if (skill instanceof TurboPropulsion turboPropulsion) {
            skillRange = turboPropulsion.getMovementPoints();
        }

        if (skill instanceof TurboPropulsion) {
            return getSkillMovementTargetsWithTerrain(game, robot, skillRange);
        }

        Set<Hexagon> results = new HashSet<>();
        Hexagon origin = robot.getPosition();
        if (origin == null) {
            return results;
        }
        for (Hexagon[] row : game.getMap().getGrid()) {
            for (Hexagon hex : row) {
                if (hex == null || hex.isOccupied() || hex == origin) {
                    continue;
                }
                int dist = game.getMap().getDistance(origin, hex);
                if (dist <= skillRange) {
                    results.add(hex);
                }
            }
        }
        return results;
    }

    public Set<Hexagon> getSkillAttackTargets(Game game, Robot robot) {
        Skill skill = robot.getSkill();
        Set<Hexagon> targets = new HashSet<>();
        Hexagon origin = robot.getPosition();
        if (origin == null || skill == null) {
            return targets;
        }

        int skillRange;
        boolean targetsAllies;
        boolean ignoresVisibility;

        if (skill instanceof RepairNanobots repairNanobots) {
            skillRange = repairNanobots.getRange();
            targetsAllies = true;
            ignoresVisibility = false;
        } else if (skill instanceof FreezeRay freezeRay) {
            skillRange = freezeRay.getRange();
            targetsAllies = false;
            ignoresVisibility = false;
        } else if (skill instanceof GuidedMissile guidedMissile) {
            skillRange = guidedMissile.getRange();
            targetsAllies = false;
            ignoresVisibility = guidedMissile.isIgnoresVisibility();
        } else if (skill instanceof PlasmaSaberAttack plasmaSaberAttack) {
            skillRange = plasmaSaberAttack.getRange();
            targetsAllies = false;
            ignoresVisibility = false;
        } else {
            skillRange = 1;
            targetsAllies = false;
            ignoresVisibility = false;
        }

        for (Hexagon[] row : game.getMap().getGrid()) {
            for (Hexagon hex : row) {
                if (hex == null || !hex.isOccupied()) {
                    continue;
                }
                Robot occupant = hex.getOccupant();
                if (occupant.isDestroyed()) {
                    continue;
                }
                if (targetsAllies != (occupant.getOwner() == robot.getOwner())) {
                    continue;
                }
                int dist = game.getMap().getDistance(origin, hex);
                if (dist > skillRange) {
                    continue;
                }
                if (!ignoresVisibility && !targetsAllies && !game.getMap().hasVisibility(origin, hex)) {
                    continue;
                }
                targets.add(hex);
            }
        }
        return targets;
    }

    public boolean canUseSkill(Robot robot) {
        return robot != null && robot.getSkill() != null
                && robot.canUseSkill()
                && robot.getSkill().isReady()
                && !(robot.isUsedMovement() && robot.getSkill().getType() == SkillType.MOVEMENT)
                && !(robot.isUsedAttack() && robot.getSkill().getType() == SkillType.ATTACK);
    }

    public void execute(GameCommand command) {
        ActionManager.getInstance().executeCommand(command);
    }

    public void endTurn(Game game) {
        game.nextTurn();
        for (Robot robot : game.getCurrentPlayer().getUnits()) {
            if (!robot.isDestroyed()) {
                robot.resetActions();
            }
        }
    }

    private Set<Hexagon> getSkillMovementTargetsWithTerrain(Game game, Robot robot, int maxMP) {
        Hexagon origin = robot.getPosition();
        if (origin == null) {
            return new HashSet<>();
        }

        java.util.Map<Hexagon, Integer> dist = new java.util.HashMap<>();
        dist.put(origin, 0);
        java.util.PriorityQueue<HexNode> queue = new java.util.PriorityQueue<>();
        queue.add(new HexNode(origin, 0));

        while (!queue.isEmpty()) {
            HexNode current = queue.poll();
            if (current.cost >= maxMP) {
                continue;
            }

            for (int[] dir : AXIAL_DIRS) {
                int nq = current.hex.getQ() + dir[0];
                int nr = current.hex.getR() + dir[1];
                Hexagon neighbor = game.getMap().getHexagon(nq, nr);
                if (neighbor == null || neighbor.getTerrain() == TerrainType.WATER_DEEP) {
                    continue;
                }
                if (!neighbor.isReachableFrom(current.hex.getHeight()) || neighbor.isOccupied()) {
                    continue;
                }

                int moveCost = current.cost + neighbor.getMovementCost(robot, current.hex.getHeight());
                if (moveCost <= maxMP) {
                    Integer prev = dist.get(neighbor);
                    if (prev == null || moveCost < prev) {
                        dist.put(neighbor, moveCost);
                        queue.add(new HexNode(neighbor, moveCost));
                    }
                }
            }
        }

        Set<Hexagon> reachable = new HashSet<>(dist.keySet());
        reachable.remove(origin);
        return reachable;
    }

    private static class HexNode implements Comparable<HexNode> {
        final Hexagon hex;
        final int cost;

        HexNode(Hexagon hex, int cost) {
            this.hex = hex;
            this.cost = cost;
        }

        @Override
        public int compareTo(HexNode other) {
            return Integer.compare(this.cost, other.cost);
        }
    }
}
