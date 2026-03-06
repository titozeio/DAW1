package com.titozeio;

import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.enums.AttackType;
import com.titozeio.enums.TerrainType;
import com.titozeio.model.Weapon;
import com.titozeio.skills.Skill;

final class TestFixtures {
    private TestFixtures() {
    }

    static Robot robot(String name, Player owner, int hp, int movement, int range, int damage, Skill skill) {
        return new Robot(
                name,
                "desc",
                hp,
                movement,
                new Weapon("w", range, damage, "d", AttackType.ENERGY),
                skill,
                owner);
    }

    static Hexagon hex(int q, int r, TerrainType terrain, int height) {
        return new Hexagon(q, r, terrain, height);
    }
}
