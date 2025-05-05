package com.example.proyecto_dam_aritz_ayensa.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.proyecto_dam_aritz_ayensa.activities.LoginActivity

/**
 * Clase SessionManager
 *
 * Esta clase gestiona la sesión del usuario utilizando SharedPreferences para almacenar y recuperar datos
 * relacionados con la autenticación y las credenciales del usuario.
 *
 * @property prefs Instancia de SharedPreferences para acceder a los datos almacenados.
 */
class SessionManager(context: Context) {

    // Instancia de SharedPreferences para almacenar y recuperar datos de la sesión del usuario
    private val prefs: SharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    /**
     * Método: saveUserSession
     *
     * Guarda la sesión del usuario almacenando su ID y marcando que ha iniciado sesión.
     *
     * @param userId ID del usuario que ha iniciado sesión.
     */
    fun saveUserSession(userId: String) {
        prefs.edit().apply {
            putString("USER_ID", userId) // Guardar el ID del usuario
            putBoolean("IS_LOGGED_IN", true) // Marcar que el usuario ha iniciado sesión
            apply() // Guardar los cambios de manera asíncrona
        }
    }

    /**
     * Método: isLoggedIn
     *
     * Verifica si el usuario ha iniciado sesión.
     *
     * @return `true` si el usuario ha iniciado sesión, `false` en caso contrario.
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false) // Obtener el estado de la sesión
    }

    /**
     * Método: getUserId
     *
     * Obtiene el ID del usuario que ha iniciado sesión.
     *
     * @return ID del usuario o `null` si no ha iniciado sesión.
     */
    fun getUserId(): String? {
        return prefs.getString("USER_ID", null) // Obtener el ID del usuario
    }

    /**
     * Método: logout
     *
     * Cierra la sesión del usuario eliminando su ID y marcando que ha cerrado sesión.
     */
    fun logout() {
        prefs.edit().apply {
            remove("USER_ID") // Eliminar el ID del usuario
            remove("IS_LOGGED_IN") // Marcar que el usuario ha cerrado sesión
            apply() // Guardar los cambios de manera asíncrona
        }
    }

    /**
     * Método: saveCredentials
     *
     * Guarda las credenciales del usuario (email y contraseña) y marca que se deben recordar.
     *
     * @param userEmail Email del usuario.
     * @param userPassword Contraseña del usuario.
     */
    fun saveCredentials(userEmail: String) {
        prefs.edit().apply {
            putString("USER_EMAIL", userEmail)
            putBoolean("REMEMBER_CHECK", true)
            apply()
        }
    }

    /**
     * Método: clearCredentials
     *
     * Elimina las credenciales del usuario y marca que no se deben recordar.
     */
    fun clearCredentials() {
        prefs.edit().apply {
            remove("USER_EMAIL")
            putBoolean("REMEMBER_CHECK", false)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().apply {
            putBoolean("REMEMBER_CHECK", false)
            apply()
        }
    }

    /**
     * Método: isChecked
     *
     * Verifica si se deben recordar las credenciales del usuario.
     *
     * @return `true` si se deben recordar las credenciales, `false` en caso contrario.
     */
    fun isChecked(): Boolean {
        println("📌 Estado de REMEMBER_CHECK = ${prefs.getBoolean("REMEMBER_CHECK", false)}") // Log para depuración
        return prefs.getBoolean("REMEMBER_CHECK", false) // Obtener el estado de recordar credenciales
    }

    /**
     * Método: getUserEmail
     *
     * Obtiene el email del usuario almacenado.
     *
     * @return Email del usuario o `null` si no está almacenado.
     */
    fun getUserEmail(): String? {
        return prefs.getString("USER_EMAIL", null) // Obtener el email del usuario
    }

}