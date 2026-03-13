package com.titozeio.match;

import com.titozeio.enums.RobotTemplate;
import com.titozeio.telemetry.SimulationStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Servicio para ejecutar simulaciones masivas (IA vs IA).
 * Permite lanzar un lote (batch) de partidas consecutivas para recopilar
 * estadísticas de balanceo, winrates y rendimiento de robots.
 */
public class BatchSimulationService {
    private final MatchRunner matchRunner;

    public BatchSimulationService(MatchRunner matchRunner) {
        this.matchRunner = matchRunner;
    }

    public SimulationStats runBatch(BatchSimulationConfig config) {
        return runBatch(config, null, null);
    }

    /**
     * Ejecuta un lote de simulaciones basado en la configuración proporcionada.
     * 
     * @param config           Configuración del lote (nº partidas, IA, equipos,
     *                         semilla).
     * @param onMatchCompleted Callback opcional que se invoca al terminar cada
     *                         partida.
     * @param shouldCancel     Proveedor opcional para interrumpir la simulación.
     * @return Objeto SimulationStats con los datos agregados del lote.
     */
    public SimulationStats runBatch(BatchSimulationConfig config, Consumer<MatchResult> onMatchCompleted,
            BooleanSupplier shouldCancel) {
        SimulationStats stats = new SimulationStats();
        // Usamos una semilla base para que el lote sea reproducible
        Random random = new Random(config.seed());
        for (int i = 0; i < config.matchCount(); i++) {
            if (shouldCancel != null && shouldCancel.getAsBoolean()) {
                break;
            }
            // Generamos una semilla única por partida derivada de la base
            long matchSeed = random.nextLong();
            List<RobotTemplate> teamP1 = config.teamP1() != null ? config.teamP1() : randomTeam(matchSeed);
            List<RobotTemplate> teamP2 = config.teamP2() != null ? config.teamP2()
                    : randomTeam(matchSeed ^ 0x9E3779B97F4A7C15L);

            MatchResult result = matchRunner.runMatch(
                    i + 1L,
                    matchSeed,
                    config.difficultyP1(),
                    config.difficultyP2(),
                    teamP1,
                    teamP2,
                    config.maxTurns());

            stats.record(result);
            if (onMatchCompleted != null) {
                onMatchCompleted.accept(result);
            }
        }
        return stats;
    }

    private List<RobotTemplate> randomTeam(long seed) {
        List<RobotTemplate> pool = new ArrayList<>(Arrays.asList(RobotTemplate.values()));
        Collections.shuffle(pool, new Random(seed));
        return List.copyOf(pool.subList(0, 3));
    }
}
