package com.example.proyecto_dam_aritz_ayensa.model.entity

import com.example.proyecto_dam_aritz_ayensa.utils.GenericConstants

data class Producto(
    var id: String = "",
    var nombre: String = "",
    var precioAproximado: Double = 0.0,
    var codigoBarras: String = "",
    var categoria: String = "",
    var idCreador: String = ""
):Comparable<Producto> {
    override fun compareTo(other: Producto): Int {
        // Sacamos la prioridad de la categoría, con un valor muy alto por defecto
        val thisPri = GenericConstants.PRIORIDAD_CATEGORIAS[this.categoria] ?: Double.MAX_VALUE
        val otherPri = GenericConstants.PRIORIDAD_CATEGORIAS[other.categoria] ?: Double.MAX_VALUE

        // Comparamos solo por prioridad
        return thisPri.compareTo(otherPri)
    }
    override fun toString(): String = nombre


}