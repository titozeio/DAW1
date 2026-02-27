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
        +start()
        +nextTurn()
        +checkVictory() Player
    }

    class Map {
        -Hexagon[][] grid
        +getHexagon(int q, int r) Hexagon
        +getDistance(Hexagon a, Hexagon b) int
    }

    class Hexagon {
        -TerrainType terrain
        -int height
        -Robot occupant
        +getMovementCost(Robot r) int
        +getDamageModifier() float
        +isOccupied() boolean
    }

    class TerrainType
    TerrainType : NORMAL
    TerrainType : VEGETATION
    TerrainType : WATER

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
        -Weapon weapon
        -Skill skill
        -Player owner
        -Hexagon position
        -boolean usedMovement
        -boolean usedAttack
        +move(Hexagon target)
        +attack(Robot target)
        +useSkill(Object target)
        +takeDamage(int amount)
        +isDestroyed() boolean
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
    }

    class Skill {
        <<abstract>>
        -String name
        -SkillType type
        -int cooldown
        -int currentCooldown
        +execute(Robot actor, Object target)*
        +isReady() boolean
        +tickCooldown()
    }

    class AttackSkill {
        -int range
        -int damage
        -String specialEffect
        +execute(Robot actor, Object target)
    }

    class MovementSkill {
        -int movementPoints
        -String specialEffect
        +execute(Robot actor, Object target)
    }

    class SkillType
    SkillType : ATTACK
    SkillType : MOVEMENT

    %% Relationships
    Game "1" -- "1" Map : contains
    Game "1" -- "2" Player : manages
    Map "1" -- "*" Hexagon : composed of
    Player "1" -- "*" Robot : controls
    Robot "*" -- "0..1" Hexagon : stands on
    Robot "1" -- "1" Weapon : equipped with
    Robot "1" -- "1" Skill : possesses
    Skill <|-- AttackSkill
    Skill <|-- MovementSkill
    Skill "1" -- "1" SkillType : categorized as
    Hexagon "1" -- "1" TerrainType : has
    RobotTemplate ..> Robot : creates
```

## Notas de Diseño:

1. **Game**: Centraliza la lógica de turnos y victoria.
2. **Hexagon**: Gestiona el coste de movimiento y modificadores de daño según el terreno (`TerrainType`) y la altura.
3. **Robot**: Entidad principal. Según el GDD, cada robot tiene exactamente **1 arma** y **1 skill**. Se añade el campo `description` para el lore del robot.
4. **RobotTemplate**: Nueva enumeración que representa las **6 plantillas de robots** definidas en el GDD:
   - *Victory Saber* (HP 12, Mov 4, Sable de plasma)
   - *Bullseye* (HP 6, Mov 5, Misiles teledirigidos)
   - *Bulwark* (HP 15, Mov 4, Nanobots reparadores)
   - *Scout* (HP 7, Mov 5, Retropropulsores)
   - *Death Knight* (HP 13, Mov 3, Turbo propulsores)
   - *Ice Age* (HP 10, Mov 4, Rayo congelador)
   
   Actúa como fábrica (`Factory`) para instanciar objetos `Robot` con sus estadísticas predefinidas.
5. **Skill**: Se añaden `cooldown` y `currentCooldown` para gestionar la recarga entre turnos, y `specialEffect` en las subclases para describir los efectos especiales de cada skill (ignorar visibilidad, recuperar HP, inmovilizar, etc.).
6. **Weapon**: Se añade el campo `description` para el lore del arma.
7. **Corrección**: La relación `Skill <|-- SkillType` del diagrama anterior era **incorrecta** (implicaba que `SkillType` hereda de `Skill`). `SkillType` es una enumeración asociada a `Skill`, no una subclase. Corregido a `Skill "1" -- "1" SkillType`.
