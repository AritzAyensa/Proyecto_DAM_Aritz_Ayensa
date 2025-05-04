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
        val thisPri = GenericConstants.PRIORIDAD_CATEGORIAS[this.categoria] ?: Double.MAX_VALUE
        val otherPri = GenericConstants.PRIORIDAD_CATEGORIAS[other.categoria] ?: Double.MAX_VALUE
        val priCompare = thisPri.compareTo(otherPri)
        if (priCompare != 0) return priCompare

        return this.nombre.compareTo(other.nombre, ignoreCase = true)
    }
    override fun toString(): String = nombre


}