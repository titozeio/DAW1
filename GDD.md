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
  - [Sistema de Coordenadas del Mapa:](#sistema-de-coordenadas-del-mapa)
  - [Arquitectura y Patrones:](#arquitectura-y-patrones)
  - [Estructura de Paquetes:](#estructura-de-paquetes)
- [4. Bucle de Juego (Game Loop)](#4-bucle-de-juego-game-loop)
- [5. Contenido, Niveles](#5-contenido-niveles)
  - [Asset List:](#asset-list)
  - [Diseño de Nivel (mapa):](#diseño-de-nivel-mapa)
  - [Lista de skills:](#lista-de-skills)
  - [Lista de Robots:](#lista-de-robots)
- [6. Interfaz (UI/HUD)](#6-interfaz-uihud)
  - [Pantalla de inicio:](#pantalla-de-inicio)
  - [Pantalla de Nueva partida (Selección de Robots):](#pantalla-de-nueva-partida-selección-de-robots)
  - [Pantalla de Combate:](#pantalla-de-combate)


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

1. P1 posiciona sus robots en las casillas de despliegue designadas para él en el mapa. Puede elegir qué casillas usar, en caso de haber más casillas que robots.
2. P2 hace lo mismo en las casillas designadas para él.
3. P1 realiza su turno. Se comprueba si se cumplen las condiciones de victoria. Si se cumple alguna, se acaba el juego y gana el jugador que cumpla las condiciones. Si no, se pasa al paso 4.
4. P2 realiza su turno. Se comprueba si se cumplen las condiciones de victoria.  Si se cumple alguna, se acaba el juego y gana el jugador que cumpla las condiciones. Si no, se vuelve al paso 3.

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

El proyecto se desarrolla como una aplicación de escritorio multiplataforma utilizando tecnologías modernas de Java.

- **Lenguaje**: Java 21+.
- **Motor Gráfico y UI**: JavaFX 21. Se utiliza un enfoque declarativo mediante archivos **FXML** para la estructura y **CSS** para el estilo visual.
- **Herramientas de Diseño**: 
    - **SceneBuilder**: Para la maquetación visual de las pantallas y componentes.
    - **Tiled Map Editor**: Para la creación de niveles, definición de capas de terreno y exportación en formato JSON o XML para su integración en el motor.
- **Gestión de Proyecto**: **Maven** para la resolución de dependencias, compilación y empaquetado.

## Sistema de Coordenadas del Mapa:

Para la lógica del tablero hexagonal, se utiliza un sistema de **Coordenadas Axiales (q, r)**:
- **q (columna)**: Eje horizontal inclinado.
- **r (fila)**: Eje vertical.
- **s (derivada)**: Se calcula como `s = -q - r` para permitir cálculos rápidos de distancia y líneas de visión mediante el sistema de coordenadas cúbicas.
- **Orientación**: Los hexágonos están orientados con "punta arriba" (pointy-topped) para una mejor perspectiva táctica.

## Arquitectura y Patrones:

- **Modelo-Vista-Controlador (MVC)**: Clara separación entre los modelos de datos (`Robot`, `Map`), las vistas (FXML) y los controladores de UI.
- **Arquitectura de Componentes Reutilizables**: El motor permite la inyección de componentes FXML dentro de otros contenedores (ej: `RobotCard.fxml` dentro de `RobotSelectionScreen.fxml`), facilitando el mantenimiento y la escalabilidad de la interfaz.
- **Sistema de Enums**: Los parámetros de configuración (Robots, Terrenos, Skills) se gestionan mediante Enums que actúan como fábricas (`Template Pattern`), asegurando la integridad de los datos en tiempo de ejecución.

## Estructura de Paquetes:

- `com.titozeio.engine`: Núcleo de lógica del juego (mapa, jugadores, hexagon).
- `com.titozeio.ui`: Controladores JavaFX y lógica de presentación.
- `com.titozeio.model`: Definición de entidades (Weapon, Objective).
- `com.titozeio.enums`: Enumerados constantes (RobotTemplate, TerrainType).
- `com.titozeio.skills`: Implementación de habilidades especiales.
- `com.titozeio.victory`: Gestores de condiciones de victoria.

# 4. Bucle de Juego (Game Loop)

1. El juego comienza en la pantalla de **Nueva Partida**, donde se ve el título, instrucciones y se comienza el juego.
2. Después se pasa a la pantalla de **Elección de Robots**, donde los jugadores eligen sus robots. 
3. Una vez que ambos jugadores han elegido sus 3 robots, de forma alterna, se pasa a la pantalla de **Combate**, donde se desarrolla el core loop.
4. Una vez se cumplen las condiciones de victoria del mapa en cuestión, se muestra el mensaje de victoria y se vuelve a la pantalla de **Nueva Partida** y vuelta al paso 1.

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

- **Saber Prime**:
    - **Modelo**: Saber Prime.
    - **Descripción**: Un robot de vanguardia con una silueta heroica y atlética, inspirado en la estética clásica de los grandes mechas pero con tecnología militar moderna. Su casco evoca el estilo icónico de la serie "Victory Saber". Su presencia en el campo de batalla inspira respeto, especialmente cuando desenvaina su imponente sable de energía chisporroteante. Visualmente usa una paleta azul real vibrante con acentos rojos y metal expuesto.
    - **HP**: 12.
    - **Movimiento**: 4.
    - **Armas**:
        - Nombre: Cañón de plasma.
        - Alcance: 3.  
        - Daño: 3.
        - Descripción: Cañón de plasma compacto montado en el brazo izquierdo. Poco efectivo y de corto alcance.
    - **Skills**: Sable de plasma (hoja de energía pura y chisporroteante).
  
- **Bullseye**:
    - **Modelo**: Bullseye.
    - **Descripción**: También conocido en el terreno de combate como "Glass Cannon" (Cañón de cristal). Es un robot de artillería móvil con un sensor óptico central de color rojo intenso que proyecta un destello letal. Muy rápido y con gran alcance, pero con blindaje ligero y visiblemente desgastado por el combate.
    - **HP**: 6.
    - **Movimiento**: 5.
    - **Armas**:
        - Nombre: Doble cañón automático.
        - Alcance: 4.  
        - Daño: 5.
        - Descripción: Brazos integrados convertidos en cañones automáticos rotatorios.
    - **Skills**: Misiles teledirigidos.
  
- **Bulwark**:
    - **Modelo**: Bulwark.
    - **Descripción**: Bulwark, también conocido como DOC en el terreno de combate, es un robot extremadamente robusto y pesado. Su estructura masiva y su capacidad de reparación mediante nanobots lo convierten en el muro infranqueable del equipo. Su acabado visual usa camuflaje verde oliva con luces técnicas cyan.
    - **HP**: 15.
    - **Movimiento**: 4.
    - **Armas**:
        - Nombre: Cañón laser.
        - Alcance: 3.  
        - Daño: 3.
        - Descripción: Cañón laser montado en el hombro derecho.    
    - **Skills**: Nanobots reparadores.

- **Scout**:
    - **Modelo**: Scout.
    - **Descripción**: Un robot ligero de reconocimiento con piernas digitígradas que le permiten una agilidad asombrosa. Su cabeza cuenta con un array multisensor cian. Equipado con potentes retropropulsores de plasma azul en la espalda, es capaz de flanquear posiciones enemigas en segundos. Su pintura usa camuflaje táctico marrón y verde.
    - **HP**: 7.
    - **Movimiento**: 5.
    - **Armas**:
        - Nombre: Cañón automático.
        - Alcance: 5.  
        - Daño: 5.
        - Descripción: Cañón automático de alta precisión integrado en el brazo derecho.  
    - **Skills**: Retropropulsores.

- **Death Knight**:
    - **Modelo**: Death Knight.
    - **Descripción**: Una mole de asalto con una armadura medieval-gótica pesada inspirada en los caballeros oscuros. Su esquema de color es monocromático: negro obsidiana y gris carbón, resaltado únicamente por sus ojos de un rojo intenso con efecto lens flare. Carece de propulsores en la espalda; toda su potencia de empuje reside en los talones de sus pies sobredimensionados y en su martillo gigante propulsado. 
    - **HP**: 13.
    - **Movimiento**: 3.
    - **Armas**:
        - Nombre: Martillo propulsado.
        - Alcance: 1.  
        - Daño: 8.
        - Descripción: Martillo masivo a dos manos con múltiples motores de cohete en ignición activa. 
    - **Skills**: turbo propulsores (integrados en los talones).

- **Ice Age**:
    - **Modelo**: Ice Age.
    - **Descripción**: Especialista en control técnico con una silueta estilizada y angular. Su cabeza cuenta con un visor cibernético de una sola línea horizontal (estilo Robocop) que emite una luz azul intensa con efecto lens flare. Su arma principal es un rifle criogénico de alta tecnología que dispara ráfagas de nitrógeno líquido. Es un robot preciso, letal y con una estética que mezcla lo gótico-técnico con lo ártico.
    - **HP**: 10.
    - **Movimiento**: 4.
    - **Armas**:
        - Nombre: Misiles de racimo
        - Alcance: 6.  
        - Daño: 4.
        - Descripción: Lanzamisiles de hombro integrados de perfil bajo.
    - **Skills**: Rayo congelador.

### Dirección visual canónica para concept art (vinculante)

- Estética objetivo: mecha militar-industrial tipo Battletech pero con toques de diseño heroico o gótico según el modelo.
- Acabado visual obligatorio: uso de ojos/sensores rojos con efecto lens flare para acentuar la naturaleza de la IA.
- Desgaste realista: suciedad técnica, rayones, marcas de impacto y metal expuesto.
- Mantener coherencia estricta arma-mano:
  - Saber Prime: sable de plasma (energía pura) en mano derecha.
  - Bullseye: brazos-cañón.
  - Death Knight: gran martillo a dos manos, sin propulsores dorsales.
  - Scout: piernas digitígradas y retropropulsores de plasma azul.


# 6. Interfaz (UI/HUD)

## Pantalla de inicio:
- **Descripción**: La pantalla de inicio es la primera interacción del jugador. Presenta una estética ciberpunk/futurista coherente con el lore del juego.
- **Elementos Visuales**: 
    - **Fondo**: Imagen de arte conceptual (`Splash concept.png`) que cubre toda la pantalla.
    - **Panel Central**: Un contenedor semi-transparente oscuro (`rgba(0,0,0,0.4)`) con bordes redondeados que agrupa el título y las acciones.
    - **Título**: Logotipo del juego (`title_transparent.png`) centrado en la parte superior del panel.
    - **Acciones**: 
        - Botón de "Play" estilizado con una imagen de base personalizada (`boton_1.png`) y tipografía "Exo 2".
        - Botón de "Settings" estilizado con una imagen de base personalizada (`boton_1.png`) y tipografía "Exo 2". Abre el panel de settings.
    - **Panel de settings**: Un panel (que necesita su gráfico correspondiente, mientras crear un panel default) que se abre por encima del contenido de la pantalla a modo de popup con un overlay que oscurezca todo. contiene:
        - Toggle (icono) de musica on/off (con su texto en "Exo 2").
        - Toggle (icono) de sfx on/off (con su texto en "Exo 2").
        - Botón de cerrar el panel (un botón con típica aspa en x). Cuando se pulsa se cierra el popup.

## Pantalla de Nueva partida (Selección de Robots):
- **Descripción**: 
    - Los jugadores eligen su equipo de 3 robots de forma alterna. Por equilibrio competitivo, **J2 elige primero** (compensando que J1 tendrá el primer turno en combate).
- **Elementos UI**: 
    - **Fondo**: Imagen de fondo de hangar/laboratorio (`bg1.png`).
    - **Título**: Panel contenedor del título de la pantalla (a unos 30 px del top, centrado horizontalmente, unos 800 px de ancho). El gráfico del texto aun por hacer.
    - **Bloque de selección de robots**: (unos 650 pix de alto, a unos 120px del top, ocupando todo el ancho).  No tiene elemento visual, es un contenedor. Contenido:
        - Botones de Flechas de izquierda y derecha a cada lado del bloque (a unos 20 pix de los lados), para ir rotando entre la info del robot que se muestra en el panel. Entre los botones hay más contenido con imágenes e info del robot seleccionado en ese momento. Inicialmente, se verá el primero por defecto (Saber Prime en esta versión). Cada vez que se pulsa a izquierda se va al robot anterior (si no lo hay, se va al último). Cada vez que se pulsa en la derecha se va al siguiente (si no lo hay, al primero). Si solo queda un robot, los botones de izquierda y derecha se inhabilitan (efecto de inhabilitación).
        - Panel contenedor de concepts (izquierda)  (aprox. 700x700 px). Un contenedor semi-transparente oscuro (`rgba(0,0,0,0.4)`).Muestra la imagen de concept del robot correspondiente. Las imágenes están en src\main\resources\com\titozeio\images y terminan en "_concept.jpg" (por ejemplo, "iceage_concept.jpg").
        -Panel de stats (derecha de concepts) (375x270 px). Aun sin elemento de UI final. Muestra los HPs del robot y el alcance y daño del arma. 
        -Panel de Skills (derecha de concepts, debajo de stats) (375x340 px). Elemento de UI similar al panel de stats, sin gráfico aun. Muestra la info del skill: Nombre, tipo (defensa, ataque, curación,...), y sus stats (alcance y daño para ataque, por ejemplo).
        -Panel de info del robot seleccionado (derecha de stats y skills) (aprox.500 x700 px). Elemento de UI similar al panel de stats, sin gráfico aun. Muestra los textos de descripción del robot, su arma y su skill.
    - **Botón de “Confirmar selección"**: No tiene elemento visual aun.(Unos 10 px debajo del bloque de selección de robots). Cuando el J1 o el J2 pulsan este botón confirman que se quieren quedar con el robot seleccionado (aquel del que se está mostrando la info ahora mismo). El turno de selección pasa al otro jugador, se muestra brevemente el “mensaje de cambio de jugador” y el robot seleccionado deja de estar disponible en la lista de seleccionables. Si ya no quedan robots por seleccionar, entonces en lugar de eso, pasamos directamente a la pantalla de Game Screen.
    - **Bloque de abajo**: Bloque de abajo (a unos 20 cm del bottom), centrado:  No tiene elemento visual, es un contenedor. Contenido:
        -Panel de robots seleccionados de P1. Un panel de UI que aun no tenemos. Conteenido:
            -Indicador “P1” de texto.
            - Lista de robots seleccionados por P1 (inicialmente vacío,  con siluetas temporales que serán sustituidas por las imágenes de los robots que vaya seleccionando P1). Las imágenes serán las mismas que se usan en el GameScreen(src\main\resources\com\titozeio\sprites, terminados en “_P1” para P1).
            - Nombres en pequeño de cada robot debajo de su imagen.
        -Panel de robots seleccionados de P2 (contenido similar al de P1 pero aplicado a P2). A la derecha del conteneedor de robots de P1.
        -Botón de Terminar al azar" (a unos 20 px a la derecha del panel de P2). Al ser pulsado, El sistema realizará las selecciones pendientes al azar y pasamos al Game Screen.
    - **Mensaje de cambio de jugador**: Cada vez que un jugador confirma su selección (y al mostrarse la pantalla al principio), aparece un panel de mensaje centrado, mostrando: “Turno de elegir robot: P1” (o P2, si es el turno de P2). El panel de mensaje aparece centrado, y todo lo demás difuminado o con un overlay oscureciendo todo. A los 3 segundos (o si uno de los jugadores hace click donde sea) se cierra, quitándose el overlay.

- **Flujo de Selección**: 
    1. Se muestra el  "Mensaje de cambio de jugador" con la ifno correspondiente..  
    2. Una vez se cierre el popup del "Mensaje de cambio de jugador", el jugador activo puede ir visualizando la info (concepts, stats, descripciones) del robot activo de la lista de disponibles, y puede pasar al siguiente robot disponiblee pulsando en la flecha dercha o al anterior en la izquierda. Una vez que pulse en el botón de "Confirmar selección", el robot seleccionado pasa a formar parte de su equipo, se elimina de los robots disponibles (apareciendo en su bloque), los paneles pasan a mostrar la info del siguiente (si no quedaran, se habría completado la selección y pasamos a la Game Screen).
    3. Si aun quedan robots aparecería el  "Mensaje de cambio de jugador" con la ifno correspondiente. Se cambia de jugador activo, y volvemos al paso 2.
    4. En cualquier momento, cualquiera de los dos jugadores puede pulsar el botón de "Terminar al azar". El sistema realizará las selecciones pendientes al azar y pasamos al Game Screen.

 ## Pantalla de Combate:
  - **Descripción**: La pantalla de combate es la pantalla principal del juego y donde se desarrola el core loop. 
  - **Elementos**: 
    - **Turno actual**: Indica el número de turno actual de quién es el turno (por ej.: "Turn: 5. P1")
    - **Mapa**: Se muestra el mapa donde se desarrolla el combate.
    - **Mensajes**: Se muestran los mensajes del juego.Pueden ser los siguientes:
      - "Objetivos del mapa: [objetivos]" (va acompañado de un botón de "Aceptar" que al pulsarlo pasa al siguiente mensaje)
      - "Jugador 1, posiciona tus robots" (pasa al siguiente mensaje cuando P1 ha posicionado todos sus robots)
      - "Jugador 2, posiciona tus robots" (desaparece cuando P2 ha posicionado todos sus robots, y muestra el mensaje de "Turno actual")
    - **Mensajes de combate**: Cuando se produce daño a un robot, se muestra un mensaje indicando el daño causado y el robot que lo ha recibido. Por ejemplo: "[Robot1] ha impactado en [Robot2] causando [daño] puntos de daño!".
    - **Bloque de info adicional**: Un panel, inicalmente vacío (simplemente un label informativo de "info"), que mostrará info adicional cuando: 
      - Se pasa el ratón por encima de un robot: se muestra su nombre, estadísticas y descripción.
      - Se pasa el ratón por encima de un terreno: se muestra su descripción, el tipo, la altura.
    - **Botón de Fin de turno**: El jugador puede pulsar este botón para indicar que ha terminado de realizar las acciones de sus robots. Al pulsarlo, se pasa al siguiente jugador. Si queda algún robot por realizar acciones, se mostrará un mensaje indicándolo: "Jugador [X], te quedan robots por realizar acciones. Seguro que quieres finalizar el turno?" con dos opciones: "Cancelar" y "Finalizar turno". Cuando al jugador no le quedan más acciones de ningún robot, se realiza el cambio de turno automáticamente, mostrándose un mensaje antes.
    - **Botón de Pausa**: El jugador puede pulsar este botón para pausar el juego. Al pulsarlo, se muestra un menú con las opciones: "Reanudar", "Rendirse". Si pulsa "Reanudar", se vuelve a la pantalla de combate. Si pulsa "Rendirse", se muestra el mensaje de "Victoria"del oponente. 
    - **Robots**: 
      - Los robots aparecen representados por figuras (a escala, de forma que quepan sobradamente en un hexágono del mapa) fuera del mapa al comienzo y luego, una vez posicionados, en las casillas elegidas. Debajo de cada robot aparece una barra de vida que indica su barra actual de HP. La barra está seccionadao en segmentos. Cada uno es 1 HP.
      - Cuando se pasa el ratón por encima de un robot, se resalta de alguna manera (y se muestra la info correspondiente en el bloque de info adicional).
      - Cuando se hace clic en un robot, se muestra un menú con las acciones disponibles para ese robot inicialmente: moverse, atacar, usar habilidad (se mostaría el nombre de la skill). 
        - Moverse: El sistema calcula las casillas a las que puede moverse el robot (teniendo en cuenta su movimiento, el terreno y los obstáculos) y las resalta. El jugador puede hacer clic en una de las casillas resaltadas para confirmar el moviento.El robot se desplaza a la casilla seleccionada. Se puede seleccionar la casilla donde está el robot si no desea reealizar ningún movimiento.
        - Atacar: El sistema calcula las casillas a las que puede disparar el robot (teniendo en cuenta su alcance, el terreno y los obstáculos) y las resalta. Si hay algún enemigo en alguna de las casillas resaltadas, se resalta de alguna manera especial. El jugador puede hacer clic en una de las casillas donde haya un enemigo para realizar el ataque. Se produce una animación de ataque y se resuelve el daño. Aparece un efecto de impacto sobre el robot enemigo, con el número de daño, y la barra de HP se actualiza. También se muestra en mensaje en grande en el **Mensajes de combate**. También se puede elegir hacer click en la casilla propia o en cualquiera, si no se quiere o puede atacar a un enemigo.
        - Usar habilidad: Dependiendo de si es una skill de ataque o de movimiento, el sistema actúa como corresponda, aplicando las reglas especiales de cada skill. Por ejemplo, la skill retropropulsares, al ser de movimiento, resaltará las casillas a las que puede moverse el robot, pero sin tener en cuenta el terreno. Las casillas resaltas  para las skills, ya sean por movimiento o ataque, tendrán un color diferente.
        - Cancelar: Cuando se seleccionan las opciones movimiento, atacar, o skill, aparece este botón de cancelar, que cancela la acción seleccionada y vuelve a mostrar el menú de acciones disponibles.
        - Confirmación de acción: Una vez que el jugador ha realizado una acción de movimiento o skill de movimiento, se activa automáticamente la acción de atacar. Y viceversa: una vez realizada la acción de ataque (o skill de ataque), se activa automáticamente la acción de movimiento. (una vez se empieza a realizar las acciones de un robot, hay que completar ambas).
        - Cuando un robot ha realizado sus dos acciones posibles, el robot aparece como "agotado" (un efecto visual de algún tipo, por ejemplo, se oscurece) y no se puede volver a seleccionar.
       


