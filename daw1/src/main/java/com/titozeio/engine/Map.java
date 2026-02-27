package com.titozeio.engine;

import com.titozeio.model.Objective;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el mapa hexagonal del juego.
 * Contiene la cuadrícula de hexágonos y los objetivos del mapa.
 */
public class Map {

    private Hexagon[][] grid;
    private List<Objective> objectives;

    /**
     * @param rows número de filas del mapa.
     * @param cols número de columnas del mapa.
     */
    public Map(int rows, int cols) {
        this.grid = new Hexagon[rows][cols];
        this.objectives = new ArrayList<>();
    }

    /**
     * Devuelve el hexágono en las coordenadas axiales (q, r).
     * 
     * @param q coordenada columna.
     * @param r coordenada fila.
     * @return el Hexagon correspondiente, o null si está fuera de límites.
     */
    public Hexagon getHexagon(int q, int r) {
        if (r < 0 || r >= grid.length || q < 0 || q >= grid[r].length) {
            return null;
        }
        return grid[r][q];
    }

    /**
     * Establece un hexágono en las coordenadas indicadas.
     */
    public void setHexagon(int q, int r, Hexagon hex) {
        if (r >= 0 && r < grid.length && q >= 0 && q < grid[r].length) {
            grid[r][q] = hex;
        }
    }

    /**
     * Calcula la distancia entre dos hexágonos usando coordenadas cúbicas.
     * 
     * @return distancia en número de hexágonos.
     */
    public int getDistance(Hexagon a, Hexagon b) {
        int dq = Math.abs(a.getQ() - b.getQ());
        int dr = Math.abs(a.getR() - b.getR());
        int ds = Math.abs(a.getS() - b.getS());
        return Math.max(Math.max(dq, dr), ds);
    }

    /**
     * Calcula los hexágonos visibles desde un origen dentro de un rango.
     * Aplica la regla del GDD: no puede haber ninguna casilla intermedia
     * con más de 1 nivel de altura superior a cualquiera de los dos extremos.
     *
     * @param origin hexágono origen.
     * @param range  alcance máximo en hexágonos.
     * @return lista de hexágonos visibles.
     */
    public List<Hexagon> calculateVisibility(Hexagon origin, int range) {
        List<Hexagon> visible = new ArrayList<>();
        for (Hexagon[] row : grid) {
            for (Hexagon hex : row) {
                if (hex != null && hex != origin) {
                    int dist = getDistance(origin, hex);
                    if (dist <= range && hasVisibility(origin, hex)) {
                        visible.add(hex);
                    }
                }
            }
        }
        return visible;
    }

    /**
     * Determina si existe línea de visión entre dos hexágonos.
     * Regla GDD: ninguna casilla intermedia puede tener más de 1 nivel
     * de altura superior a la altura de ambos extremos.
     */
    public boolean hasVisibility(Hexagon origin, Hexagon target) {
        List<Hexagon> line = getHexLine(origin, target);
        int maxEnd = Math.max(origin.getHeight(), target.getHeight());
        for (Hexagon h : line) {
            if (h != origin && h != target) {
                if (h.getHeight() > maxEnd + 1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Devuelve el modificador de cobertura (0.0 = sin cobertura,
     * 0.5 = cobertura media, -1 = cobertura total / ataque imposible).
     * Regla GDD:
     * - Casilla intermedia +1 nivel → cobertura media (daño ×0.5)
     * - Casilla intermedia +2 niveles → cobertura total (no se puede atacar)
     */
    public float getCoverModifier(Hexagon attacker, Hexagon defender) {
        List<Hexagon> line = getHexLine(attacker, defender);
        int maxEnd = Math.max(attacker.getHeight(), defender.getHeight());
        boolean halfCover = false;
        for (Hexagon h : line) {
            if (h != attacker && h != defender) {
                int heightDiff = h.getHeight() - maxEnd;
                if (heightDiff >= 2)
                    return -1f; // Cobertura total
                if (heightDiff == 1)
                    halfCover = true;
            }
        }
        return halfCover ? 0.5f : 0.0f;
    }

    /**
     * Obtiene todos los hexágonos en línea recta entre dos hexágonos
     * (interpolación).
     */
    private List<Hexagon> getHexLine(Hexagon a, Hexagon b) {
        List<Hexagon> line = new ArrayList<>();
        int dist = getDistance(a, b);
        for (int i = 0; i <= dist; i++) {
            float t = dist == 0 ? 0 : (float) i / dist;
            int q = Math.round(a.getQ() + (b.getQ() - a.getQ()) * t);
            int r = Math.round(a.getR() + (b.getR() - a.getR()) * t);
            Hexagon h = getHexagon(q, r);
            if (h != null)
                line.add(h);
        }
        return line;
    }

    public Hexagon[][] getGrid() {
        return grid;
    }

    public List<Objective> getObjectives() {
        return objectives;
    }

    public void addObjective(Objective obj) {
        objectives.add(obj);
    }
}
