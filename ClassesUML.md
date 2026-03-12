# UML de Clases — DAW1

Diagramas de clases generados por **ingeniería inversa** del código fuente (marzo 2026).
Incluye también una sección final de **arquitectura objetivo planificada** para la futura integración de IA, simulación masiva y telemetría.
Divididos en secciones para mantener legibilidad. Las relaciones de los enums de apoyo se describen en la sección 8.

---

## 1. Núcleo del dominio (`engine` + `model`)

```mermaid
classDiagram
    class Game {
      -Map map
      -Player p1
      -Player p2
      -Player currentPlayer
      -int turnCounter
      -GamePhase phase
      -Screen currentScreen
      -List~VictoryCondition~ victoryConditions
      -boolean musicEnabled
      -boolean sfxEnabled
      +start()
      +nextTurn()
      +checkVictory() Player
      +getOpponent(Player) Player
      +displayScreen(Screen)
      +isMusicEnabled() boolean
      +setMusicEnabled(boolean)
      +isSfxEnabled() boolean
      +setSfxEnabled(boolean)
    }

    class Map {
      -Hexagon[][] grid
      +getDistance(Hexagon, Hexagon) int
      +hasVisibility(Hexagon, Hexagon) boolean
      +getCoverModifier(Hexagon, Hexagon) float
    }

    class Hexagon {
      -int q
      -int r
      -TerrainType terrain
      -int height
      -Robot occupant
      -boolean isDeployZoneP1
      -boolean isDeployZoneP2
      -boolean isBaseP1
      -boolean isBaseP2
      +getMovementCost(Robot, int) int
      +getDamageModifier() float
      +getAttackDamageModifier() float
      +getCoverTypeAgainst(int) CoverType
      +isAccessibleForCombat() boolean
      +isOccupied() boolean
      +isReachableFrom(int) boolean
      +getS() int
    }

    class Player {
      -String name
      -List~Robot~ units
      -Hexagon baseLocation
      -int baseControlTurns
      +addRobot(Robot)
      +getBaseControlTurns() int
      +incrementBaseControlTurns()
      +resetBaseControlTurns()
    }

    class Robot {
      -String modelName
      -String description
      -int maxHp
      -int currentHp
      -int movementPoints
      -Weapon weapon
      -Skill skill
      -Player owner
      -Hexagon position
      -RobotStateBehavior currentState
      -Map~RobotState, Integer~ activeStates
      -boolean usedMovement
      -boolean usedAttack
      +move(Hexagon)
      +attack(Robot, Map)
      +useSkill(Object)
      +takeDamage(int)
      +heal(int)
      +resetActions()
      +canAttack(Robot, Map) boolean
      +isDestroyed() boolean
      +isExhausted() boolean
      +applyState(RobotState, int)
      +removeState(RobotState)
      +hasState(RobotState) boolean
      +getStateDuration(RobotState) int
      +tickStatusEffects()
      +updateStatusBehavior()
    }

    class Weapon {
      -String name
      -int range
      -int damage
      -AttackType attackType
      -String description
    }

    class Objective {
      -String name
      -Hexagon location
      -Player controllingPlayer
      -int turnsHeld
    }

    class MapFactory {
      +createMap1() Map
    }

    class MovementCalculator {
      +getReachable(Robot, Map) Set~Hexagon~
    }

    class RobotFactory {
      +createTeam(List~RobotTemplate~, Player) List~Robot~
    }

    class RobotTemplate {
      <<enum>>
      SABER_PRIME
      BULLSEYE
      BULWARK
      SCOUT
      DEATH_KNIGHT
      ICE_AGE
      +createRobot(Player) Robot
    }

    Game *-- Map
    Game *-- Player
    Game o-- Screen
    Game o-- VictoryCondition
    Player o-- Robot
    Player o-- Hexagon : baseLocation
    Robot *-- Weapon
    Robot *-- Skill
    Robot --> Player : owner
    Robot --> Hexagon : position
    Hexagon o-- Robot : occupant
    Map *-- Hexagon
    MovementCalculator ..> Map
    MovementCalculator ..> Robot
    MovementCalculator ..> Hexagon
    MapFactory ..> Map
    MapFactory ..> Hexagon
    RobotFactory ..> RobotTemplate
    RobotTemplate ..> Robot
    RobotTemplate ..> Weapon
    Objective --> Hexagon
    Objective --> Player
```

---

## 2. Comandos y flujo de acciones (Command Pattern)

