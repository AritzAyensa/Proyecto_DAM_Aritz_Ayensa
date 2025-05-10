package com.example.proyecto_dam_aritz_ayensa.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.proyecto_dam_aritz_ayensa.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService : FirebaseMessagingService() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        guardarTokenEnFirestore(token)
    }

    private fun guardarTokenEnFirestore(token: String) {
        sessionManager = SessionManager(applicationContext)
        val userRef = FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(sessionManager.getUserId().toString())

        // Añadir token como elemento de un ARRAY (no mapa)
        userRef.update("fcmTokens", FieldValue.arrayUnion(token))
            .addOnSuccessListener {
                Log.d("FCM", "Token guardado correctamente")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error al guardar el token", e)
            }
    }

    @SuppressLint("MissingPermission") // El permiso se verifica en Activity
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Manejar notificación en primer plano
        remoteMessage.notification?.let { notification ->
            mostrarNotificacion(
                notification.title ?: "Nueva invitación",
                notification.body ?: "Tienes una nueva invitación"
            )
        }

        // Procesar datos adicionales (opcional)
        val listId = remoteMessage.data["listId"]
        val fromUid = remoteMessage.data["fromUid"]
        Log.d("FCM", "Datos recibidos: listId=$listId, fromUid=$fromUid")
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "invitaciones",
            "Invitaciones",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Canal para notificaciones de invitaciones"
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    private fun mostrarNotificacion(title: String, body: String) {
        val notification = NotificationCompat.Builder(this, "invitaciones")
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}