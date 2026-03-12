package com.titozeio.commands;

import com.titozeio.engine.Map;
import com.titozeio.engine.Robot;

/**
 * Comando que encapsula un ataque de un robot atacante a un objetivo.
 */
public class AttackCommand implements GameCommand {
    private final Robot attacker;
    private final Robot target;
    private final Map map;
    private int damageDealt;
    private boolean fatal;
    private boolean executed;

    public AttackCommand(Robot attacker, Robot target, Map map) {
        this.attacker = attacker;
        this.target = target;
        this.map = map;
    }

    @Override
    public void execute() {
        if (executed) {
            return;
        }
        int hpBefore = target.getCurrentHp();
        attacker.attack(target, map);
        damageDealt = hpBefore - target.getCurrentHp();
        fatal = target.isDestroyed();
        executed = true;
    }

    @Override
    public void undo() {
        // El deshacer un ataque suele estar prohibido en tácticos por turnos una vez
        // hay RNG o muerte,
        // pero se podría implementar devolviendo HP.
        throw new UnsupportedOperationException("No se puede deshacer un ataque una vez realizado.");
    }

    @Override
    public String getDescription() {
        String msg = attacker.getModelName() + " ataca a " + target.getModelName() + " (" + damageDealt + " daño)";
        if (fatal)
            msg += " - DESTRUIDO";
        return msg;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public boolean isFatal() {
        return fatal;
    }

    public Robot getAttacker() {
        return attacker;
    }

    public Robot getTarget() {
        return target;
    }
}