```mermaid
classDiagram
    class GameCommand {
      <<interface>>
      +execute()
      +undo()
      +getDescription() String
    }

    class MoveCommand {
      -Robot actor
      -Hexagon target
      -Hexagon origin
      -int fallDamage
      +execute()
      +undo()
      +getDescription() String
    }

    class AttackCommand {
      -Robot attacker
      -Robot defender
      -Map map
      -int damageDealt
      -boolean fatal
      +execute()
      +undo()
      +getDamageDealt() int
      +isFatal() boolean
      +getDescription() String
    }

    class SkillCommand {
      -Robot actor
      -Object target
      +execute()
      +undo()
      +getDescription() String
    }

    class ActionManager {
      <<Singleton>>
      -Stack~GameCommand~ history
      +getInstance() ActionManager
      +executeCommand(GameCommand)
      +undoLastCommand()
      +clearHistory()
    }

    class GameScreen {
      -Robot selectedRobot
      -SelectionMode selectionMode
      +executeMove(Hexagon)
      +executeAttack(Hexagon)
      +executeSkill(Hexagon)
    }

    GameCommand <|.. MoveCommand
    GameCommand <|.. AttackCommand
    GameCommand <|.. SkillCommand
    ActionManager o-- GameCommand
    GameScreen ..> ActionManager
    GameScreen ..> MoveCommand
    GameScreen ..> AttackCommand
    GameScreen ..> SkillCommand
```

---

## 3. Skills y estados del robot (State Pattern)

```mermaid
classDiagram
    class Skill {
      <<abstract>>
      -String name
      -SkillType type
      -int cooldown
      -int currentCooldown
      +execute(Robot, Object)*
      +isReady() boolean
      +tickCooldown()
      +resetCooldown()
      +getName() String
      +getType() SkillType
      +getDisplayStats() List~String[]~*
    }

    class FreezeRay {
      -int range
      -int damage
      -int freezeDuration
    }
    class GuidedMissile {
      -int range
      -int damage
    }
    class PlasmaSaberAttack {
      -int range
      -int damage
    }
    class RepairNanobots {
      -int range
      -int healAmount
    }
    class JetpackBoost {
      -int movementPoints
    }
    class TurboPropulsion {
      -int movementPoints
    }

    class RobotStateBehavior {
      <<interface>>
      +canMove(Robot) boolean
      +canAttack(Robot) boolean
      +canUseSkill(Robot) boolean
      +onTurnBegin(Robot)
    }

    class NormalState
    class FrozenState {
      -int turnsRemaining
    }
    class ExhaustedState

    class Robot {
      -Skill skill
      -RobotStateBehavior currentState
      -Map~RobotState, Integer~ activeStates
      +useSkill(Object)
      +applyState(RobotState, int)
      +tickStatusEffects()
      +updateStatusBehavior()
      +resetActions()
    }

    Skill <|-- FreezeRay
    Skill <|-- GuidedMissile
    Skill <|-- PlasmaSaberAttack
    Skill <|-- RepairNanobots
    Skill <|-- JetpackBoost
    Skill <|-- TurboPropulsion

    Robot *-- Skill
    Robot o-- RobotStateBehavior : currentState
    RobotStateBehavior <|.. NormalState
    RobotStateBehavior <|.. FrozenState
    RobotStateBehavior <|.. ExhaustedState
```

---

## 4. Condiciones de victoria (Strategy Pattern)

```mermaid
classDiagram
    class VictoryCondition {
      <<abstract>>
      +check(Game) Player*
    }

    class EliminationVC {
      +check(Game) Player
    }

    class BaseCaptureVC {
      -int turnsToCapture
      +check(Game) Player
      +getTurnsToCapture() int
    }

    class ObjectiveControlVC {
      +check(Game) Player
    }

    class Game {
      -List~VictoryCondition~ victoryConditions
      +checkVictory() Player
    }

    VictoryCondition <|-- EliminationVC
    VictoryCondition <|-- BaseCaptureVC
    VictoryCondition <|-- ObjectiveControlVC
    Game o-- VictoryCondition
```

> **Nota**: `ObjectiveControlVC` está implementado en el código pero **no se instancia aún** en el `Game` del MVP. Es una extensión preparada para futuras versiones.

---

## 5. UI y navegación de pantallas

