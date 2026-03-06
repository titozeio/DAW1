package com.titozeio.ui;

import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Map;
import com.titozeio.engine.Robot;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Renderiza el mapa hexagonal sobre un Pane de JavaFX.
 *
 * Orientación : pointy-top (punta arriba).
 * Coordenadas : axial (q, r). La cúbica s = -q - r se usa sólo en Map.
 *
 * Conversión axial → píxel (fórmula estándar pointy-top):
 * x = OFFSET_X + HEX_SIZE * (√3 * q + √3/2 * r)
 * y = OFFSET_Y + HEX_SIZE * ( 3/2 * r)
 *
 * Con HEX_SIZE=40, OFFSET_X=150, OFFSET_Y=120 el mapa 11×9 queda
 * centrado en la ventana de 1280×720.
 */
public class HexRenderer {

    // ── Geometría ─────────────────────────────────────────────────────────────
    /** Circunradio (centro → vértice). */
    private static final double HEX_SIZE = 40.0;
    /** Offset horizontal para centrar el mapa en 1280px. */
    private static final double OFFSET_X = 150.0;
    /** Offset vertical para centrar el mapa en 720px (respetando HUD). */
    private static final double OFFSET_Y = 120.0;

    // ── Paleta de terrenos ────────────────────────────────────────────────────
    private static final Color C_NORMAL = Color.web("#4a5242");
    private static final Color C_NORMAL_H1 = Color.web("#5e6655");
    private static final Color C_NORMAL_H2 = Color.web("#72786a");
    private static final Color C_VEGETATION = Color.web("#2a5c22");
    private static final Color C_WATER_SHALLOW = Color.web("#1e5e8e");
    private static final Color C_WATER_DEEP = Color.web("#0e3a60");

    // ── Paleta de zonas especiales (overlays semitransparentes) ───────────────
    private static final Color C_DEPLOY_P1 = Color.web("#3366cc", 0.25);
    private static final Color C_DEPLOY_P2 = Color.web("#cc3333", 0.25);
    private static final Color C_BASE_P1 = Color.web("#5599ff", 0.45);
    private static final Color C_BASE_P2 = Color.web("#ff5555", 0.45);

    // ── Borde de los hexágonos ────────────────────────────────────────────────
    private static final Color C_STROKE = Color.web("#0a1008", 0.6);
    private static final double STROKE_WIDTH = 1.2;
    /** Cache de sprites cargados por nombre de modelo de robot. */
    private static final java.util.Map<String, Image> ROBOT_SPRITE_CACHE = new HashMap<>();

    // ── Estado interno ────────────────────────────────────────────────────────
    /** Pane objetivo donde se dibujan los nodos. */
    private final Pane pane;

    /**
     * Callback opcional: se llama con el Hexagon cuando el jugador hace clic en él.
     */
    private Consumer<Hexagon> onHexClick;

    /** Hexágonos resaltados actualmente (zonas de despliegue disponibles, etc.). */
    private Set<Hexagon> highlighted = new HashSet<>();

    /**
     * Hexágonos resaltados para movimiento (rango de movimiento del robot
     * seleccionado).
     */
    private Set<Hexagon> moveHighlighted = new HashSet<>();

    /** Hexágonos resaltados para ataque (objetivos enemigos en rango). */
    private Set<Hexagon> attackHighlighted = new HashSet<>();

    /**
     * Hexágonos resaltados para skill (verde-lima = movimiento, violeta = ataque).
     */
    private Set<Hexagon> skillHighlighted = new HashSet<>();
    /**
     * true si el skill resaltado es de tipo ataque (violeta), false si es
     * movimiento (lima).
     */
    private boolean skillHighlightAttack = false;

    // ── Constructor ───────────────────────────────────────────────────────────
    public HexRenderer(Pane pane) {
        this.pane = pane;
    }

    /**
     * Registra el handler que se ejecuta al hacer clic en un hexágono.
     * Llama a esto desde GameScreen para conectar la lógica de selección.
     */
    public void setOnHexClick(Consumer<Hexagon> handler) {
        this.onHexClick = handler;
    }

    /**
     * Establece los hexágonos que se resaltarán en verde (ej: zonas de despliegue).
     * Llamar antes de render() para que el efecto se aplique.
     */
    public void setHighlightedHexes(Set<Hexagon> hexes) {
        this.highlighted = (hexes != null) ? hexes : Collections.emptySet();
    }

