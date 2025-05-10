package com.example.proyecto_dam_aritz_ayensa.model.dao


import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore


class NotificacionEmergenteDAO {

    // Instancia de Firestore para interactuar con la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Instancia de FirebaseAuth para manejar la autenticación de usuarios
    private val auth = FirebaseAuth.getInstance()

    // Referencia a la colección "usuarios" en Firestore
    private val notificacionesEmergentesCollection = db.collection("notificacionesEmergentes")

    fun saveNotificacionEmergente(notificacionEmergenteData: Map<String, Any?>): Task<DocumentReference> {
        return notificacionesEmergentesCollection.add(notificacionEmergenteData)
    }


}