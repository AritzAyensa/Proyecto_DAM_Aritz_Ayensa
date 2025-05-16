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
/**
 * Clase: MyFirebaseService
 *
 * Servicio para manejar tokens FCM y mostrar notificaciones push.
 */
class MyFirebaseService : FirebaseMessagingService() {

    private val CHANNEL_ID = "notificacionesEmergentes"

    /**
     * Método: onCreate
     *
     * Se llama al crear el servicio. Crea el canal de notificaciones.
     */
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    /**
     * Método: onNewToken
     *
     * Recibe un nuevo token FCM generado. Lo guarda en Firestore.
     *
     * @param token Nuevo token FCM generado para el dispositivo.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        guardarTokenEnFirestore(token)
    }
    /**
     * Método: guardarTokenEnFirestore
     *
     * Guarda el token FCM en el documento del usuario en Firestore.
     *
     * @param token Token FCM a guardar.
     */
    private fun guardarTokenEnFirestore(token: String) {
        val sessionManager = SessionManager(applicationContext)
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(sessionManager.getUserId().toString())
            .update("fcmTokens", FieldValue.arrayUnion(token))
            .addOnSuccessListener { Log.d("FCM", "Token guardado correctamente") }
            .addOnFailureListener { e -> Log.e("FCM", "Error al guardar token", e) }
    }
    /**
     * Método: onMessageReceived
     *
     * Recibe un mensaje push de Firebase Cloud Messaging y muestra notificación.
     *
     * @param remoteMessage Mensaje recibido de FCM.
     */
    @SuppressLint("MissingPermission")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Extraer datos
        val data    = remoteMessage.data
        val tipo    = data["tipo"]?.toIntOrNull()
        val from    = data["fromName"] ?: "Alguien"
        val list    = data["listName"] ?: ""
        val title: String
        val body: String

        // Elegir título y cuerpo según tipo
        when (tipo) {
            1 -> {
                title = "Nueva invitación"
                body  = "$from te ha compartido \"$list\""
            }
            2 -> {
                title = "Compra completada"
                body  = "$from ha completado la compra \"$list\""
            }
            else -> {
                // Fallback a payload notification si existe
                remoteMessage.notification?.let {
                    mostrarNotificacion(it.title ?: "", it.body ?: "")
                }
                return
            }
        }

        mostrarNotificacion(title, body)
        Log.d("FCM", "Notificación recibida: tipo=$tipo, from=$from, list=$list")
    }
    /**
     * Método: createNotificationChannel
     *
     * Crea un canal de notificaciones para Android Oreo y superior.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones ListApp",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Canal único para todas las notificaciones" }
            (getSystemService(NotificationManager::class.java))
                .createNotificationChannel(channel)
        }
    }
    /**
     * Método: mostrarNotificacion
     *
     * Construye y muestra una notificación con título y cuerpo dados.
     *
     * @param title Título de la notificación.
     * @param body  Texto del cuerpo de la notificación.
     */
    @SuppressLint("MissingPermission")
    private fun mostrarNotificacion(title: String, body: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
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
