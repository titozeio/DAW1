# UML de Secuencia (Flujos Clave)

Este documento recoge los flujos más importantes del juego en diagramas de secuencia Mermaid.

## 1) Arranque y navegación inicial

```mermaid
sequenceDiagram
    actor User as Jugador
    participant Launcher
    participant Main
    participant Game
    participant MainMenu as MainMenuScreen

    User->>Launcher: Ejecuta app
    Launcher->>Main: main(args)
    Main->>Game: new Game(stage)
    Main->>Game: start()
    Game->>MainMenu: create(stage, game)
    Game->>MainMenu: displayScreen(mainMenu)
    MainMenu-->>User: Menú principal visible
```

## 2) Selección de robots y paso a combate

```mermaid
sequenceDiagram
    actor User as Jugador
    participant MainMenu as MainMenuScreen
    participant Selection as RobotSelectionScreen
    participant Game
    participant Player
    participant Template as RobotTemplate
    participant GameScreen

    User->>MainMenu: Click "Jugar"
    MainMenu->>Selection: create(stage, game)
    Game->>Selection: displayScreen(selection)

    loop Por cada robot elegido
        User->>Selection: Selecciona plantilla
        Selection->>Template: createRobot(owner)
        Template-->>Selection: Robot
        Selection->>Player: addRobot(robot)
    end

    User->>Selection: Confirmar selección
    Selection->>GameScreen: create(game)
    Game->>GameScreen: displayScreen(gameScreen)
```

## 3) Despliegue en mapa (fase DEPLOYING)

```mermaid
sequenceDiagram
    actor User as Jugador
    participant GS as GameScreen
    participant HR as HexRenderer
    participant G as Game
    participant H as Hexagon
    participant R as Robot

    GS->>HR: highlightDeployZones()
    User->>HR: Click en hex
    HR->>GS: onHexClicked(hex)
    GS->>GS: handleDeployClick(hex)

    alt Casilla válida y libre
        GS->>R: robot = robotsToPlace.poll()
        GS->>H: setOccupant(robot)
        GS->>R: setPosition(hex)
        GS->>HR: markLanding(hex)
        GS->>HR: render(map)
    else Casilla no válida/ocupada
        GS-->>User: Mensaje en log
    end

    alt Termina despliegue P1
        GS-->>User: Overlay turno despliegue P2
    else Termina despliegue P2
        GS->>G: setPhase(COMBAT)
        GS-->>User: Overlay "¡Combate iniciado!"
    end
```

## 4) Selección de robot y movimiento

```mermaid
sequenceDiagram
    actor User as Jugador
    participant HR as HexRenderer
    participant GS as GameScreen
    participant MC as MovementCalculator
    participant AM as ActionManager
    participant CMD as MoveCommand
    participant R as Robot

    User->>HR: Click en robot aliado
    HR->>GS: onHexClicked(hexOcupado)
    GS->>GS: selectRobot(robot)
    GS->>MC: getReachable(robot, map)
    MC-->>GS: Set<Hexagon>
    GS->>HR: setMoveHighlightedHexes(reachable)
    GS->>HR: render(map)

    User->>HR: Click en hex resaltado
    HR->>GS: onHexClicked(hexDestino)
    GS->>CMD: new MoveCommand(selectedRobot, hexDestino)
    GS->>AM: executeCommand(cmd)
    AM->>CMD: execute()
    CMD->>R: move(hexDestino)
    GS->>HR: render(map)
```

## 5) Ataque normal

```mermaid
sequenceDiagram
    actor User as Jugador
    participant HR as HexRenderer
    participant GS as GameScreen
    participant AM as ActionManager
    participant ACMD as AttackCommand
    participant AR as Robot
    participant DR as Robot
    participant M as Map

    User->>GS: Cambia a modo ATTACK (ciclo)
    GS->>GS: getAttackableHexes(selectedRobot)
    GS->>HR: setAttackHighlightedHexes(targets)

    User->>HR: Click en objetivo enemigo
    HR->>GS: onHexClicked(targetHex)
    GS->>HR: markAttack(attackerHex, targetHex)
    GS->>ACMD: new AttackCommand(attacker, defender, map)
    GS->>AM: executeCommand(attackCmd)
    AM->>ACMD: execute()
    ACMD->>AR: attack(defender, map)
    AR->>M: getDistance/hasVisibility/getCoverModifier
    AR->>DR: takeDamage(damage)
    GS->>GS: checkAndShowVictory("eliminación")
```

## 6) Uso de skill (ataque o movimiento)

```mermaid
sequenceDiagram
    actor User as Jugador
    participant GS as GameScreen
    participant HR as HexRenderer
    participant AM as ActionManager
    participant SCMD as SkillCommand
    participant A as Robot
    participant S as Skill
    participant T as Robot
    participant HX as Hexagon

    User->>GS: Entra en modo SKILL
    GS->>GS: getSkillAttackTargets()/getSkillMovementTargets()
    GS->>HR: setSkillHighlightedHexes(targets, isAttackSkill)

    User->>HR: Click en objetivo/hex
    HR->>GS: onHexClicked(hex)

    alt Skill de ataque
        GS->>HR: markAttack(actorHex, targetHex, isHeal)
        GS->>SCMD: new SkillCommand(actor, targetRobot)
        GS->>AM: executeCommand(skillCmd)
        AM->>SCMD: execute()
        SCMD->>A: useSkill(targetRobot)
        A->>S: execute(actor, targetRobot)
    else Skill de movimiento
        GS->>SCMD: new SkillCommand(actor, targetHex)
        GS->>AM: executeCommand(skillCmd)
        AM->>SCMD: execute()
        SCMD->>A: useSkill(targetHex)
        A->>S: execute(actor, targetHex)
    end

    GS->>HR: render(map)
    GS->>GS: checkAndShowVictory("eliminación")
```

## 7) Fin de turno y condiciones de victoria

```mermaid
sequenceDiagram
    actor User as Jugador
    participant GS as GameScreen
    participant G as Game
    participant VC as VictoryCondition
    participant R as Robot

    User->>GS: Click "FINALIZAR TURNO"
    GS->>G: nextTurn()
    loop Cada robot del jugador actual
        GS->>R: resetActions()
    end
    GS->>G: checkVictory()
    loop Por cada condición
        G->>VC: check(game)
        VC-->>G: winner|null
    end

    alt Hay ganador
        GS-->>User: Overlay de victoria
    else No hay ganador
        GS-->>User: Continúa turno siguiente
    end
```

## 8) Hover de info de hex en movimiento/skill de movimiento

```mermaid
sequenceDiagram
    actor User as Jugador
    participant HR as HexRenderer
    participant GS as GameScreen

    User->>HR: MouseEnter sobre hex
    HR->>GS: onHexHovered(hex)
    GS->>GS: shouldPreviewHexOnHover()
    alt selectionMode MOVE o SKILL(MOVEMENT)
        GS->>GS: showTerrainInfo(hex)
    else Otro modo
        GS-->>GS: Ignora hover
    end

    User->>HR: MouseExit del hex/mapa
    HR->>GS: onHexHoverExited()
    GS->>GS: restoreInfoPanelAfterHover()

    Note over GS: También se restaura al ejecutar move/skill\ny al limpiar selección.
```
