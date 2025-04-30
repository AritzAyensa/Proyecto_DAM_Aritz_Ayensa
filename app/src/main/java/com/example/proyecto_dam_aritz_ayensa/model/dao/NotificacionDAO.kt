package com.example.proyecto_dam_aritz_ayensa.model.dao


import android.util.Log
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await


class NotificacionDAO {

    // Instancia de Firestore para interactuar con la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Instancia de FirebaseAuth para manejar la autenticación de usuarios
    private val auth = FirebaseAuth.getInstance()

    // Referencia a la colección "usuarios" en Firestore
    private val notificacionesCollection = db.collection("notificaciones")


    suspend fun saveNotificacion(notificacion: Notificacion): String {
        val docRef = notificacionesCollection.document()

        val notificacionData = hashMapOf(
            "id" to docRef.id,
            "tipo" to notificacion.tipo,
            "descripcion" to notificacion.descripcion,
            "idsUsuarios" to notificacion.idsUsuarios,
            "idProductos" to notificacion.idProductos,
            "precio" to notificacion.precio,
        )
        Log.e("NotificacionDAO", "Notificacion creada")
        docRef.set(notificacionData).await()
        return docRef.id
    }

    suspend fun getNotificacionesPorUsuario(idUsuario: String): List<Notificacion> {
        return try {
            val querySnapshot = notificacionesCollection
                .whereArrayContains("idsUsuarios", idUsuario)
                .get()
                .await()
            // Mapear al data class Notificacion
            querySnapshot.toObjects(Notificacion::class.java)
        } catch (e: Exception) {
            Log.e("NotificacionDAO", "Error al obtener notificaciones para el usuario $idUsuario", e)
            emptyList()
        }
    }


    suspend fun eliminarNotificacion(idNotificacion: String, idUsuario: String) {
        try {
            // Utilizamos arrayRemove para sacar el idUsuario del array
            notificacionesCollection
                .document(idNotificacion)
                .update("idsUsuarios", FieldValue.arrayRemove(idUsuario))
                .await()
            Log.i("NotificacionDAO", "Usuario $idUsuario eliminado de notificación $idNotificacion")
        } catch (e: Exception) {
            Log.e("NotificacionDAO", "Error al eliminar usuario $idUsuario de notificación $idNotificacion", e)
            throw e
        }
    }

}