    /**
     * Establece los hexágonos que se resaltarán en azul (rango de movimiento).
     */
    public void setMoveHighlightedHexes(Set<Hexagon> hexes) {
        this.moveHighlighted = (hexes != null) ? hexes : Collections.emptySet();
    }

    /**
     * @return true si el hexágono está actualmente en el rango de movimiento
     *         resaltado.
     */
    public boolean isMoveHighlighted(Hexagon hex) {
        return moveHighlighted.contains(hex);
    }

    /**
     * Establece los hexágonos que se resaltarán en naranja (objetivos de ataque).
     */
    public void setAttackHighlightedHexes(Set<Hexagon> hexes) {
        this.attackHighlighted = (hexes != null) ? hexes : Collections.emptySet();
    }

    /** @return true si el hexágono está actualmente como objetivo de ataque. */
    public boolean isAttackHighlighted(Hexagon hex) {
        return attackHighlighted.contains(hex);
    }

    /** Establece los hexágonos resaltados para usar una skill. */
    public void setSkillHighlightedHexes(Set<Hexagon> hexes, boolean isAttackSkill) {
        this.skillHighlighted = (hexes != null) ? hexes : Collections.emptySet();
        this.skillHighlightAttack = isAttackSkill;
    }

    /** @return true si el hexágono está en el rango de uso de skill. */
    public boolean isSkillHighlighted(Hexagon hex) {
        return skillHighlighted.contains(hex);
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Pinta todos los hexágonos del mapa en el pane.
     * Llama a este método cada vez que el estado del mapa cambie.
     *
     * @param map el mapa a renderizar
     */
    public void render(Map map) {
        pane.getChildren().clear();

        Hexagon[][] grid = map.getGrid();
        for (Hexagon[] row : grid) {
            for (Hexagon hex : row) {
                if (hex != null) {
                    renderHex(hex);
                }
            }
        }
    }

    // ── Renderizado interno ───────────────────────────────────────────────────

    private void renderHex(Hexagon hex) {
        double[] center = axialToPixel(hex.getQ(), hex.getR());
        double cx = center[0];
        double cy = center[1];

        // 1. Polígono base (terreno + altura)
        Polygon base = buildHexPolygon(cx, cy, HEX_SIZE);
        base.setFill(terrainColor(hex));
        base.setStroke(C_STROKE);
        base.setStrokeType(StrokeType.INSIDE);
        base.setStrokeWidth(STROKE_WIDTH);

        // Evento de clic en el hexágono
        final Hexagon captured = hex;
        base.setOnMouseClicked(e -> {
            if (onHexClick != null)
                onHexClick.accept(captured);
        });

        // Hover visual sutil
        base.setOnMouseEntered(e -> base.setOpacity(0.8));
        base.setOnMouseExited(e -> base.setOpacity(1.0));

        pane.getChildren().add(base);

        // 2. Overlay de zona especial (encima del terreno)
        Color overlayColor = zoneOverlayColor(hex);
        if (overlayColor != null) {
            Polygon overlay = buildHexPolygon(cx, cy, HEX_SIZE - 1.5);
            overlay.setFill(overlayColor);
            overlay.setStroke(Color.TRANSPARENT);
            overlay.setMouseTransparent(true);
            pane.getChildren().add(overlay);
        }

        // 3. Overlay de resaltado de despliegue (verde)
        if (highlighted.contains(hex)) {
            Polygon hl = buildHexPolygon(cx, cy, HEX_SIZE - 1);
            hl.setFill(Color.web("#00ff88", 0.28));
            hl.setStroke(Color.web("#00ff88", 0.9));
            hl.setStrokeType(StrokeType.INSIDE);
            hl.setStrokeWidth(2.0);
            hl.setMouseTransparent(true);
            pane.getChildren().add(hl);
        }

        // 3b. Overlay de rango de movimiento (azul-cian)
        if (moveHighlighted.contains(hex)) {
            Polygon hl = buildHexPolygon(cx, cy, HEX_SIZE - 1);
            hl.setFill(Color.web("#00ccff", 0.22));
            hl.setStroke(Color.web("#00aaff", 0.85));
            hl.setStrokeType(StrokeType.INSIDE);
            hl.setStrokeWidth(2.0);
            hl.setMouseTransparent(true);
            pane.getChildren().add(hl);
        }

        // 3c. Overlay de objetivo de ataque (naranja-rojo)
        if (attackHighlighted.contains(hex)) {
            Polygon hl = buildHexPolygon(cx, cy, HEX_SIZE - 1);
            hl.setFill(Color.web("#ff4400", 0.25));
            hl.setStroke(Color.web("#ff6600", 0.9));
            hl.setStrokeType(StrokeType.INSIDE);
            hl.setStrokeWidth(2.5);
            hl.setMouseTransparent(true);
            pane.getChildren().add(hl);
        }

        // 3d. Overlay de skill activa (lima=movimiento, violeta=ataque)
        if (skillHighlighted.contains(hex)) {
            String fill = skillHighlightAttack ? "#cc44ff" : "#aaff00";
            String stroke = skillHighlightAttack ? "#dd88ff" : "#ccff44";
            Polygon hl = buildHexPolygon(cx, cy, HEX_SIZE - 2);
            hl.setFill(Color.web(fill, 0.28));
            hl.setStroke(Color.web(stroke, 0.95));
            hl.setStrokeType(StrokeType.INSIDE);
            hl.setStrokeWidth(2.5);
            hl.setMouseTransparent(true);
            pane.getChildren().add(hl);
        }

        // 4. Indicador de altura (triángulos ascendentes, semi-transparentes)
        if (hex.getHeight() > 0) {
            addHeightLabel(cx, cy, hex.getHeight());
        }

        // 5. Borde luminoso en bases
        if (hex.isBaseP1() || hex.isBaseP2()) {
            Polygon rim = buildHexPolygon(cx, cy, HEX_SIZE);
            rim.setFill(Color.TRANSPARENT);
            Color borderColor = hex.isBaseP1()
                    ? Color.web("#77aaff", 0.9)
                    : Color.web("#ff7777", 0.9);
            rim.setStroke(borderColor);
            rim.setStrokeType(StrokeType.INSIDE);
            rim.setStrokeWidth(2.5);
            rim.setMouseTransparent(true);
            pane.getChildren().add(rim);
        }

        // 6. Token del robot (si hay un ocupante)
        if (hex.isOccupied()) {
            renderRobotToken(cx, cy, hex.getOccupant());
        }
    }

    // ── Helpers de construcción ───────────────────────────────────────────────

    /**
     * Construye un polígono hexagonal pointy-top centrado en (cx, cy)
     * con el circunradio indicado.
     */
    private static Polygon buildHexPolygon(double cx, double cy, double radius) {
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            // Ángulo 0° → vértice superior (pointy-top): -90°, luego cada 60°
            double angle = Math.toRadians(60.0 * i - 90.0);
            hex.getPoints().add(cx + radius * Math.cos(angle));
            hex.getPoints().add(cy + radius * Math.sin(angle));
        }
        return hex;
    }

