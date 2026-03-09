package com.titozeio.commands;

import com.titozeio.engine.Map;
import com.titozeio.engine.Robot;

/**
 * Comando que encapsula un ataque de un robot atacante a un objetivo.
 */
public class AttackCommand implements GameCommand {
    private final Robot attacker;
    private final Robot target;
    private final int damageDealt;
    private final boolean fatal;

    public AttackCommand(Robot attacker, Robot target, Map map) {
        this.attacker = attacker;
        this.target = target;

        // Guardamos info para el log o posible undo (aunque el undo de daño es complejo
        // en juegos)
        int hpBefore = target.getCurrentHp();
        this.attacker.attack(target, map);
        this.damageDealt = hpBefore - target.getCurrentHp();
        this.fatal = target.isDestroyed();
    }

    @Override
    public void execute() {
        // En este diseño simple, la lógica se ejecuta en el constructor para capturar
        // el estado,
        // pero lo ideal es dispararla aquí. El constructor ya disparó el ataque.
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
}
