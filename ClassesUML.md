# Diagrama de Clases UML - DAW1

A continuación se presenta el diagrama de clases para el prototipo **Devastation Ai Wars 1 (DAW1)**, actualizado según el GDD.

```mermaid
classDiagram
    class Game {
        -Map map
        -Player p1
        -Player p2
        -Player currentPlayer
        -int turnCounter
        -Screen currentScreen
        -List~VictoryCondition~ victoryConditions
        +start()
        +nextTurn()
        +checkVictory() Player
        +displayScreen(Screen screen)
        +handleInput(InputEvent event)
    }

    class Map {
        -Hexagon[][] grid
        -List~Objective~ objectives
        +getHexagon(int q, int r) Hexagon
        +getDistance(Hexagon a, Hexagon b) int
        +calculateVisibility(Hexagon origin, int range) List~Hexagon~
        +getCoverModifier(Hexagon attacker, Hexagon defender) float
    }

    class Hexagon {
        -TerrainType terrain
        -int height
        -Robot occupant
        -CoverType cover
        +getMovementCost(Robot r) int
        +getDamageModifier() float
        +isOccupied() boolean
        +hasCover() boolean
    }

    class TerrainType {
        <<enumeration>>
        NORMAL
        VEGETATION
        WATER_LEVEL_1
        WATER_LEVEL_2
    }

    class CoverType {
        <<enumeration>>
        NONE
        HALF
        FULL
    }

    class Player {
        -String name
        -List~Robot~ units
        -Hexagon baseLocation
        -int baseControlTurns
        +isEliminated() boolean
    }

    class Robot {
        -String modelName
        -String description
        -int maxHp
        -int currentHp
        -int movementPoints
        -int actionPoints
        -Weapon weapon
        -Skill skill
        -Player owner
        -Hexagon position
        -boolean usedMovement
        -boolean usedAttack
        -Set~RobotState~ activeStates
        +move(Hexagon target)
        +attack(Robot target)
        +useSkill(Object target)
        +takeDamage(int amount)
        +isDestroyed() boolean
        +applyState(RobotState state, int duration)
        +removeState(RobotState state)
        +hasState(RobotState state) boolean
    }

    class RobotState {
        <<enumeration>>
        FROZEN
        EXHAUSTED
        STUNNED
        BURNING
        // ... otros estados
    }

    class RobotTemplate {
        <<enumeration>>
        VICTORY_SABER
        BULLSEYE
        BULWARK
        SCOUT
        DEATH_KNIGHT
        ICE_AGE
        +createRobot() Robot
    }

    class Weapon {
        -String name
        -int range
        -int damage
        -String description
        -AttackType attackType
        -String specialEffect
    }

    class AttackType {
        <<enumeration>>
        PHYSICAL
        ENERGY
        EXPLOSIVE
    }

    class Skill {
        <<abstract>>
        -String name
        -SkillType type
        -int cooldown
        -int currentCooldown
        -String description
        +execute(Robot actor, Object target)*
        +isReady() boolean
        +tickCooldown()
        +resetCooldown()
    }

    class SkillType {
        <<enumeration>>
        ATTACK
        MOVEMENT
        SUPPORT
        UTILITY
    }

    class PlasmaSaberAttack {
        +execute(Robot actor, Object target)
    }

    class GuidedMissile {
        -boolean ignoresCover
        +execute(Robot actor, Object target)
    }

    class RepairNanobots {
        -int healAmount
        -int range
        +execute(Robot actor, Object target)
    }

    class JetpackBoost {
        -int extraMovement
        +execute(Robot actor, Object target)
    }

    class TurboPropulsion {
        -int extraMovement
        -int duration
        +execute(Robot actor, Object target)
    }

    class FreezeRay {
        -int range
        -int duration
        +execute(Robot actor, Object target)
    }

    class Screen {
        <<abstract>>
        +display()
        +handleInput(InputEvent event)
    }

    class MainMenuScreen {
        +display()
        +handleInput(InputEvent event)
    }

    class GameScreen {
        +display()
        +handleInput(InputEvent event)
    }

    class VictoryScreen {
        -Player winner
        +display()
        +handleInput(InputEvent event)
    }

    class PauseScreen {
        +display()
        +handleInput(InputEvent event)
    }

    class VictoryCondition {
        <<abstract>>
        +check(Game game) boolean
    }

    class EliminationVC {
        +check(Game game) boolean
    }

    class BaseCaptureVC {
        -int turnsToCapture
        +check(Game game) boolean
    }

    class ObjectiveControlVC {
        -Objective targetObjective
        -int turnsToControl
        +check(Game game) boolean
    }

    class Objective {
        -String name
        -Hexagon location
        -Player controllingPlayer
        -int controlTurns
        +isControlledBy(Player p) boolean
    }

    %% Relationships
    Game "1" -- "1" Map : contains
    Game "1" -- "2" Player : manages
    Game "1" -- "1" Screen : currentScreen
    Game "1" -- "*" VictoryCondition : has
    Map "1" -- "*" Hexagon : composed of
    Map "1" -- "*" Objective : contains
    Player "1" -- "*" Robot : controls
    Robot "*" -- "0..1" Hexagon : stands on
    Robot "1" -- "1" Weapon : equipped with
    Robot "1" -- "1" Skill : possesses
    Robot "1" -- "*" RobotState : has
    Hexagon "1" -- "1" TerrainType : has
    Hexagon "1" -- "1" CoverType : provides
    RobotTemplate ..> Robot : creates
    Skill <|-- PlasmaSaberAttack
    Skill <|-- GuidedMissile
    Skill <|-- RepairNanobots
    Skill <|-- JetpackBoost
    Skill <|-- TurboPropulsion
    Skill <|-- FreezeRay
    Skill "1" -- "1" SkillType : categorized as
    Weapon "1" -- "1" AttackType : uses
    Screen <|-- MainMenuScreen
    Screen <|-- GameScreen
    Screen <|-- VictoryScreen
    Screen <|-- PauseScreen
    VictoryCondition <|-- EliminationVC
    VictoryCondition <|-- BaseCaptureVC
    VictoryCondition <|-- ObjectiveControlVC
    Objective "1" -- "1" Hexagon : located at
    Objective "0..1" -- "1" Player : controlled by
```

