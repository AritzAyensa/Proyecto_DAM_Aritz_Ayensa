package com.example.proyecto_dam_aritz_ayensa.model.service
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionEmergenteDAO
import com.google.firebase.firestore.DocumentReference


class NotificacionEmergenteService(private val notificacionEmergenteDAO: NotificacionEmergenteDAO) {
    fun saveNotificacionEmergente(
        notificacionEmergenteData: Map<String, Any?>,
        onSuccess: (DocumentReference) -> Unit,
        onError: (Exception) -> Unit
    ) {
        notificacionEmergenteDAO.saveNotificacionEmergente(notificacionEmergenteData)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onError)
    }
}