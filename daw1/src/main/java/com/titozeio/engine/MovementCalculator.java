package com.titozeio.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Calcula las casillas alcanzables por un robot en un turno.
 *
 * Usa el algoritmo de Dijkstra para encontrar todos los hexágonos
 * a los que el robot puede llegar con sus puntos de movimiento,
 * respetando las reglas del GDD:
 * - Coste base = 1 por casilla
 * - Vegetación / Agua poco profunda = +1
 * - Agua profunda = bloqueada
 * - Subir 1 nivel = +1, bajar 1+ niveles = +1
 * - Subir ≥2 niveles = bloqueado
 * - Casillas ocupadas por robots aliados = bloqueadas
 * - Casillas ocupadas por robots enemigos = bloqueadas (no se puede atravesar)
 */
public class MovementCalculator {

    /** Los 6 vecinos en coordenadas axiales (pointy-top). */
    private static final int[][] AXIAL_DIRS = {
            { 1, 0 }, { -1, 0 },
            { 0, 1 }, { 0, -1 },
            { 1, -1 }, { -1, 1 }
    };

    /**
     * Devuelve el conjunto de hexágonos vacíos a los que el robot puede moverse.
     * Incluye casillas con coste acumulado ≤ movementPoints del robot.
     * Excluye la casilla actual y casillas con ocupante.
     *
     * @param robot el robot que va a moverse
     * @param map   el mapa actual
     * @return conjunto de hexágonos destino válidos
     */
    public static Set<Hexagon> getReachable(Robot robot, Map map) {
        Hexagon origin = robot.getPosition();
        if (origin == null)
            return new HashSet<>();

        int maxMP = robot.getMovementPoints();

        // Coste mínimo para llegar a cada hexágono (Dijkstra)
        java.util.Map<Hexagon, Integer> dist = new HashMap<>();
        dist.put(origin, 0);

        // Min-heap por coste acumulado
        PriorityQueue<HexNode> queue = new PriorityQueue<>();
        queue.add(new HexNode(origin, 0));

        while (!queue.isEmpty()) {
            HexNode current = queue.poll();

            // Si ya alcanzamos el máximo no tiene sentido explorar más
            if (current.cost >= maxMP)
                continue;

            for (int[] dir : AXIAL_DIRS) {
                int nq = current.hex.getQ() + dir[0];
                int nr = current.hex.getR() + dir[1];
                Hexagon neighbor = map.getHexagon(nq, nr);
                if (neighbor == null)
                    continue;

                // Agua profunda: bloqueada
                if (neighbor.getTerrain() == com.titozeio.enums.TerrainType.WATER_DEEP)
                    continue;

                // Subir ≥2 niveles: bloqueado
                if (!neighbor.isReachableFrom(current.hex.getHeight()))
                    continue;

                // Casillas ocupadas se pueden "ver" como destino final si son enemigas,
                // pero no se puede atravesar ningún robot
                if (neighbor.isOccupied())
                    continue;

                int moveCost = current.cost
                        + neighbor.getMovementCost(robot, current.hex.getHeight());

                if (moveCost <= maxMP) {
                    Integer prev = dist.get(neighbor);
                    if (prev == null || moveCost < prev) {
                        dist.put(neighbor, moveCost);
                        queue.add(new HexNode(neighbor, moveCost));
                    }
                }
            }
        }

        // El conjunto de destinos son todas las casillas alcanzables excepto el origen
        Set<Hexagon> reachable = new HashSet<>(dist.keySet());
        reachable.remove(origin);
        return reachable;
    }

    /** Nodo auxiliar para la cola de prioridad de Dijkstra. */
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
