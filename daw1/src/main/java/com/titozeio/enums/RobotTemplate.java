package com.titozeio.enums;

import com.titozeio.engine.Player;
import com.titozeio.engine.Robot;
import com.titozeio.model.Weapon;
import com.titozeio.skills.*;

/**
 * Plantillas de los 6 robots del GDD.
 * Actúa como Factory: cada valor instancia un Robot con sus stats exactas.
 *
 * Stats según el GDD:
 * -------------------------------------------------------------------
 * VICTORY_SABER | HP 12 | Mov 5 | Cañón de plasma (3/4) | PlasmaSaberAttack
 * BULLSEYE | HP 5 | Mov 5 | Doble cañón auto. (5/6) | GuidedMissile
 * BULWARK | HP 14 | Mov 4 | Cañón laser (3/3) | RepairNanobots
 * SCOUT | HP 8 | Mov 4 | Cañón automático (6/5) | JetpackBoost
 * DEATH_KNIGHT | HP 15 | Mov 4 | Martillo propulsado(1/8) | TurboPropulsion
 * ICE_AGE | HP 9 | Mov 4 | Misiles de racimo (4/3) | FreezeRay
 */
public enum RobotTemplate {

        VICTORY_SABER {
                @Override
                /**
                 * Crea una instancia de Victory Saber.
                 * 
                 * @param owner Jugador propietario.
                 * @return Robot configurado con stats de Victory Saber.
                 */
                public Robot createRobot(Player owner) {
                        Weapon weapon = new Weapon(
                                        "Cañón de plasma",
                                        3, 4,
                                        "Cañón de plasma. Poco efectivo y de corto alcance.",
                                        AttackType.ENERGY);
                        Skill skill = new PlasmaSaberAttack();
                        return new Robot(
                                        "Victory Saber",
                                        "Un robot de otra época, con aspecto bastante humanoide, anterior a las DAW1, "
                                                        + "pensado para ser pilotado por un humano y reconvertido a uso por la IA.",
                                        12, 5, weapon, skill, owner);
                }
        },

        BULLSEYE {
                @Override
                /**
                 * Crea una instancia de Bullseye.
                 * 
                 * @param owner Jugador propietario.
                 * @return Robot configurado con stats de Bullseye.
                 */
                public Robot createRobot(Player owner) {
                        Weapon weapon = new Weapon(
                                        "Doble cañón automático",
                                        5, 6,
                                        "En lugar de brazos, Bullseye cuenta con dos cañones automáticos "
                                                        + "que hacen bastante daño a un alcance importante.",
                                        AttackType.KINETIC);
                        Skill skill = new GuidedMissile();
                        return new Robot(
                                        "Bullseye",
                                        "También conocido como \"Glass Cannon\", es un robot muy rápido "
                                                        + "y con un gran alcance de ataque, pero con muy poco blindaje.",
                                        5, 5, weapon, skill, owner);
                }
        },

        BULWARK {
                @Override
                /**
                 * Crea una instancia de Bulwark.
                 * 
                 * @param owner Jugador propietario.
                 * @return Robot configurado con stats de Bulwark.
                 */
                public Robot createRobot(Player owner) {
                        Weapon weapon = new Weapon(
                                        "Cañón laser",
                                        3, 3,
                                        "Cañón laser montado en el hombro derecho. Poco efectivo y de corto alcance.",
                                        AttackType.ENERGY);
                        Skill skill = new RepairNanobots();
                        return new Robot(
                                        "Bulwark",
                                        "También conocido como DOC en el terreno de combate, es un robot muy resistente. "
                                                        + "Su capacidad para recuperar robots aliados lo convierte en pieza clave del equipo.",
                                        15, 4, weapon, skill, owner);
                }
        },

        SCOUT {
                @Override
                /**
                 * Crea una instancia de Scout.
                 * 
                 * @param owner Jugador propietario.
                 * @return Robot configurado con stats de Scout.
                 */
                public Robot createRobot(Player owner) {
                        Weapon weapon = new Weapon(
                                        "Cañón automático",
                                        6, 5,
                                        "Su brazo derecho es un cañón automático de gran alcance y potencia.",
                                        AttackType.KINETIC);
                        Skill skill = new JetpackBoost();
                        return new Robot(
                                        "Scout",
                                        "Un robot pequeño y ágil. Su movilidad gracias a los retropropulsores "
                                                        + "y su temible cañón automático lo convierten en pieza clave del equipo.",
                                        8, 4, weapon, skill, owner);
                }
        },

        DEATH_KNIGHT {
                @Override
                /**
                 * Crea una instancia de Death Knight.
                 * 
                 * @param owner Jugador propietario.
                 * @return Robot configurado con stats de Death Knight.
                 */
                public Robot createRobot(Player owner) {
                        Weapon weapon = new Weapon(
                                        "Martillo propulsado",
                                        1, 8,
                                        "Porta un martillo gigante a dos manos con retropropulsores "
                                                        + "que le permiten golpear con una fuerza devastadora.",
                                        AttackType.KINETIC);
                        Skill skill = new TurboPropulsion();
                        return new Robot(
                                        "Death Knight",
                                        "Un robot lento, extremadamente resistente y con un gran ataque cuerpo a cuerpo. "
                                                        + "Cuando activa sus retropropulsores, puede desplazarse rápidamente.",
                                        14, 4, weapon, skill, owner);
                }
        },

        ICE_AGE {
                @Override
                /**
                 * Crea una instancia de Ice Age.
                 * 
                 * @param owner Jugador propietario.
                 * @return Robot configurado con stats de Ice Age.
                 */
                public Robot createRobot(Player owner) {
                        Weapon weapon = new Weapon(
                                        "Misiles de racimo",
                                        4, 3,
                                        "Tiene dos afustes de misiles en los hombros. Tienen muy buen alcance, "
                                                        + "pero fallan la mitad, por lo que su daño no es tan temible.",
                                        AttackType.EXPLOSIVE);
                        Skill skill = new FreezeRay();
                        return new Robot(
                                        "Ice Age",
                                        "Con un diseño muy angular y agresivo, azul y blanco. Su arma favorita "
                                                        + "es un rifle de rayo congelador que porta con ambos brazos.",
                                        9, 4, weapon, skill, owner);
                }
        };

        /**
         * Instancia un Robot con las estadísticas predefinidas de esta plantilla.
         * 
         * @param owner el jugador al que pertenecerá el robot.
         * @return un nuevo objeto Robot.
         */
        public abstract Robot createRobot(Player owner);
}