```mermaid
classDiagram
    class Screen {
      <<abstract>>
      +display()*
      +handleInput(String)*
      +applyGlobalStyle(Scene)
      +enforceUppercaseText(Parent)
      +initButtonSounds(Parent)
      +fadeInNode(Node, int)
      +fadeOutNode(Node, int)
    }

    class MainMenuScreen {
      -Stage window
      -Game game
      -StackPane settingsOverlay
      -CheckBox musicToggle
      -CheckBox sfxToggle
      +create(Stage, Game) MainMenuScreen$
      +handlePlayAction()
      +handleSettingsOpenAction()
      +handleSettingsCloseAction()
      +handleMusicToggleAction()
      +handleSfxToggleAction()
    }

    class RobotSelectionScreen {
      -Stage window
      -Game game
      -int currentIndex
      -List~RobotTemplate~ p1Team
      -List~RobotTemplate~ p2Team
      +create(Stage, Game) RobotSelectionScreen$
      +handlePrevRobotAction()
      +handleNextRobotAction()
      +handleConfirmAction()
      +handleSkipAction()
      +handleDismissTurnOverlay()
      +isSelectionComplete() boolean
      +transitionToCombatScreen()
    }

    class GameScreen {
      -Game game
      -Stage window
      -HexRenderer hexRenderer
      -Robot selectedRobot
      -SelectionMode selectionMode
      +create(Game) GameScreen$
      +startDeployPhase()
      +startCombatPhase()
      +executeMove(Hexagon)
      +executeAttack(Hexagon)
      +executeSkill(Hexagon)
      +handleSurrenderAction()
      +showOverlayMessage(String, String)
      +addLogMessage(String)
    }

    class VictoryScreen {
      -Player winner
      -Stage window
      -Game game
      +display()
      +getWinner() Player
    }

    class PauseScreen {
      +display()
      +handleInput(String)
    }

    class HexRenderer {
      -Pane hexMapPane
      +setOnHexClick(Consumer~Hexagon~)
      +setOnHexHover(Consumer~Hexagon~)
      +setOnHexHoverExit(Runnable)
      +render(Map)
      +setHighlightedHexes(Set~Hexagon~)
      +setMoveHighlightedHexes(Set~Hexagon~)
      +setAttackHighlightedHexes(Set~Hexagon~)
      +setSkillHighlightedHexes(Set~Hexagon~, boolean)
      +isMoveHighlighted(Hexagon) boolean
      +isAttackHighlighted(Hexagon) boolean
      +isSkillHighlighted(Hexagon) boolean
      +markAttack(Hexagon, Hexagon)
      +markAttack(Hexagon, Hexagon, boolean)
      +markLanding(Hexagon)
    }

    class RobotCardController {
      +setRobot(Robot)
      +setOnClick(Consumer~RobotTemplate~)
    }

    Screen <|-- MainMenuScreen
    Screen <|-- RobotSelectionScreen
    Screen <|-- GameScreen
    Screen <|-- VictoryScreen
    Screen <|-- PauseScreen

    Game o-- Screen
    GameScreen *-- HexRenderer
    GameScreen --> Game
    MainMenuScreen --> Game
    RobotSelectionScreen --> Game
    VictoryScreen --> Game
    RobotSelectionScreen ..> RobotCardController
```

> **Nota**: `PauseScreen` existe como stub en el código pero la lógica de pausa/rendición está integrada directamente en `GameScreen` mediante overlays FXML.

---

## 6. Bootstrap y utilidades

```mermaid
classDiagram
    class Launcher {
      +main(String[]) void$
    }

    class Main {
      +start(Stage)
      +main(String[])$
    }

    class SoundManager {
      <<utility>>
      -boolean enabled
      +play(String)$
      +setEnabled(boolean)$
      +attachButtonSounds(Button)$
      +attachButtonSoundsToAll(Parent)$
      +isEnabled() boolean$
    }

Launcher ..> Main
    Main ..> Game
    Screen ..> SoundManager
    Game ..> SoundManager
    GameScreen ..> SoundManager
```

---

## 7. IA, ejecución de partidas y telemetría (arquitectura objetivo)

> **Nota**: Este bloque describe la **extensión prevista** para `PvIA`, `IAvsIA` y simulación de balanceo. No corresponde todavía al código implementado en marzo de 2026.

