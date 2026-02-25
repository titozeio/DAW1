# Diagrama de Clases UML - DAW1

A continuación se presenta una propuesta inicial del diagrama de clases para el prototipo **Devastation Ai Wars 1 (DAW1)**, basado en los requerimientos del GDD.

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
        -int maxHp
        -int currentHp
        -int movementPoints
        -List~Weapon~ weapons
        -List~Skill~ skills
        -Player owner
        -Hexagon position
        -boolean usedMovement
        -boolean usedAttack
        +move(Hexagon target)
        +attack(Robot target, Weapon weapon)
        +useSkill(Skill skill, Object target)
        +takeDamage(int amount)
    }

    class Weapon {
        -int range
        -int damage
        -String name
    }

    class Skill {
        <<abstract>>
        -String name
        -SkillType type
        +execute(Robot actor, Object target)*
    }

    class AttackSkill {
        -int range
        -int damage
        +execute(Robot actor, Object target)
    }

    class MovementSkill {
        -int specificMovementPoints
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
    Robot "1" -- "*" Weapon : equipped with
    Robot "1" -- "*" Skill : possesses
    Skill <|-- SkillType
    Hexagon "1" -- "1" TerrainType : has
    Skill "1" -- "1" SkillType : categorized as
```

## Notas de Diseño:

1.  **Game**: Centraliza la lógica de turnos y victoria.
2.  **Hexagon**: Gestiona el coste de movimiento y modificadores de daño según el terreno (`TerrainType`) y la altura.
3.  **Robot**: Es la entidad principal. Mantiene el estado de sus acciones (movimiento/ataque) por turno.
4.  **Skills**: Se utiliza herencia para diferenciar habilidades de ataque y movimiento, permitiendo extenderlas fácilmente para efectos especiales (OMSI).
5.  **Multiplicidad**: Un `Hexagon` puede estar ocupado por un `Robot` o ninguno.
