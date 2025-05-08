package com.example.proyecto_dam_aritz_ayensa.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.proyecto_dam_aritz_ayensa.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MiFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(applicationContext)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        guardarTokenEnFirestore(token)
    }

    private fun guardarTokenEnFirestore(token: String) {
        val userRef = FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(sessionManager.getUserId().toString())

        userRef.update("fcmTokens", FieldValue.arrayUnion(token))
            .addOnSuccessListener {
                Log.d("FCM", "Token guardado correctamente")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error al guardar el token", e)
            }
    }

    @SuppressLint("MissingPermission")
    override fun onMessageReceived(msg: RemoteMessage) {
        val notif = NotificationCompat.Builder(this, "invitaciones")
            .setSmallIcon(R.mipmap.logo)
            .setContentTitle(msg.notification?.title)
            .setContentText(msg.notification?.body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notif.build())
    }
    /*override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Verificar si el mensaje contiene datos
        remoteMessage.data.isNotEmpty().let {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            if (title != null && body != null) {
                mostrarNotificacion(title, body)
            }
        }
    }

    private fun mostrarNotificacion(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "invitaciones"

        // Construir la notificación
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Mostrar la notificación
        notificationManager.notify(0, notification)
    }*/


}


