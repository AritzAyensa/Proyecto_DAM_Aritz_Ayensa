package com.example.proyecto_dam_aritz_ayensa.model.entity

data class Lista (
    var id: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var idCreador: String = "",
    var idsUsuariosCompartidos: List<String> = emptyList()
)