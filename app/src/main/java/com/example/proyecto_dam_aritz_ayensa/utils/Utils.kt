package com.example.proyecto_dam_aritz_ayensa.utils

import android.content.Context
import android.widget.Toast

/**
 * Clase Utils
 *
 * Esta clase sirve para reutlizar métodos que se utlizan en otras clases.
 */
class Utils {
    companion object {

        fun mostrarMensaje(context: Context?, mensaje: String?) {
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
        }


        fun comprobarCorreo(email: String): Boolean {
            val emailRegex =
                "^(?!.*\\.\\.)(?!\\.)(?!.*\\.$)[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
            return email.matches(emailRegex)
        }

        /*fun comprobarContraseña(contraseña: String): Boolean {
            val contraseñaRegex =
                "^(?!.*\\.\\.)(?!\\.)(?!.*\\.$)[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
            return contraseña.matches(contraseñaRegex)
        }*/


        fun validarNombreUsuario(username: String): Boolean {
            return username.matches("^[a-zA-Z0-9_-]{3,20}$".toRegex())
        }
    }

}