```mermaid
classDiagram
    class PlayerController {
      <<interface>>
      +decideNextStep(MatchContext, Player) TurnDecision
    }

    class HumanController {
      +decideNextStep(MatchContext, Player) TurnDecision
    }

    class AiController {
      -AiStrategy strategy
      +decideNextStep(MatchContext, Player) TurnDecision
    }

    class AiStrategy {
      <<interface>>
      +chooseDecision(MatchContext, Player) TurnDecision
    }

    class EasyAiStrategy {
      -ActionEvaluator evaluator
      +chooseDecision(MatchContext, Player) TurnDecision
    }

    class ActionEvaluator {
      +scoreMove(MatchContext, Robot, Hexagon) int
      +scoreAttack(MatchContext, Robot, Robot) int
      +scoreSkill(MatchContext, Robot, Object) int
    }

    class TurnDecision {
      <<abstract>>
    }

    class ExecuteCommandDecision {
      -GameCommand command
      +getCommand() GameCommand
    }

    class EndTurnDecision

    class TurnEngine {
      +getLegalCommands(Game, Player, Robot) List~GameCommand~
      +execute(GameCommand)
      +endTurn(Game)
      +checkVictory(Game) Player
    }

    class MatchContext {
      -Game game
      -long seed
      -int maxTurns
      -boolean headless
      +getGame() Game
    }

    class MatchRunner {
      -TurnEngine turnEngine
      -List~GameEventListener~ listeners
      +runMatch(MatchContext, PlayerController, PlayerController) MatchResult
    }

    class MatchResult {
      -Player winner
      -String victoryType
      -int turnCount
      -long seed
    }

    class BatchSimulationService {
      -MatchRunner matchRunner
      +runBatch(BatchSimulationConfig) SimulationStats
    }

    class BatchSimulationConfig {
      -int matchCount
      -long seed
      -int maxTurns
    }

    class SimulationStats {
      +record(MatchResult)
      +exportCsv(Path)
      +exportJson(Path)
    }

    class GameEvent {
      <<abstract>>
      +getType() String
    }

    class TurnEndedEvent
    class AttackResolvedEvent
    class RobotDestroyedEvent
    class MatchFinishedEvent

    class GameEventListener {
      <<interface>>
      +onEvent(GameEvent)
    }

    class StatsCollector {
      +onEvent(GameEvent)
      +toSimulationStats() SimulationStats
    }

    PlayerController <|.. HumanController
    PlayerController <|.. AiController
    AiStrategy <|.. EasyAiStrategy
    TurnDecision <|-- ExecuteCommandDecision
    TurnDecision <|-- EndTurnDecision
    AiController o-- AiStrategy
    EasyAiStrategy o-- ActionEvaluator
    ExecuteCommandDecision o-- GameCommand
    MatchContext o-- Game
    MatchRunner o-- TurnEngine
    MatchRunner o-- MatchContext
    MatchRunner o-- MatchResult
    MatchRunner o-- GameEventListener
    BatchSimulationService o-- MatchRunner
    BatchSimulationService o-- BatchSimulationConfig
    BatchSimulationService o-- SimulationStats
    GameEvent <|-- TurnEndedEvent
    GameEvent <|-- AttackResolvedEvent
    GameEvent <|-- RobotDestroyedEvent
    GameEvent <|-- MatchFinishedEvent
    GameEventListener <|.. StatsCollector
    StatsCollector ..> SimulationStats
    TurnEngine ..> GameCommand
    TurnEngine ..> Game
    TurnEngine ..> Player
    TurnEngine ..> Robot
```

---

## 8. Enums de apoyo

| Enum            | Valores                                                                  | Uso principal                                  |
| --------------- | ------------------------------------------------------------------------ | ---------------------------------------------- |
| `GamePhase`     | `DEPLOYING`, `COMBAT`                                                    | Estado de fase en `Game`                       |
| `RobotTemplate` | `SABER_PRIME`, `BULLSEYE`, `BULWARK`, `SCOUT`, `DEATH_KNIGHT`, `ICE_AGE` | Fábrica de robots en selección                 |
| `TerrainType`   | `NORMAL`, `VEGETATION`, `WATER_SHALLOW`, `WATER_DEEP`                    | Tipo de casilla en `Hexagon`                   |
| `CoverType`     | `NONE`, `HALF`, `FULL`                                                   | Cobertura de casillas en resolución de ataque  |
| `AttackType`    | `ENERGY`, `EXPLOSIVE`, `KINETIC`                                         | Tipo de arma en `Weapon` → SFX                 |
| `SkillType`     | `MOVEMENT`, `ATTACK`                                                     | Tipo de skill → modo de resaltado en mapa      |
| `RobotState`    | `FROZEN`, `EXHAUSTED`                                                    | Estado activo del robot → `RobotStateBehavior` |

---

## Notas

- Se priorizan clases de dominio y relaciones relevantes de ejecución.
- Los métodos FXML (`@FXML`) están omitidos en los diagramas de UI para no saturarlos; solo se muestran los handlers de acciones relevantes.
- La sección 7 representa una **arquitectura objetivo** y no una fotografía exacta del código actual.
- Las convenciones de relación usadas:
  - `*--` composición (el hijo no vive sin el padre)
  - `o--` agregación (el hijo puede cambiar o existir independientemente)
  - `-->` dependencia de referencia directa
  - `..>` dependencia de uso puntual
