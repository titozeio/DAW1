package com.titozeio.ui;

import com.titozeio.engine.Hexagon;
import com.titozeio.engine.Map;
import com.titozeio.engine.Robot;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

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
    private static final Color C_DEPLOY_P1 = Color.web("#cc3333", 0.25);
    private static final Color C_DEPLOY_P2 = Color.web("#3366cc", 0.25);
    private static final Color C_BASE_P1 = Color.web("#ff5555", 0.45);
    private static final Color C_BASE_P2 = Color.web("#5599ff", 0.45);

    // ── Borde de los hexágonos ────────────────────────────────────────────────
    private static final Color C_STROKE = Color.web("#0a1008", 0.6);
    private static final double STROKE_WIDTH = 1.2;
    /** Cache de sprites cargados por nombre de modelo de robot. */
    private static final java.util.Map<String, Image> ROBOT_SPRITE_CACHE = new HashMap<>();
    /** Cache de tiles de hexágonos por clave de terreno/altura. */
    private static final java.util.Map<String, Image> HEX_TILE_CACHE = new HashMap<>();
    private static final boolean DEBUG_HEX_TILES = true;

    // ── Estado interno ────────────────────────────────────────────────────────
    /** Pane objetivo donde se dibujan los nodos. */
    private final Pane pane;

    /**
     * Callback opcional: se llama con el Hexagon cuando el jugador hace clic en él.
     */
    private Consumer<Hexagon> onHexClick;
    /** Callback opcional: hover sobre un hexágono. */
    private Consumer<Hexagon> onHexHover;
    /** Callback opcional: salida de hover de un hexágono. */
    private Runnable onHexHoverExit;

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

    /**
     * Hexágono que debe reproducir la animación de aterrizaje en el próximo
     * renderizado.
     */
    private Hexagon landingHex = null;

    /** Datos para animación de ataque/habilidad */
    private Hexagon attackerHexScope = null;
    private Hexagon targetHexScope = null;
    private boolean isHealScope = false;

    // ── Constructor ───────────────────────────────────────────────────────────
    /**
     * Crea un renderizador vinculado a un contenedor de JavaFX.
     * 
     * @param pane El Pane donde se dibujarán los hexágonos y robots.
     */
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

    /** Registra el handler de hover al entrar en un hexágono. */
    public void setOnHexHover(Consumer<Hexagon> handler) {
        this.onHexHover = handler;
    }

    /** Registra el handler de hover al salir de un hexágono. */
    public void setOnHexHoverExit(Runnable handler) {
        this.onHexHoverExit = handler;
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

    /**
     * Marca un hexágono para que su ocupante aterrice con animación en el próximo
     * render.
     */
    public void markLanding(Hexagon hex) {
        this.landingHex = hex;
    }

    /** Registra un ataque o habilidad para animarla en el próximo render. */
    public void markAttack(Hexagon attacker, Hexagon target, boolean isHeal) {
        this.attackerHexScope = attacker;
        this.targetHexScope = target;
        this.isHealScope = isHeal;
    }

    /** Versión por defecto (ataque normal) */
    public void markAttack(Hexagon attacker, Hexagon target) {
        markAttack(attacker, target, false);
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

        // Primera capa: Los hexágonos (terreno, overlays, resaltados)
        for (Hexagon[] row : grid) {
            for (Hexagon hex : row) {
                if (hex != null) {
                    renderHex(hex);
                }
            }
        }

        // Segunda capa: Los robots (siempre encima de todo el terreno)
        for (Hexagon[] row : grid) {
            for (Hexagon hex : row) {
                if (hex != null && hex.isOccupied()) {
                    double[] center = axialToPixel(hex.getQ(), hex.getR());
                    renderRobotToken(center[0], center[1], hex.getOccupant());
                }
            }
        }

        // Limpiar estados de animación después de un ciclo completo de renderizado
        this.landingHex = null;
        this.attackerHexScope = null;
        this.targetHexScope = null;
    }

    // ── Renderizado interno ───────────────────────────────────────────────────

    private void renderHex(Hexagon hex) {
        double[] center = axialToPixel(hex.getQ(), hex.getR());
        double cx = center[0];
        double cy = center[1];

        // 1. Polígono base (borde + interacción)
        Polygon base = buildHexPolygon(cx, cy, HEX_SIZE);

        // Variedad extra: volteo horizontal determinista (50% de probabilidad)
        // EXCEPCIÓN: h1, h2 y vegetation_2 (tienen sombras direccionales que no deben
        // invertirse)
        int variantHash = stableVariantIndex(hex.getQ(), hex.getR());
        boolean hasShadows = (hex.getHeight() > 0);
        if (hex.getTerrain() == com.titozeio.enums.TerrainType.VEGETATION) {
            // vegetation_2 es la segunda variante (indice 1 de 3 en hexTileCandidates)
            if (Math.floorMod(variantHash, 3) == 1) {
                hasShadows = true;
            }
        }

        if (!hasShadows && Math.abs(variantHash) % 2 == 0) {
            base.setScaleX(-1.0);
        }
        Image terrainTile = resolveHexTileImage(hex);
        if (terrainTile == null) {
            base.setFill(terrainColor(hex));
        } else {
            double tileW = Math.sqrt(3) * HEX_SIZE;
            double tileH = 2.0 * HEX_SIZE;
            base.setFill(new javafx.scene.paint.ImagePattern(
                    terrainTile,
                    cx - tileW / 2.0,
                    cy - tileH / 2.0,
                    tileW,
                    tileH,
                    false));
        }
        base.setStroke(C_STROKE);
        base.setStrokeType(StrokeType.INSIDE);
        base.setStrokeWidth(STROKE_WIDTH);

        // Evento de clic en el hexágono
        final Hexagon captured = hex;
        base.setOnMouseClicked(e -> {
            if (onHexClick != null)
                onHexClick.accept(captured);
        });

        // Hover visual sin alterar alpha del relleno (evita que se vea el fondo).
        base.setOnMouseEntered(e -> {
            base.setStroke(Color.web("#bfefff", 0.95));
            base.setStrokeWidth(STROKE_WIDTH + 0.8);
            if (onHexHover != null) {
                onHexHover.accept(captured);
            }
        });
        base.setOnMouseExited(e -> {
            base.setStroke(C_STROKE);
            base.setStrokeWidth(STROKE_WIDTH);
            if (onHexHoverExit != null) {
                onHexHoverExit.run();
            }
        });

        pane.getChildren().add(base);

        // 3. Overlay de zona especial (encima del terreno)
        Color overlayColor = zoneOverlayColor(hex);
        if (overlayColor != null) {
            Polygon overlay = buildHexPolygon(cx, cy, HEX_SIZE - 1.5);
            overlay.setFill(overlayColor);
            overlay.setStroke(Color.TRANSPARENT);
            overlay.setMouseTransparent(true);
            pane.getChildren().add(overlay);
        }

        // 4. Overlay de resaltado de despliegue (verde)
        if (highlighted.contains(hex)) {
            Polygon hl = buildHexPolygon(cx, cy, HEX_SIZE - 1);
            boolean isP1Deploy = hex.isDeployZoneP1();
            String fill = isP1Deploy ? "#ff6666" : "#66a3ff";
            String stroke = isP1Deploy ? "#ff9a9a" : "#9ac3ff";
            hl.setFill(Color.web(fill, 0.32));
            hl.setStroke(Color.web(stroke, 0.95));
            hl.setStrokeType(StrokeType.INSIDE);
            hl.setStrokeWidth(2.0);
            hl.setMouseTransparent(true);
            pane.getChildren().add(hl);
        }

        // 4b. Overlay de rango de movimiento (azul-cian)
        if (moveHighlighted.contains(hex)) {
            Polygon hl = buildHexPolygon(cx, cy, HEX_SIZE - 1);
            hl.setFill(Color.web("#00ccff", 0.22));
            hl.setStroke(Color.web("#00aaff", 0.85));
            hl.setStrokeType(StrokeType.INSIDE);
            hl.setStrokeWidth(2.0);
            hl.setMouseTransparent(true);
            pane.getChildren().add(hl);
        }

        // 4c. Overlay de objetivo de ataque (naranja-rojo)
        if (attackHighlighted.contains(hex)) {
            Polygon hl = buildHexPolygon(cx, cy, HEX_SIZE - 1);
            hl.setFill(Color.web("#ff4400", 0.25));
            hl.setStroke(Color.web("#ff6600", 0.9));
            hl.setStrokeType(StrokeType.INSIDE);
            hl.setStrokeWidth(2.5);
            hl.setMouseTransparent(true);
            pane.getChildren().add(hl);
        }

        // 4d. Overlay de skill activa (lima=movimiento, violeta=ataque)
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

        // 5. Indicador de altura (triángulos ascendentes, semi-transparentes)
        if (hex.getHeight() > 0 && terrainTile == null) {
            addHeightLabel(cx, cy, hex.getHeight());
        }

        // 6. Borde luminoso en bases
        if (hex.isBaseP1() || hex.isBaseP2()) {
            Polygon rim = buildHexPolygon(cx, cy, HEX_SIZE);
            rim.setFill(Color.TRANSPARENT);
            Color borderColor = hex.isBaseP1()
                    ? Color.web("#ff7777", 0.9)
                    : Color.web("#77aaff", 0.9);
            rim.setStroke(borderColor);
            rim.setStrokeType(StrokeType.INSIDE);
            rim.setStrokeWidth(2.5);
            rim.setMouseTransparent(true);
            pane.getChildren().add(rim);
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

        Node robotNode = null;
        Color borderColor = resolveOwnerBorderColor(robot);
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
            robotNode = spriteView;
        } else {
            // Fallback si no se encuentra el sprite de prueba
            javafx.scene.shape.Circle fallback = new javafx.scene.shape.Circle(cx, cy, HEX_SIZE * 0.4);
            fallback.setFill(Color.web("#333333"));
            fallback.setStroke(borderColor != null ? borderColor : Color.WHITE);
            fallback.setStrokeWidth(1.0);
            fallback.setMouseTransparent(true);
            pane.getChildren().add(fallback);
            robotNode = fallback;
        }

        Node hpBarNode = renderHpBar(cx, cy + HEX_SIZE * 0.55, robot);

        // 0. Aplicar efectos de estado visuales persistentes
        if (borderColor != null) {
            robotNode.getProperties().put("ownerBorderColor", borderColor);
        }
        applyRobotStatusVisuals(robotNode, robot);

        // 1. Animación de aterrizaje (despliegue)
        if (robot.getPosition() != null && robot.getPosition().equals(landingHex)) {
            applyLandingAnimation(robotNode, hpBarNode);
        }
        // 2. Animación de desplazamiento (movimiento)
        else if (robot.getPreviousPosition() != null) {
            applyMoveAnimation(robotNode, hpBarNode, robot.getPreviousPosition(), cx, cy);
            robot.clearPreviousPosition();
        }
        // 3. Animación de ataque (retroceso o impacto)
        else if (robot.getPosition() != null) {
            if (robot.getPosition().equals(attackerHexScope)) {
                applyRecoilAnimation(robotNode, hpBarNode, targetHexScope);
                drawLaserTracer(attackerHexScope, targetHexScope, isHealScope);
            } else if (robot.getPosition().equals(targetHexScope)) {
                applyImpactAnimation(robotNode, hpBarNode, isHealScope, robot);
            }
        }
    }

    /**
     * Aplica los efectos visuales correspondientes a los estados del robot (ej:
     * tinte azul si está congelado).
     */
    private void applyRobotStatusVisuals(Node robotNode, Robot robot) {
        if (robot == null || robotNode == null)
            return;
        javafx.scene.effect.Effect baseEffect = null;
        Object borderObj = robotNode.getProperties().get("ownerBorderColor");
        if (borderObj instanceof Color borderColor) {
            javafx.scene.effect.DropShadow outline = new javafx.scene.effect.DropShadow();
            outline.setRadius(1.0);
            outline.setSpread(1.0);
            outline.setColor(borderColor);
            baseEffect = outline;
        }
        if (robot.hasState(com.titozeio.enums.RobotState.FROZEN)) {
            ColorAdjust ice = new ColorAdjust();
            ice.setBrightness(0.35); // Tinta blanca/clara
            ice.setSaturation(-0.2); // Desaturar un poco
            ice.setHue(-0.05); // Toque azulado frío
            ice.setInput(baseEffect);
            robotNode.setEffect(ice);
        } else {
            robotNode.setEffect(baseEffect);
        }
    }

    private void drawLaserTracer(Hexagon from, Hexagon to, boolean isHeal) {
        if (from == null || to == null)
            return;
        double[] p1 = axialToPixel(from.getQ(), from.getR());
        double[] p2 = axialToPixel(to.getQ(), to.getR());

        // Offset visual para que el láser salga "del arma/pecho"
        double beamY = -12.0;

        Line laser = new Line(p1[0], p1[1] + beamY, p2[0], p2[1] + beamY);
        Color beamColor = isHeal ? Color.web("#44ff44") : Color.web("#57C0EF");
        laser.setStroke(beamColor);
        laser.setStrokeWidth(3.0);
        laser.setStrokeLineCap(StrokeLineCap.ROUND);
        laser.setOpacity(0.0);
        laser.setMouseTransparent(true);

        Line glow = new Line(p1[0], p1[1] + beamY, p2[0], p2[1] + beamY);
        glow.setStroke(Color.WHITE);
        glow.setStrokeWidth(1.2);
        glow.setOpacity(0.0);
        glow.setMouseTransparent(true);

        pane.getChildren().addAll(laser, glow);

        Timeline anim = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(laser.opacityProperty(), 0),
                        new KeyValue(glow.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(50), new KeyValue(laser.opacityProperty(), 1),
                        new KeyValue(glow.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(250), new KeyValue(laser.opacityProperty(), 0),
                        new KeyValue(glow.opacityProperty(), 0)));

        anim.setOnFinished(e -> pane.getChildren().removeAll(laser, glow));
        anim.play();
    }

    private void applyRecoilAnimation(Node robot, Node hpBar, Hexagon target) {
        if (target == null)
            return;

        // Simplemente retrocedemos un poco en dirección contraria al objetivo
        // (aproximado)
        // Para hacerlo perfecto necesitaríamos la posición actual del atacante, pero
        // un pequeño retroceso visual "hacia atrás" suele ser suficiente.

        TranslateTransition recoil = new TranslateTransition(Duration.millis(120), robot);
        recoil.setByX(-10); // Retroceder a la izquierda
        recoil.setByY(5);
        recoil.setAutoReverse(true);
        recoil.setCycleCount(2);
        recoil.play();

        if (hpBar != null) {
            TranslateTransition hpRecoil = new TranslateTransition(Duration.millis(120), hpBar);
            hpRecoil.setByX(-10);
            hpRecoil.setByY(5);
            hpRecoil.setAutoReverse(true);
            hpRecoil.setCycleCount(2);
            hpRecoil.play();
        }
    }

    private void applyImpactAnimation(Node robotNode, Node hpBar, boolean isHeal, Robot robot) {
        ColorAdjust flashEffect = new ColorAdjust();
        flashEffect.setBrightness(0.0);
        if (isHeal) {
            flashEffect.setHue(0.4); // Verde sanador
        } else {
            flashEffect.setHue(-0.05); // Un toque rojizo
        }
        flashEffect.setSaturation(0.6);
        robotNode.setEffect(flashEffect);

        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(flashEffect.brightnessProperty(), 0.0)),
                new KeyFrame(Duration.millis(100), new KeyValue(flashEffect.brightnessProperty(), 0.8)),
                new KeyFrame(Duration.millis(300), new KeyValue(flashEffect.brightnessProperty(), 0.0)));
        flash.setOnFinished(e -> applyRobotStatusVisuals(robotNode, robot));
        flash.play();

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), robotNode);
        shake.setByX(4);
        shake.setAutoReverse(true);
        shake.setCycleCount(4);
        shake.play();
    }

    private void applyMoveAnimation(Node robot, Node hpBar, Hexagon fromHex, double targetX, double targetY) {
        double[] oldPos = axialToPixel(fromHex.getQ(), fromHex.getR());
        double offsetX = oldPos[0] - targetX;
        double offsetY = oldPos[1] - targetY;

        Duration duration = Duration.millis(400);

        TranslateTransition moveRobot = new TranslateTransition(duration, robot);
        moveRobot.setFromX(offsetX);
        moveRobot.setFromY(offsetY);
        moveRobot.setToX(0);
        moveRobot.setToY(0);
        moveRobot.setInterpolator(Interpolator.EASE_BOTH);
        moveRobot.play();

        if (hpBar != null) {
            TranslateTransition moveHp = new TranslateTransition(duration, hpBar);
            moveHp.setFromX(offsetX);
            moveHp.setFromY(offsetY);
            moveHp.setToX(0);
            moveHp.setToY(0);
            moveHp.setInterpolator(Interpolator.EASE_BOTH);
            moveHp.play();
        }
    }

    private void applyLandingAnimation(Node robotNode, Node hpBarNode) {
        robotNode.setOpacity(0);
        robotNode.setScaleX(2.0);
        robotNode.setScaleY(2.0);
        robotNode.setTranslateY(-300);

        Duration d = Duration.millis(600);

        TranslateTransition t = new TranslateTransition(d, robotNode);
        t.setToY(0);
        t.setInterpolator(Interpolator.EASE_IN);

        ScaleTransition s = new ScaleTransition(d, robotNode);
        s.setToX(1.0);
        s.setToY(1.0);

        FadeTransition f = new FadeTransition(d, robotNode);
        f.setToValue(1.0);

        ParallelTransition pt = new ParallelTransition(t, s, f);

        if (hpBarNode != null) {
            hpBarNode.setOpacity(0);
            hpBarNode.setTranslateY(-300);
            TranslateTransition tHp = new TranslateTransition(d, hpBarNode);
            tHp.setToY(0);
            tHp.setInterpolator(Interpolator.EASE_IN);
            FadeTransition fHp = new FadeTransition(d, hpBarNode);
            fHp.setToValue(1.0);
            pt.getChildren().addAll(tHp, fHp);
        }

        pt.play();
    }

    private static Image resolveRobotSprite(Robot robot) {
        String key = robot == null ? "" : robot.getModelName();
        if (ROBOT_SPRITE_CACHE.containsKey(key)) {
            return ROBOT_SPRITE_CACHE.get(key);
        }

        List<String> candidates = spriteCandidatesFor(robot == null ? null : robot.getModelName());
        for (String candidate : candidates) {
            var resource = HexRenderer.class.getResource("/com/titozeio/sprites/figures/" + candidate + ".png");
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
                return withFallbacks("saberprime");
            case "death knight":
                return withFallbacks("death_knight");
            case "scout":
                return withFallbacks("scout");
            case "bullseye":
                return withFallbacks("bullseye");
            case "bulwark":
                return withFallbacks("bulwark", "Bulwark");
            case "ice age":
                return withFallbacks("ice_age");
            default:
                return withFallbacks("saberprime");
        }
    }

    private static List<String> withFallbacks(String primaryBase) {
        return withFallbacks(primaryBase, primaryBase);
    }

    private static List<String> withFallbacks(String primaryBase, String altPrimaryBase) {
        return List.of(
                primaryBase,
                altPrimaryBase,
                "saberprime",
                "death_knight",
                "scout");
    }

    private static Color resolveOwnerBorderColor(Robot robot) {
        if (robot == null || robot.getOwner() == null || robot.getOwner().getName() == null) {
            return null;
        }
        String ownerName = robot.getOwner().getName().trim().toLowerCase().replace(" ", "");
        if (ownerName.contains("jugador1") || ownerName.contains("player1") || ownerName.contains("p1")
                || ownerName.endsWith("1")) {
            return Color.web("#ff4444");
        }
        if (ownerName.contains("jugador2") || ownerName.contains("player2") || ownerName.contains("p2")
                || ownerName.endsWith("2")) {
            return Color.web("#4488ff");
        }
        return null;
    }

    private Node renderHpBar(double cx, double barY, Robot robot) {
        int hp = robot.getCurrentHp();
        int maxHp = robot.getMaxHp();
        double barW = HEX_SIZE * 0.9;
        double barH = 5.0;

        Pane hpContainer = new Pane();
        hpContainer.setMouseTransparent(true);

        // Fondo de la barra: gris oscuro al 80%, sin redondeado.
        Rectangle bg = new Rectangle(cx - barW / 2.0, barY, barW, barH);
        bg.setFill(Color.web("#2d2d2d", 0.8));
        hpContainer.getChildren().add(bg);

        // Segmentos de HP: uno por HP máximo. Perdidos al 40% opacidad.
        double gap = 1.0;
        double segW = (barW - gap * (maxHp + 1)) / maxHp;
        for (int i = 0; i < maxHp; i++) {
            double segX = cx - barW / 2.0 + gap + i * (segW + gap);
            Rectangle seg = new Rectangle(segX, barY + 1, segW, Math.max(1.0, barH - 2));
            seg.setFill(Color.web("#44dd44"));
            seg.setOpacity(i < hp ? 1.0 : 0.4);
            hpContainer.getChildren().add(seg);
        }

        pane.getChildren().add(hpContainer);
        return hpContainer;
    }

    /** Devuelve 2 iniciales del nombre del modelo. */
    @Deprecated(forRemoval = true)
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

    // ── Tiles de terreno ─────────────────────────────────────────────────────

    private static Image resolveHexTileImage(Hexagon hex) {
        List<String> candidates = hexTileCandidates(hex);
        for (String candidate : candidates) {
            if (HEX_TILE_CACHE.containsKey(candidate)) {
                Image cached = HEX_TILE_CACHE.get(candidate);
                if (cached != null) {
                    return cached;
                }
                continue; // Ya se intentó y falló
            }

            var resource = HexRenderer.class.getResource(candidate);
            if (resource != null) {
                Image image = new Image(resource.toExternalForm(), false);
                if (!image.isError() && image.getWidth() > 0 && image.getHeight() > 0) {
                    HEX_TILE_CACHE.put(candidate, image);
                    if (DEBUG_HEX_TILES) {
                        System.out.println("[HexRenderer] Loaded tile " + candidate + " -> "
                                + (int) image.getWidth() + "x" + (int) image.getHeight());
                    }
                    return image;
                }
            } else {
                Image image = tryLoadFromFile(candidate);
                if (image != null) {
                    HEX_TILE_CACHE.put(candidate, image);
                    if (DEBUG_HEX_TILES) {
                        System.out.println("[HexRenderer] Loaded tile (fs) " + candidate + " -> "
                                + (int) image.getWidth() + "x" + (int) image.getHeight());
                    }
                    return image;
                }
            }
            // Marcar como no disponible para no reintentar
            HEX_TILE_CACHE.put(candidate, null);
        }

        if (DEBUG_HEX_TILES) {
            System.out.println("[HexRenderer] No tile found for hex at (" + hex.getQ() + "," + hex.getR() + ") among "
                    + candidates);
        }
        return null;
    }

    private static Image tryLoadFromFile(String resourcePath) {
        String userDir = System.getProperty("user.dir");
        if (userDir == null || userDir.isBlank()) {
            return null;
        }
        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;

        java.nio.file.Path direct = java.nio.file.Paths.get(userDir, "src", "main", "resources", normalized);
        Image image = loadImageIfExists(direct);
        if (image != null) {
            return image;
        }

        java.nio.file.Path nested = java.nio.file.Paths.get(userDir, "daw1", "src", "main", "resources", normalized);
        return loadImageIfExists(nested);
    }

    private static Image loadImageIfExists(java.nio.file.Path path) {
        if (path == null || !java.nio.file.Files.exists(path)) {
            return null;
        }
        Image image = new Image(path.toUri().toString(), false);
        if (image.isError() || image.getWidth() <= 0 || image.getHeight() <= 0) {
            return null;
        }
        return image;
    }

    private static List<String> hexTileCandidates(Hexagon hex) {
        int variantIndex = stableVariantIndex(hex.getQ(), hex.getR());
        switch (hex.getTerrain()) {
            case VEGETATION:
                return rotatedVariants(variantIndex,
                        List.of(
                                "/com/titozeio/ui/hexes/vegetation_1.png",
                                "/com/titozeio/ui/hexes/vegetation_2.png",
                                "/com/titozeio/ui/hexes/vegetation.png"),
                        List.of(
                                "/com/titozeio/images/terrain/vegetation.png"));
            case WATER_SHALLOW:
                return List.of(
                        "/com/titozeio/ui/hexes/water_shallow_1.png",
                        "/com/titozeio/ui/hexes/water_shallow1.png",
                        "/com/titozeio/ui/hexes/water_shallow.png",
                        "/com/titozeio/images/terrain/water_shallow.png");
            case WATER_DEEP:
                return List.of(
                        "/com/titozeio/ui/hexes/water_deep_1.png",
                        "/com/titozeio/ui/hexes/water_deep1.png",
                        "/com/titozeio/ui/hexes/water_deep.png",
                        "/com/titozeio/images/terrain/water_deep.png");
            default:
                break;
        }

        switch (hex.getHeight()) {
            case 2:
                return rotatedVariants(variantIndex,
                        List.of(
                                "/com/titozeio/ui/hexes/normal_h2__1.png",
                                "/com/titozeio/ui/hexes/normal_h2__2.png",
                                "/com/titozeio/ui/hexes/normal_h2.png"),
                        List.of(
                                "/com/titozeio/images/terrain/plateau_h2.png",
                                "/com/titozeio/ui/hexes/normal_1.png",
                                "/com/titozeio/ui/hexes/normal_2.png",
                                "/com/titozeio/ui/hexes/normal.png",
                                "/com/titozeio/images/terrain/plain.png"));
            case 1:
                return rotatedVariants(variantIndex,
                        List.of(
                                "/com/titozeio/ui/hexes/normal_h1_1.png",
                                "/com/titozeio/ui/hexes/normal_h1.png"),
                        List.of(
                                "/com/titozeio/images/terrain/hill_h1.png",
                                "/com/titozeio/ui/hexes/normal_1.png",
                                "/com/titozeio/ui/hexes/normal_2.png",
                                "/com/titozeio/ui/hexes/normal.png",
                                "/com/titozeio/images/terrain/plain.png"));
            default:
                return rotatedVariants(variantIndex,
                        List.of(
                                "/com/titozeio/ui/hexes/normal_1.png",
                                "/com/titozeio/ui/hexes/normal_2.png",
                                "/com/titozeio/ui/hexes/normal.png"),
                        List.of(
                                "/com/titozeio/images/terrain/plain.png"));
        }
    }

    private static List<String> rotatedVariants(int startIndex, List<String> variants, List<String> fallbacks) {
        if (variants == null || variants.isEmpty()) {
            return fallbacks == null ? List.of() : fallbacks;
        }
        int offset = Math.floorMod(startIndex, variants.size());
        java.util.ArrayList<String> ordered = new java.util.ArrayList<>(
                variants.size() + (fallbacks == null ? 0 : fallbacks.size()));
        for (int i = 0; i < variants.size(); i++) {
            ordered.add(variants.get((offset + i) % variants.size()));
        }
        if (fallbacks != null) {
            ordered.addAll(fallbacks);
        }
        return ordered;
    }

    private static int stableVariantIndex(int q, int r) {
        int h = 17;
        h = 31 * h + q;
        h = 31 * h + r;
        return h;
    }
}
