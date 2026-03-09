package com.titozeio.engine;

import com.titozeio.enums.RobotTemplate;

/**
 * Factoría para la creación de robots.
 * Centraliza la instanciación basada en plantillas.
 */
public class RobotFactory {

    /**
     * Crea un nuevo robot basado en su nombre de modelo.
     * 
     * @param typeName Nombre del modelo (ej: "Bullseye").
     * @param owner    Jugador dueño del robot.
     * @return El robot instanciado o Victory Saber como fallback.
     */
    public static Robot createRobot(String typeName, Player owner) {
        try {
            String enumName = typeName.toUpperCase().replace(" ", "_");
            return RobotTemplate.valueOf(enumName).createRobot(owner);
        } catch (IllegalArgumentException | NullPointerException e) {
            System.err.println(
                    "[RobotFactory] Tipo de robot no reconocido: " + typeName + ". Usando Victory Saber por defecto.");
            return RobotTemplate.VICTORY_SABER.createRobot(owner);
        }
    }
}
