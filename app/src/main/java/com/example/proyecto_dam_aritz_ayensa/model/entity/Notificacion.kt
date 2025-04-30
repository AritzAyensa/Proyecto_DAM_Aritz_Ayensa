package com.example.proyecto_dam_aritz_ayensa.model.entity

data class Notificacion (
    var id: String = "",
    var tipo: Int = 0,
    var descripcion: String = "",
    var idsUsuarios: List<String> = emptyList(),
    var idProductos: List<String> = emptyList(),
    var precio: Double = 0.0,
)