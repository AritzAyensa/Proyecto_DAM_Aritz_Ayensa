package com.example.proyecto_dam_aritz_ayensa.utils

import java.security.MessageDigest

/**
 * Objeto HashUtil
 *
 * Este objeto proporciona utilidades para cifrar y verificar contraseñas utilizando el algoritmo SHA-256.
 * Es un objeto singleton, lo que significa que solo existe una instancia en toda la aplicación.
 */
object HashUtil {

    /**
     * Método: hashPassword
     *
     * Cifra una contraseña utilizando el algoritmo SHA-256.
     *
     * @param password Contraseña en texto plano que se desea cifrar.
     * @return Contraseña cifrada en formato hexadecimal, o una cadena vacía si ocurre un error.
     */
    fun hashPassword(password: String): String {
        return try {
            // Obtener una instancia del algoritmo SHA-256
            val digest = MessageDigest.getInstance("SHA-256")

            // Convertir la contraseña en un arreglo de bytes y cifrarla
            val hashBytes = digest.digest(password.toByteArray())

            // Convertir el arreglo de bytes cifrado a una cadena hexadecimal
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // Manejar cualquier excepción que ocurra durante el cifrado
            e.printStackTrace()
            "" // Devolver una cadena vacía en caso de error
        }
    }

    /**
     * Método: verifyPassword
     *
     * Compara una contraseña en texto plano con un hash para verificar si coinciden.
     *
     * @param password Contraseña en texto plano que se desea verificar.
     * @param hash Hash almacenado con el que se compara la contraseña.
     * @return `true` si la contraseña coincide con el hash, `false` en caso contrario.
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        // Cifrar la contraseña proporcionada
        val hashedPassword = hashPassword(password)

        // Comparar el hash generado con el hash almacenado
        return hashedPassword == hash
    }
}