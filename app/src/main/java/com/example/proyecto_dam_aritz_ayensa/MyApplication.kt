package com.example.proyecto_dam_aritz_ayensa

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var hasLostConnection = false

    override fun onCreate() {
        super.onCreate()

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Crear canal de notificaciones
        val channel = NotificationChannel(
            "invitaciones",
            "Invitaciones",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Canal para notificaciones de invitaciones"
            enableVibration(true)
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        // Registrar callback de red
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                // Evitamos múltiples disparos
                if (!hasLostConnection) {
                    hasLostConnection = true
                    // Mostrar mensaje al usuario
                    Handler(Looper.getMainLooper()).post {
                        Utils.mostrarMensaje(applicationContext, "Se ha perdido la conexión a Internet. Reiniciando la aplicación…")
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        restartApp()
                    }, 2000)
                }
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Restablece la bandera para futuras desconexiones
                hasLostConnection = false
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    private fun restartApp() {
        // Obtenemos el intent de lanzamiento principal
        val packageManager = packageManager
        val intent = packageManager
            .getLaunchIntentForPackage(packageName)
            ?.apply {
                // Flags para limpiar la pila y no duplicar la activity
                addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            }

        startActivity(intent)
    }

    override fun onTerminate() {
        super.onTerminate()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
