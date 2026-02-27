# Diagrama de Casos de Uso - DAW1

Diagrama de casos de uso principal del juego **Devastation Ai Wars 1 (DAW1)**, basado en el GDD.

> **Actores**: El único actor humano del sistema es el **Jugador** (J1 y J2 tienen el mismo rol, por lo que se unifican en un único actor). El **Sistema** actúa como actor secundario en los casos de uso de resolución automática.

```mermaid
graph TD
    Actor["👤 Jugador"]

    subgraph UC_INICIO["🖥️ Pantalla de Inicio"]
        UC1["Iniciar juego"]
    end

    subgraph UC_ELECCION["🤖 Pantalla de Elección de Robots"]
        UC2["Ver información de un robot"]
        UC3["Seleccionar robot"]
    end

    subgraph UC_COMBATE["⚔️ Pantalla de Combate"]
        subgraph UC_DESPLIEGUE["📍 Fase de Despliegue"]
            UC4["Posicionar robot en casilla de despliegue"]
        end

        subgraph UC_TURNO["🔄 Durante el Turno"]
            UC5["Seleccionar robot propio"]
            UC6["Mover robot"]
            UC7["Atacar con robot"]
            UC8["Usar skill del robot"]
            UC9["Cancelar acción en curso"]
            UC10["Finalizar turno"]
        end

        subgraph UC_INFO["🔍 Consulta de información"]
            UC11["Ver información de robot (hover)"]
            UC12["Ver información de casilla (hover)"]
        end

        subgraph UC_PAUSA["⏸️ Pausa"]
            UC13["Pausar partida"]
            UC14["Reanudar partida"]
            UC15["Rendirse"]
        end
    end

    subgraph UC_SISTEMA["⚙️ Sistema (automático)"]
        UC16["Calcular casillas de movimiento accesibles"]
        UC17["Calcular casillas de ataque disponibles"]
        UC18["Resolver daño del ataque"]
        UC19["Comprobar condiciones de victoria"]
        UC20["Cambiar turno al siguiente jugador"]
    end

    %% Conexiones Actor → Casos de uso
    Actor --> UC1
    Actor --> UC2
    Actor --> UC3
    Actor --> UC4
    Actor --> UC5
    Actor --> UC6
    Actor --> UC7
    Actor --> UC8
    Actor --> UC9
    Actor --> UC10
    Actor --> UC11
    Actor --> UC12
    Actor --> UC13
    Actor --> UC14
    Actor --> UC15

    %% Flujo entre pantallas
    UC1 -->|"inicia"| UC_ELECCION
    UC3 -->|"ambos J tienen 3 robots"| UC_COMBATE

    %% Dependencias de acciones durante el turno
    UC5 -->|"incluye"| UC16
    UC5 -->|"incluye"| UC17
    UC6 -->|"incluye"| UC16
    UC7 -->|"incluye"| UC17
    UC7 -->|"incluye"| UC18
    UC8 -->|"incluye"| UC16
    UC8 -->|"incluye"| UC17
    UC8 -->|"incluye"| UC18

    %% Post-acción del sistema
    UC18 -->|"tras resolver daño"| UC19
    UC10 -->|"tras fin de turno"| UC19
    UC19 -->|"si no hay victoria"| UC20
    UC15 -->|"declara victoria rival"| UC19
```
