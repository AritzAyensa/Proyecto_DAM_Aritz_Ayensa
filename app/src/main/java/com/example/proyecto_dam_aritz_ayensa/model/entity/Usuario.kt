package com.example.proyecto_dam_aritz_ayensa.model.entity

/**
 * Clase de Usuario
 *
 * Esta clase sirve para indicar los campos que contendrá el documento (Tabla) del 'Usuario'.
 */
data class Usuario (
    var id: String = "",
    var nombre: String = "",
    var email: String = "",
    var idListas: List<String> = emptyList(),
    var idListasCompartidas: List<String> = emptyList(),
    var idsNotificaciones: Map<String, Boolean> = emptyMap()
)