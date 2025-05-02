package com.example.proyecto_dam_aritz_ayensa.model.entity

data class Producto(
    var id: String = "",
    var nombre: String = "",
    var precioAproximado: Double = 0.0,
    var codigoBarras: String = "",
    var prioridad: Double = 0.0,
    var categoria: String = "",
    var idCreador: String = ""
):Comparable<Producto> {
    override fun compareTo(other: Producto): Int {
        return this.prioridad.compareTo(other.prioridad)
    }
    override fun toString(): String = nombre


}