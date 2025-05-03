package com.example.proyecto_dam_aritz_ayensa.utils

object GenericConstants {
    const val USUARIOS = "usuarios";

    //TIPO NOTIFICACION
    const val TIPO_LISTA_COMPRATIDA = 1;
    const val TIPO_COMPRA = 2;

    //DURACION MENSAJES
    const val MUY_CORTO = 500;
    const val CORTO = 1000;
    const val LARGO = 2000;


    //CATEGORIAS/PRIORIDADES EROSKI ROTXAPEA

    val PRIORIDAD_CATEGORIAS: Map<String, Double> = mapOf(
        "Fruta" to 1.0,
        "Verdura" to 2.0,
        "Carnes" to 3.0,
        "Carne" to 4.0,
        "Pescado" to 5.0,
        "Limpieza" to 6.0,
        "Higiene" to 7.0,
        "Otros" to 8.0,
    )

}