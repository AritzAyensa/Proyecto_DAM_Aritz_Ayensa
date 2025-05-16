package com.example.proyecto_dam_aritz_ayensa.model.entity

data class Lista (
    var id: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var color: String = "",
    var idCreador: String = "",
    var idProductos: List<String> = emptyList(),
    var idProductosSeleccionados: List<String> = emptyList()
)