    private void addHeightLabel(double cx, double cy, int height) {
        // Pequeño símbolo ▲ repetido tantas veces como niveles de altura
        String symbol = "▲".repeat(height);
        Text label = new Text(symbol);
        label.setFill(Color.web("#ffffff", 0.5));
        label.setFont(Font.font(9));
        // Centrar el texto sobre el hexágono
        label.setX(cx - label.getLayoutBounds().getWidth() / 2.0);
        label.setY(cy + 4);
        label.setMouseTransparent(true);
        pane.getChildren().add(label);
    }

    /**
     * Dibuja el token del robot sobre el hexágono.
     * Intenta cargar el sprite específico del modelo y, si no existe, usa fallback.
     */
    private void renderRobotToken(double cx, double cy, Robot robot) {
        double spriteSize = HEX_SIZE * 1.8;
        double yOffset = HEX_SIZE * 0.15;
        Image sprite = resolveRobotSprite(robot);

        if (sprite != null) {
            ImageView spriteView = new ImageView(sprite);
            spriteView.setFitWidth(spriteSize);
            spriteView.setFitHeight(spriteSize);
            spriteView.setPreserveRatio(true);
            spriteView.setSmooth(true);
            spriteView.setMouseTransparent(true);
            spriteView.setX(cx - spriteSize / 2.0);
            spriteView.setY(cy - spriteSize / 2.0 - yOffset);
            pane.getChildren().add(spriteView);
        } else {
            // Fallback si no se encuentra el sprite de prueba
            javafx.scene.shape.Circle fallback = new javafx.scene.shape.Circle(cx, cy, HEX_SIZE * 0.4);
            fallback.setFill(Color.web("#333333"));
            fallback.setStroke(Color.WHITE);
            fallback.setStrokeWidth(1.0);
            fallback.setMouseTransparent(true);
            pane.getChildren().add(fallback);
        }

        renderHpBar(cx, cy + HEX_SIZE * 0.55, robot);
    }

