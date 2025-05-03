package com.example.proyecto_dam_aritz_ayensa.model.dao


import android.util.Log
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
            /*"idsUsuarios" to notificacion.idsUsuarios,*/
            "idProductos" to notificacion.idProductos,
            "fecha" to notificacion.fecha,
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

    suspend fun getNotificacionesByIds(idNotificaciones: List<String>): List<Notificacion> {
        if (idNotificaciones.isEmpty()) return emptyList()

        val notificaciones = mutableListOf<Notificacion>()
        val chunks = idNotificaciones.chunked(10)

        for (chunk in chunks) {
            val querySnapshot = notificacionesCollection
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()

            notificaciones.addAll(querySnapshot.toObjects(Notificacion::class.java))
        }

        return notificaciones
    }


    suspend fun eliminarNotificaciones(
        notificaciones: List<String>,
        idUsuario: String
    ) {
        try {
            for (notificacion in notificaciones) {
                notificacionesCollection
                    .document(notificacion)
                    .update("idsUsuarios", FieldValue.arrayRemove(idUsuario))
                    .await()
                Log.i(
                    "NotificacionDAO",
                    "Usuario $idUsuario eliminado de notificación ${notificacion}"
                )
            }
        } catch (e: Exception) {
            Log.e(
                "NotificacionDAO",
                "Error al eliminar usuario $idUsuario de notificaciones",
                e
            )
            throw e
        }
    }

    fun notificacionesCountFlow(userId: String): Flow<Int> = callbackFlow {
        val query = notificacionesCollection
            .whereArrayContains("idsUsuarios", userId)

        val registration = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                close(error)
            } else {
                try {
                    trySend(snapshots?.size() ?: 0).isSuccess
                } catch (e: Exception) {
                    close(e)
                }
            }
        }

        awaitClose {
            registration.remove()
        }
    }
}