## Notas de Diseño:

1.  **Game**: Centraliza la lógica de turnos, victoria y gestión de pantallas. Ahora incluye una referencia a la `currentScreen` y una lista de `VictoryCondition` para permitir múltiples condiciones de victoria.
2.  **Map**: Gestiona la cuadrícula de hexágonos, los objetivos del mapa y añade métodos para calcular visibilidad y modificadores de cobertura.
3.  **Hexagon**: Incluye `height` y `cover` (`CoverType`) para cálculos de visibilidad y daño. `TerrainType` ahora distingue niveles de agua.
4.  **TerrainType**: Enumeración actualizada para incluir `WATER_LEVEL_1` y `WATER_LEVEL_2` según el GDD.
5.  **CoverType**: Nueva enumeración para definir los tipos de cobertura que un hexágono puede ofrecer (`NONE`, `HALF`, `FULL`).
6.  **Player**: Sin cambios significativos, sigue gestionando las unidades y la base.
7.  **Robot**: Entidad principal. Se añade `actionPoints` para acciones secundarias, y un `Set<RobotState>` para gestionar los estados activos (como `FROZEN`, `EXHAUSTED`). Métodos para aplicar y remover estados.
8.  **RobotState**: Nueva enumeración para representar los estados que un robot puede tener.
9.  **RobotTemplate**: Enumeración que actúa como fábrica para instanciar objetos `Robot` con sus estadísticas predefinidas.
10. **Weapon**: Se añade `attackType` (físico, energía, explosivo) y `specialEffect` para mayor detalle.
11. **AttackType**: Nueva enumeración para clasificar los tipos de ataque de las armas.
12. **Skill**: Clase abstracta con `cooldown`, `currentCooldown`, `description`. Se añaden subclases concretas para representar las habilidades de los robots del GDD, con atributos específicos para cada una.
    *   `PlasmaSaberAttack` (Victory Saber)
    *   `GuidedMissile` (Bullseye)
    *   `RepairNanobots` (Bulwark)
    *   `JetpackBoost` (Scout)
    *   `TurboPropulsion` (Death Knight)
    *   `FreezeRay` (Ice Age)
13. **SkillType**: Enumeración para categorizar las habilidades (ataque, movimiento, soporte, utilidad).
14. **Screen**: Clase abstracta para la interfaz de usuario.
15. **MainMenuScreen, GameScreen, VictoryScreen, PauseScreen**: Clases concretas que implementan la interfaz de usuario para las diferentes fases del juego.
16. **VictoryCondition**: Clase abstracta para definir las condiciones de victoria.
17. **EliminationVC, BaseCaptureVC, ObjectiveControlVC**: Clases concretas que implementan las condiciones de victoria. `BaseCaptureVC` y `ObjectiveControlVC` incluyen `turnsToCapture`/`turnsToControl`.
18. **Objective**: Nueva clase para representar objetivos en el mapa que pueden ser capturados o controlados por los jugadores.

**Relaciones Adicionales:**
*   `Game` ahora tiene una relación con `Screen` y `VictoryCondition`.
*   `Map` tiene una relación con `Objective`.
*   `Hexagon` tiene una relación con `CoverType`.
*   `Robot` tiene una relación con `RobotState`.
*   `Weapon` tiene una relación con `AttackType`.
*   `Objective` tiene relaciones con `Hexagon` y `Player`.
