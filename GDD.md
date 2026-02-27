# Devastation Ai Wars 1 (DAW1) GDD

- [Devastation Ai Wars 1 (DAW1) GDD](#devastation-ai-wars-1-daw1-gdd)
- [1. Concepto de Alto Nivel (The Pitch)](#1-concepto-de-alto-nivel-the-pitch)
  - [Concepto:](#concepto)
  - [Narrativa:](#narrativa)
  - [MVP:](#mvp)
- [2. Mecánicas de Juego (Gameplay)](#2-mecánicas-de-juego-gameplay)
  - [Objetivo del juego:](#objetivo-del-juego)
  - [Mapas:](#mapas)
  - [Core Loop:](#core-loop)
  - [Robots:](#robots)
  - [Turnos:](#turnos)
  - [Skills:](#skills)
  - [Alcance:](#alcance)
  - [Visibilidad:](#visibilidad)
  - [Resolución de ataque:](#resolución-de-ataque)
  - [Casillas del mapa:](#casillas-del-mapa)
- [3. Aspectos Técnicos (Stack Tecnológico)](#3-aspectos-técnicos-stack-tecnológico)
  - [Dependencias:](#dependencias)
  - [Arquitectura:](#arquitectura)
  - [Estructura del Proyecto:](#estructura-del-proyecto)
- [4. Bucle de Juego (Game Loop)](#4-bucle-de-juego-game-loop)
    - [Input:](#input)
    - [Update:](#update)
    - [Render:](#render)
- [5. Contenido, Niveles](#5-contenido-niveles)
  - [Asset List:](#asset-list)
  - [Diseño de Nivel (mapa):](#diseño-de-nivel-mapa)
  - [Lista de skills:](#lista-de-skills)
  - [Lista de Robots:](#lista-de-robots)
- [6. Interfaz (UI/HUD)](#6-interfaz-uihud)
  - [Menús:](#menús)
  - [HUD:](#hud)


# 1. Concepto de Alto Nivel (The Pitch)

## Concepto:

Videojuego en java (maven) de estrategia por turnos en la que un equipo de robots gigantes se enfrenta a otro en un tablero.


## Narrativa:

En el futuro la humanidad ya no resuelve los conflictos enviando soldados, tanques, aviones..a la guerra… envía a robots gigantes controlados por IA. 

Enfrentaos en una batalla por el campo de batalla asumiendo el rol de la IA que controla estos robots gigantescos.

## MVP:

Esta versión inicial del GDD describe los elementos básicos para un MVP. También se darán apuntes sobre posibles mejoras para versiones futuras indicadas con el tag [OMSI] (Out of MVP Scope Improvement) .

# 2. Mecánicas de Juego (Gameplay)

## Objetivo del juego:

El juego es una batalla PvP por turnos (P1=Player1, P2=Player2) enfrentándose en un tablero hexagonal (mapa). El objetivo de ambos es conseguir cumplir una de las condiciones de victoria del mapa en el que estén jugando.

## Mapas:

Para el MVP vamos a tener un solo mapa hexagonal con dos condiciones de victoria:

- Eliminar a todos los robots controlados por el rival.

- Conquistar la base del rival (posicionar uno de los robots del jugador en la base y conseguir que sobreviva 2 turnos posicionado en la base).

Más adelante se explica cómo gestionar el mapa, cómo se visualiza y los elementos que puede tener.

## Core Loop:

El Core Loop básico sería:

1. P1 realiza su turno. Se comprueba si se cumplen las condiciones de victoria. Si se cumple alguna, se acaba el juego y gana el jugador que cumpla las condiciones. Si no, se pasa al paso 2.

2. P2 realiza su turno. Se comprueba si se cumplen las condiciones de victoria.  Si se cumple alguna, se acaba el juego y gana el jugador que cumpla las condiciones. Si no, se vuelve al paso 1.

**[OMSI]** : Habilitar condiciones de empate para los mapas (por ejemplo, poner un límite de  turnos).

## Robots:

Todos los robots tienen los siguientes parámetros:

- **Modelo**: El nombre del modelo del robot.

- **Descripción**: Un poco de lore sobre el robot.

- **Movimiento**: indica los puntos de movimiento.

- **Armas**:

    - **[OMSI]** Tipo: pueden ser de energía / explosivas / cinéticas

    - Alcance: distancia en hexágonos a la que llegan las armas.

    - Daño: Hit Points (HP) que pierde el enemigo al recibir un impacto.
      
    - Descripción: Un poco de "lore" sobre las armas del robot.

- **HP**: Los Hit Points (Puntos de impacto) que puede recibir un robot. Cada punto de daño que recibe el robot se resta de sus HPs. Al llegar a 0, queda destruido. Los HPS de un robot NUNCA pueden ser superiores a los originales.

- **Skills**: Habilidades o ataques especiales. Pueden ser de tipo movimiento o ataque, y tienen un efecto distinto con sus propias reglas. 

- **[OMSI] Protección**:

    - Puede ser  de **tipo**: escudo energía / blindaje /  cinético

    - **DR **(Damage Reduction): La cantidad de daño que protege.

## Turnos:

En cada turno, los jugadores usan todas las acciones de sus robots, 1 por 1, en el orden que quieran, pero una vez que empiezan con uno, tienen que agotar sus acciones antes de pasar al siguiente.

En un turno, los robots pueden cualquiera de estas acciones, en cualquier orden, pero solo 1 vez cada una (excepción: cuando una habilidad especial lo especifique):

- Moverse: Cada robot puede desplazarse (como máximo) un número de casillas igual a sus puntos de movimiento sobre terreno estándar. Algunas casillas aplican un coste adicional al entrar en ellas (normalmente, +1 pto de movimiento de coste).

- Atacar: Siempre que un robot tenga un enemigo al alcance (determinado por el alcance de sus armas), podrá realizar un ataque contra un enemigo.

## Skills:

Todos los robots tienen skills. En cada skill se especifica si es de movimiento o ataque. El jugador puede decidir sustituir la acción de movimiento o la de ataque por el uso de una  skill del mismo tipo (por ej. si un robot tiene una skill de movimiento, el jugador puede decidir usar la skill y atacar, en lugar de mover el robot y atacar).

- **Movimiento**: Las skills de movimiento deben especificar (como mínimo) los puntos de movimiento que se pueden invertir.

- **Ataque**: las skills de ataque deben especificar (como mínimo) los mismos parámetros que un arma.

- **Efecto especial**: Si la skill tiene un efecto especial que contradice una regla establecida del juego, el efecto predomina sobre la regla. Por ejemplo, el efecto puede decir que no se tenga en cuenta la cobertura o el tipo de terreno a la hora de determinar el coste en puntos de movimiento.

- **Cooldown**: Indica cuántos turnos deben transcurrir antes de poder usar la skill de nuevo. 

- **[OMSI]** Incluir la posibilidad de que existan skills que no sean de movimiento ni de ataque, sino que sean, por ejemplo, pasivas, o reactivas (no se activan voluntariamente sino cuando ocurre algo determinado).



## Alcance:

Se considera que el enemigo está al alcance, si el número de hexágonos de distancia entre el atacante y el objetivo (sin contar ambos) es igual o menor al alcance de las armas o skill de ataque del atacante.

## Visibilidad:

Se considera que el robot tiene visibilidad sobre un objetivo, si entre ellos no hay casillas de más de 1 altura superior a ninguno de ellos.

## Resolución de ataque:

Un robot solo puede atacar a otro cuando tiene visibilidad sobre él y  lo tiene al alcance. Si se cumplen ambas condiciones y se realiza el ataque, le produce un daño al objetivo igual al especificado por sus armas o skill de ataque. Es decir, resta el daño de los HPs restantes del robot enemigo.

Si los HPs del robot enemigo quedan a 0 o menos, es destruido.

**[OMSI]** Incorporar la regla de desactivación: Si el robot queda exactamente a 0 HPs es desactivado. No se puede usar durante el siguiente turno. Después de un turno se reactiva y puede usarse con 0 HPs, pero cualquier mínimo impacto lo destruirá.

Ciertas casillas de terreno pueden afectar al daño recibido.

**[OMSI]** Incorporar un triángulo piedra/papel/tijera entre los tipos de ataque y los tipos de blindaje que que modifiquen también el daño.

Una vez resueltos todos los modificadores, el daño se redondea. El daño mínimo, una vez resueltos todos los modificadores, nunca puede ser menor de 0.

## Casillas del mapa:

Cada casilla del mapa tiene:

- Tipo de terreno (normal, vegetación).

- Nivel de altura: Por defecto, las casillas tienen altura 0. Los robots tienen 2 de altura. Así, , por ejemplo, un terreno de altura 1 le llegaría a un robot posicionado en una casilla nivel 0 por la mitad. Otro ej: Una casilla de altura 4 cubriría justo por completo a un robot en una casilla de altura 2.

Efectos del terreno según el tipo y la altura:

- **Por Tipos:**

- **Normal**: Ningún tipo de efecto.

- **Vegetación** espesa: 

    - Los robots en casillas de este tipo reciben un 25% de reducción de daño.

    - Coste de movimiento +1.

- **Acuático**: Depende del nivel:

    - Nivel -1 (profundidad 1):

        - Overheat: Incrementa el daño que hacen las armas de los robots en estas casillas en un 25% (al estar sumergidos, el agua enfría y pueden sobrecalentar las armas aplicando más potencia).

        - Incrementa el daño de los robots en este terreno en un 25% (al estar sumergidos hasta la mitad, todos los impactos van a la cabeza u hombros, partes más vitales del robot).

        - Coste de movimiento: +1.

    - Nivel -2 (profundidad 2):

        - Los robots en estas casillas no pueden atacar ni ser atacados.

        - Coste de movimiento: +2.

- **Por nivel:**

    - Terreno **+1 nivel** (cuando se pasa de un nivel terrestre a otro superior en 1):

        - Coste de movimiento:+1 (acumulativo con terreno de vegetación espesa si la hubiera).

    - Terreno **+2 niveles**: Un robot no puede pasar de una casilla a otra contigua dos niveles superior a la que está (a menos que use una Skill que especifique lo contrario).

    - Terreno **-1 nivel**  (cuando se pasa de un nivel terrestre  a otro inferior en 1):

        - Coste de movimiento:+1 (acumulativo con terreno de vegetación espesa si la hubiera).

    - Terreno **-2 niveles (o más)**: Un robot puede saltar desde un terreno a otro de nivel inferior en 2 niveles o más:

        - Coste de movimiento:+1 (NO acumulativo con terreno de vegetación espesa si la hubiera).

        - Daño de caída: 1 HP.

        - Una Skill de movimiento podría anular el coste adicional e incluso el daño.

- **Cobertura**: En un ataque, si entre el robot atacante y el defensor (siguiendo el camino de hexágonos más corto), hay alguna casilla de terreno de 1 nivel superior a la casilla del  atacante y a la del defensor, se considera que el defensor tiene **cobertura media** (el daño se reduce en un 50%). Si hay alguna casilla de 2 niveles superior, entonces se considera **cobertura total** (el atacante no puede atacar a ese defensor desde esa posición).

# 3. Aspectos Técnicos (Stack Tecnológico)

Cómo usar Maven y Java, esta sección es crítica para el entorno educativo.

## Dependencias:

Librerías a usar (ej: LWJGL, LibGDX o JavaFX).

## Arquitectura:

Breve mención a si usar un patrón Entity Component System (ECS) o herencia simple.

## Estructura del Proyecto:

Definición de paquetes (com.game.entities, com.game.engine, etc.).

# 4. Bucle de Juego (Game Loop)

### Input:

Captura de eventos.

### Update:

Lógica y cálculo de estados.

### Render:

Dibujado en pantalla.

# 5. Contenido, Niveles

## Asset List:

Lista de sprites, sonidos y música (importante para organizar la carpeta src/main/resources).

## Diseño de Nivel (mapa):

Elementos que puede haber en un mapa.

Un boceto o descripción del único mapa que tendrá el prototipo.

## Lista de skills:

- **Retropropulsores**:
    - Tipo: Movimiento.
    - Movimiento: 3.
    - Cooldown: 1 turnos.
    - Efecto especial: Ignora el tipo de terreno a efectos de movimiento (coste y posibilidad). Por ejemplo, un robot con esta skill podría moverse de una casilla de altura 0 a una casilla de altura 2.
    - Descripción: El robot cuenta con unos retropropulsores que le impulsan por el aire. No llega a poder volar, ya que el impulso dura muy poco dado el coste energético. 

- **Turbo Propulsores**:
    - Tipo: Movimiento.
    - Movimiento: 7.
    - Cooldown: 1 turnos.
    - Efecto especial: Ninguno.
    - Descripción: Cada dos turnos, el robot puede activar unos turbo propulsores en sus patas, que le impulsan a una velocidad mucho mayor de su movimiento normal.
 
- **Misiles teledirigidos**:
    - Tipo: Ataque.
    - Alcance: 5.
    - Daño: 5.
    - Cooldown: 2 turnos.
    - Efecto especial: Permite atacar sin visibilidad e ignorando reglas de cobertura, siempre que el enemigo esté al alcance.
    - Descripción: El robot cuenta con unos misiles teledirigidos que viajan hacia el objetivo rodeando obstáculos, permitiendo ignorar todo tipo de cobertura y visibilidad.

- **Nanobots reparadores**:
    - Tipo: Ataque.
    - Alcance: 1.
    - Daño: 5.
    - Cooldown: 2 turnos.
    - Efecto especial: Hace que el robot objetivo recupere una cantidad de HPs igual a la cantidad de daño especificada (no se puede usar sobre robots ya destruidos).
    - Descripción: Aunque parezca un ataque, esta skill despliega un enjambre de nanobbots sobre un robot aliado. Los nanobots actúan rápidamente, restaurando las partes dañadas del robot aliado.

- **Sable de Plasma**:
    - Tipo: Ataque.
    - Alcance: 1.
    - Daño: 10.
    - Cooldown: 3 turnos.
    - Descripción: El robot despliega un sable de plasma que hace daño masivo. El sable es tan potente que necesita una cantidad de  energía brutal para volver a generarse, por lo que normalmente solo se puede usar 1 o 2 veces en el combate.

- **Rayo congelador**:
    - Tipo: Ataque.
    - Alcance: 3.
    - Daño: 3.
    - Cooldown: 2 turnos.
    - Efecto especial: El robot objetivo no puede actuar durante 1 turno.
    - Descripción: El robot dispara un rayo de frío criogénico que congela al robot objetivo, impidiendo que pueda actuar durante 1 turno.

- **[OMSI]**
    - **Carga Kamikaze**:
        - Tipo: Movimiento.
        - Movimiento: 10.
        - Cooldown: 2 turnos.
        - Efecto especial: Permite avanzar hasta 10 casillas, en línea recta. Si durante el trayecto, hay algún obstáculo (como una casilla de un nivel mayor que el anterior, agua u otro robot), el robot termina su desplazamiento en esa casilla. Si en esa casilla había un robot, ese robot es desplazado una casilla en dirección contraria al movimiento del robot, y recibe 2 puntos de daño por cada casilla que se haya desplazado el robot con la skill. El robot atacante también recibe daño: 1 por cada casilla que se haya desplazado.
        - Descripción: Las patas del robot cuentan con microretropropulsores, que, una vez activados, impulsan al robot hacia delante a una velocidad increíble, permitiendo recorrer una distancia de 10, pudiendo embestir al enemigo y produciendo daño proporcional a la distancia recorrida, aunque también recibe daño por cada casilla que se haya desplazado.
    - **Nanobots corrosivos**:
        - Tipo: Ataque.
        - Alcance: 3.
        - Daño: 3.
        - Cooldown: 2 turnos.
        - Efecto especial: Además del daño del ataque, el robot objetivo recibe 2 HPs de daño por cada turno que pase, durante 4 turnos.
        - Descripción: El robot dispara un proyectil, que al impactar, despliega unos nanobots corrosivos que van destruyendo el robot objetivo y causando daño constante hasta que se quedan sin energía o el robot objetivo se destruye.  
## Lista de Robots:

- **Victory Saber**:
    - **Modelo**: Victory Saber.
    - **Descripción**: Un robot de otra época, con aspecto bastante humanoide, anterior a las DAW1, pensado para ser pilotado por un humano y reconvertido a uso por la IA. Suele ser subestimado, pero cuando es capaz de cargar su temible sable de plasma, se convierte en una máquina de matar.
    - **HP**: 12.
    - **Movimiento**: 4.
    - **Armas**:
        - Nombre: Cañón de plasma.
        - Alcance: 3.  
        - Daño: 3.
        - Descripción: Cañón de plasma. Poco efectivo y de corto alcance.
     
    - **Skills**: Sable de plasma.
  
  - **Bullseye**:
    - **Modelo**: Bullseye.
    - **Descripción**: También conocido en el terreno de combate como "Glass Cannon" (Cañón de cristal), es un robot muy rápido y con un gran alcance de ataque, además de una puntería infalible cuando carga sus misiles, pero con muy poco blindaje.
    - **HP**: 6.
    - **Movimiento**: 5.
    - **Armas**:
        - Nombre: Doble cañón automático.
        - Alcance: 4.  
        - Daño: 5.
        - Descripción: En lugar de brazos, Bullseye cuenta con dos cañones automáticos que hacen bastante daño a un alcance importante.
    - **Skills**: Misiles teledirigidos.
  
  - **Bulwark**:
    - **Modelo**: Bulwark.
    - **Descripción**: Bulwark, tambi;en conocido como DOC en el terreno de combate, es un robot muy resistente, no hace mucho daño y es lento, pero su capacidad para recuperar robots aliados lo convierte en una pieza clave del equipo.
    - **HP**: 15.
    - **Movimiento**: 3.
    - **Armas**:
        - Nombre: Cañón laser.
        - Alcance: 2.  
        - Daño: 3.
        - Descripción: Cañón laser montado en el hombro derecho. Poco efectivo y de corto alcance.    
    - **Skills**: Nanobots reparadores.

  - **Scout**:
    - **Modelo**: Scout.
    - **Descripción**: Un robot pequeño y ágil, diseñado para explorar el terreno y detectar enemigos. No tiene mucho blindaje, pero su movilidad, gracias a su velocidad y a los retropropulsores en su espalda, y su temible cañón automático, lo convierten en una pieza clave del equipo.
    - **HP**: 8.
    - **Movimiento**: 4.
    - **Armas**:
        - Nombre: Cañón automático.
        - Alcance: 5.  
        - Daño: 5.
        - Descripción: Su brazo derecho es un cañón automático de gran alcance y potencia.  
    - **Skills**: Retropropulsores.

  - **Death Knight**:
    - **Modelo**: Death Knight.
    - **Descripción**: Su color negro de camuflaje nocturno, combinado con su aspecto de mole con un martillo gigante, le hacen valedor de su apelativo. Es un robot lento, pero muy resistente y con un gran ataque cuerpo a cuerpo. No conviene subestimarlo, ya que a pesar de su lentitud, cuando activa sus retropropulsores, puede desplazarse rápidamente por el campo de batalla. 
    - **HP**: 12.
    - **Movimiento**: 3.
    - **Armas**:
        - Nombre: Martillo propulsado.
        - Alcance: 1.  
        - Daño: 8.
        - Descripción: Porta un martillo gigante a dos manos, que le impide llevar ningún otro arma. El martillo tiene retropropulsores que le permiten golpear con una fuerza devastadora. 
    - **Skills**: turbo propulsores.

  - **Ice Age**:
    - **Modelo**: Ice Age.
    - **Descripción**: Con un diseño muy angular y agresivo, azul y blanco,  Ice Age parece no preocuparse por el camuflaje. Su arma favorita es un rifle de rayo congelador que porta con ambos brazos, y necesita recargarse tras cada uso. Pero no conviene subestimarlo: mientras se carga su rifle, puede usar sus misiles de racimo, que tienen un alcance considerable.
    - **HP**: 10.
    - **Movimiento**: 4.
    - **Armas**:
        - Nombre: Misiles de racimo
        - Alcance: 6.  
        - Daño: 4.
        - Descripción: Tiene dos afustes de misiles en los hombros, que disparan misiles de racimo. Tienen muy buen alcance, pero fallan la mita de ellos, por lo que su daño no es tan temible.
    - **Skills**: Rayo congelador.


# 6. Interfaz (UI/HUD)

## Menús:

Pantalla de inicio, Game Over y pausa.

## HUD:

Visualización de vidas, puntos o tiempo.
