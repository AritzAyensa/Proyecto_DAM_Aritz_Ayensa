package com.example.proyecto_dam_aritz_ayensa.utils

import android.annotation.SuppressLint
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

    private var sessionManager: SessionManager = SessionManager(this)

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


}


