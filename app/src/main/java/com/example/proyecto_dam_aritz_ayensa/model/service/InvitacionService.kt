package com.example.proyecto_dam_aritz_ayensa.model.service
import com.example.proyecto_dam_aritz_ayensa.model.dao.InvitacionDAO
import com.google.firebase.firestore.DocumentReference


class InvitacionService(private val invitacionDAO: InvitacionDAO) {
    fun notificacionCompartirLista(
        inviteData: Map<String, Any?>,
        onSuccess: (DocumentReference) -> Unit,
        onError: (Exception) -> Unit
    ) {
        invitacionDAO.addInvitation(inviteData)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onError)
    }
}