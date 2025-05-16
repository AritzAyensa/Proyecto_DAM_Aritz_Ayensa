package com.example.proyecto_dam_aritz_ayensa.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthException

/**
 * Clase Utils
 *
 * Esta clase sirve para reutlizar métodos que se utlizan en otras clases.
 */
class Utils {
    companion object {
        /**
         * Método: mostrarMensaje
         *
         * Muestra un mensaje Toast en pantalla.
         *
         * @param context Contexto desde el que se llama.
         * @param mensaje Texto del mensaje a mostrar.
         */
        fun mostrarMensaje(context: Context?, mensaje: String?) {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }
        /**
         * Método: vibrator
         *
         * Activa la vibración del dispositivo durante 200 milisegundos.
         *
         * @param context Contexto desde el que se llama.
         */
        @RequiresApi(Build.VERSION_CODES.S)
        @SuppressLint("ServiceCast")
        fun vibrator(context: Context) {
            // 1. Obtener el Vibrator
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            // Vibración única de 200 ms a intensidad por defecto
            val effect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)

        }

        /**
         * Método: comprobarCorreo
         *
         * Valida si una dirección de correo electrónico tiene un formato correcto.
         *
         * @param email Correo electrónico a comprobar.
         * @return true si el correo es válido.
         */
        fun comprobarCorreo(email: String): Boolean {
        val emailRegex =
            "^(?!.*\\.\\.)(?!\\.)(?!.*\\.$)[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        return email.matches(emailRegex)
        }

        /**
         * Método: validarNombreUsuario
         *
         * Valida un nombre de usuario basado en una expresión regular.
         *
         * @param username Nombre de usuario a validar.
         * @return true si el nombre cumple con los requisitos (3-20 caracteres, letras/números/guiones).
         */
        fun validarNombreUsuario(username: String): Boolean {
            return username.matches("^[a-zA-Z0-9_-]{3,20}$".toRegex())
        }

        /**
         * Método: obtenerMensajesErrorEspañol
         *
         * Traduce excepciones de Firebase a mensajes de error en español.
         *
         * @param exception Excepción recibida.
         * @return Mensaje de error traducido.
         */
        fun obtenerMensajesErrorEspañol(exception: Exception?): String {
            return when {
                // 1. Intentar obtener el código de error directamente desde FirebaseAuthException
                exception is FirebaseAuthException -> {
                    translateFirebaseCode(exception.errorCode)
                }
                // 2. Buscar códigos de error en el mensaje (ej: "[ INVALID_LOGIN_CREDENTIALS ]")
                else -> {
                    val errorCode = extractErrorCodeFromMessage(exception?.message)
                    translateFirebaseCode(errorCode)
                }
            }
        }
        /**
         * Método: translateFirebaseCode
         *
         * Traduce un código de error de Firebase a un mensaje legible en español.
         *
         * @param errorCode Código de error proporcionado por Firebase.
         * @return Mensaje en español correspondiente al código.
         */
        fun translateFirebaseCode(errorCode: String?): String {
            return when (errorCode?.trim()) {
                "INVALID_LOGIN_CREDENTIALS" -> "Correo o contraseña incorrectos"
                "EMAIL_ALREADY_IN_USE" -> "El correo ya está registrado"
                "INVALID_EMAIL" -> "Correo electrónico inválido"
                "WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres"
                "USER_NOT_FOUND" -> "Usuario no encontrado"
                "WRONG_PASSWORD" -> "Contraseña incorrecta"
                "TOO_MANY_REQUESTS" -> "Demasiados intentos. Intenta más tarde"
                "NETWORK_REQUEST_FAILED" -> "Error de conexión a internet"
                "USER_DISABLED" -> "Cuenta deshabilitada"
                else -> "Error desconocido. Por favor, inténtalo de nuevo."
            }
        }
        /**
         * Método: extractErrorCodeFromMessage
         *
         * Extrae el código de error de un mensaje de excepción de Firebase.
         *
         * @param message Mensaje de la excepción.
         * @return Código de error extraído o null si no se encuentra.
         */
        private fun extractErrorCodeFromMessage(message: String?): String? {
            if (message.isNullOrEmpty()) return null
            val regex = Regex("\\[\\s*([A-Z_]+)\\s*]")
            return regex.find(message)?.groupValues?.get(1)
        }


    }

}