    private static Image resolveRobotSprite(Robot robot) {
        String key = robot.getModelName();
        if (ROBOT_SPRITE_CACHE.containsKey(key)) {
            return ROBOT_SPRITE_CACHE.get(key);
        }

        List<String> candidates = spriteCandidatesFor(robot.getModelName());
        for (String candidate : candidates) {
            var resource = HexRenderer.class.getResource("/com/titozeio/sprites/" + candidate + ".png");
            if (resource != null) {
                Image image = new Image(resource.toExternalForm());
                ROBOT_SPRITE_CACHE.put(key, image);
                return image;
            }
        }

        ROBOT_SPRITE_CACHE.put(key, null);
        return null;
    }

    private static List<String> spriteCandidatesFor(String modelName) {
        String normalized = modelName == null ? "" : modelName.trim().toLowerCase();
        switch (normalized) {
            case "victory saber":
                return List.of("saberprime", "death_knight", "scout");
            case "death knight":
                return List.of("death_knight", "saberprime", "scout");
            case "scout":
                return List.of("scout", "saberprime", "death_knight");
            case "bullseye":
                return List.of("bullseye", "saberprime", "death_knight", "scout");
            case "bulwark":
                return List.of("bulwark", "saberprime", "death_knight", "scout");
            case "ice age":
                return List.of("ice_age", "saberprime", "death_knight", "scout");
            default:
                return List.of("saberprime", "death_knight", "scout");
        }
    }

    private void renderHpBar(double cx, double barY, Robot robot) {
        int hp = robot.getCurrentHp();
        int maxHp = robot.getMaxHp();
        double barW = HEX_SIZE * 0.9;
        double barH = 5.0;

        // Fondo de la barra
        Rectangle bg = new Rectangle(cx - barW / 2.0, barY, barW, barH);
        bg.setFill(Color.web("#220000"));
        bg.setArcWidth(3);
        bg.setArcHeight(3);
        bg.setMouseTransparent(true);
        pane.getChildren().add(bg);

        // Segmentos de HP
        double segW = (barW - (maxHp - 1)) / maxHp;
        for (int i = 0; i < hp; i++) {
            double segX = cx - barW / 2.0 + i * (segW + 1);
            Color segColor = (hp > maxHp * 0.5) ? Color.web("#44dd44")
                    : (hp > maxHp * 0.25) ? Color.web("#dddd22")
                            : Color.web("#dd2222");
            Rectangle seg = new Rectangle(segX, barY, segW, barH);
            seg.setFill(segColor);
            seg.setMouseTransparent(true);
            pane.getChildren().add(seg);
        }
    }

    /** Devuelve 2 iniciales del nombre del modelo. */
    private static String getInitials(String modelName) {
        String[] words = modelName.trim().split("\\s+");
        if (words.length >= 2) {
            return ("" + words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
        } else if (modelName.length() >= 2) {
            return modelName.substring(0, 2).toUpperCase();
        }
        return modelName.toUpperCase();
    }

    // ── Conversión de coordenadas ─────────────────────────────────────────────

    /**
     * Convierte coordenadas axiales (q, r) en píxeles (x, y).
     * Orientación pointy-top.
     */
    public static double[] axialToPixel(int q, int r) {
        double x = OFFSET_X + HEX_SIZE * (Math.sqrt(3) * q + Math.sqrt(3) / 2.0 * r);
        double y = OFFSET_Y + HEX_SIZE * (1.5 * r);
        return new double[] { x, y };
    }

    // ── Paleta ────────────────────────────────────────────────────────────────

    private static Color terrainColor(Hexagon hex) {
        switch (hex.getTerrain()) {
            case VEGETATION:
                return C_VEGETATION;
            case WATER_SHALLOW:
                return C_WATER_SHALLOW;
            case WATER_DEEP:
                return C_WATER_DEEP;
            default: // NORMAL
                switch (hex.getHeight()) {
                    case 2:
                        return C_NORMAL_H2;
                    case 1:
                        return C_NORMAL_H1;
                    default:
                        return C_NORMAL;
                }
        }
    }

    private static Color zoneOverlayColor(Hexagon hex) {
        if (hex.isBaseP1())
            return C_BASE_P1;
        if (hex.isBaseP2())
            return C_BASE_P2;
        if (hex.isDeployZoneP1())
            return C_DEPLOY_P1;
        if (hex.isDeployZoneP2())
            return C_DEPLOY_P2;
        return null;
    }
}
