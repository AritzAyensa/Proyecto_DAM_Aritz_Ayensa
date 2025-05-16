package com.example.proyecto_dam_aritz_ayensa.utils

object GenericConstants {
    //TIPO NOTIFICACION
    const val TIPO_LISTA_COMPRATIDA = 1;
    const val TIPO_COMPRA = 2;

    //DURACION MENSAJES
    const val MUY_CORTO = 500;
    const val CORTO = 1000;
    const val LARGO = 2000;


    //CATEGORIAS/PRIORIDADES EROSKI ROTXAPEA

    val PRIORIDAD_CATEGORIAS: Map<String, Double> = mapOf(
        "Sin glúten" to 1.0,
        "Higiene personal" to 1.2,
        "Cosmeticos" to 1.4,
        "Parafarmacia" to 1.5,
        "Higiene dental" to 1.6,
        "Higiene femenina" to 1.7,
        "Bebés" to 1.5,
        "Mascotas" to 2.0,
        "Limpieza" to 3.0,
        "Conservas" to 4.0,
        "Tomate frito" to 4.2,
        "Arroz" to 4.5,
        "Pasta" to 5.0,
        "Comida instantánea" to 5.5,
        "Harinas" to 7.0,
        "Aceites" to 8.0,
        "Mariscos" to 10.0,
        "Salsas" to 11.5,
        "Legumbres" to 12.0,
        "Helados" to 13.0,
        "Congelados" to 15.0,
        "Batidos" to 16.0,
        "Chocolates" to 17.0,
        "Galletas" to 18.0,
        "Café" to 19.0,
        "Cacao" to 20.0,
        "Desayuno" to 21.0,
        "Azúcar/Edulcorantes" to 22.0,
        "Pan de molde" to 23.0,
        "Mermeladas" to 24.0,
        "Repostería casera" to 25.0,
        "Frutos secos" to 26.0,
        "Encurtidos" to 27.0,
        "Leche" to 28.0,
        "Bebidas" to 29.0,
        "Snacks" to 30.0,
        "Alcohol" to 31.0,
        "Huevos" to 32.0,
        "Bio" to 32.5,
        "Lácteos" to 33.0,
        "Carne envasada" to 34.0,
        "Tubérculo" to 34.5,
        "Fruta" to 35.0,
        "Verdura" to 36.0,
        "Comida preparada" to 37.0,
        "Embutido" to 38.0,
        "Pan" to 39.0,
        "Carnicería" to 40.0,
        "Queso" to 41.0,
        "Pescadería" to 42.0,
        "Pescado/Marisco congelado" to 43.0,
    )


}