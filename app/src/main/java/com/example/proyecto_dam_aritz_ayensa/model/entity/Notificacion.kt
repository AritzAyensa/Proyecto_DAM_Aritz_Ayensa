package com.example.proyecto_dam_aritz_ayensa.model.entity

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import java.text.SimpleDateFormat
import java.util.Locale

data class Notificacion(
    var id: String = "",
    var tipo: Int = 0,
    var descripcion: String = "",
    var idProductos: List<String> = emptyList(),
    var precio: Double = 0.0,
    var fecha: Timestamp = Timestamp.now()
):Comparable<Notificacion> {
    override fun compareTo(other: Notificacion): Int {
        return other.fecha.compareTo(this.fecha)
    }
}
