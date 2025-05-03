package com.example.proyecto_dam_aritz_ayensa.model.entity

import java.util.Date

data class Notificacion (
    var id: String = "",
    var tipo: Int = 0,
    var descripcion: String = "",
    var idProductos: List<String> = emptyList(),
    var precio: Double = 0.0,
    var fecha: String = "",
)