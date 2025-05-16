package com.example.proyecto_dam_aritz_ayensa.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import com.google.firebase.messaging.FirebaseMessaging
/**
 * Clase: RegisterActivity
 *
 * Actividad para registrar usuario con validación y manejo de sesión.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var usuarioService: UsuarioService
    private lateinit var notificacionService: NotificacionService
    private lateinit var inputCorreo: EditText
    private lateinit var textoError: TextView
    private lateinit var inputContraseña: EditText
    private lateinit var sessionManager: SessionManager
    /**
     * onCreate:
     * Inicializa servicios y referencias de vistas.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        notificacionService = NotificacionService(NotificacionDAO())
        usuarioService = UsuarioService(UsuarioDAO(), notificacionService)
        inputCorreo = findViewById(R.id.register_et_usuario)
        inputContraseña = findViewById(R.id.register_et_contraUser)
        textoError = findViewById(R.id.texto_error)
        sessionManager = SessionManager(this)

    }
    /**
     * registrarUsuario:
     * Valida campos, crea usuario y guarda sesión.
     */
    fun registrarUsuario(view: View) {
        val email = inputCorreo.text.toString().trim()
        val password = inputContraseña.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            crearUsuario(email, password,
                onSuccess = { userId ->
                    val sessionManager = SessionManager(this)
                    sessionManager.saveUserSession(userId)
                    sessionManager.saveCredentials(email)


                    startActivity(Intent(this, BottomNavigationActivity::class.java))
                    finish()
                },
                onFailure = {
                    Log.e("RegisterActivity", "No se pudo registrar el usuario")
                }
            )
        } else {
            Utils.mostrarMensaje(this, "Ingrese el correo y la contraseña")
        }
    }
    /**
     * crearUsuario:
     * Valida email y contraseña, guarda usuario y maneja éxito o error.
     */
    @SuppressLint("SetTextI18n")
    fun crearUsuario(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        textoError.visibility = View.GONE
        if (!Utils.comprobarCorreo(email)) {
            Utils.mostrarMensaje(this, "Introduce un correo válido")
            onFailure(Exception("Correo inválido"))
            return
        } else {
            val lengthOk     = password.length >= 10
            val hasLowercase = password.any { it.isLowerCase() }
            val hasUppercase = password.any { it.isUpperCase() }
            val hasDigit     = password.any { it.isDigit() }

            if (!lengthOk || !hasLowercase || !hasUppercase || !hasDigit) {
                textoError.text = "La contraseña debe tener al menos 10 caracteres,1 minúscula, 1 mayúscula y 1 número"
                textoError.visibility = View.VISIBLE
                onFailure(Exception("Contraseña no válida"))
                return
            }
        }

        val nombrePorDefecto = email.substringBefore("@")

        usuarioService.saveUser(
            nombre = nombrePorDefecto,
            email = email,
            contraseña = password,
            FirebaseMessaging.getInstance().token.toString(),
            onSuccess = { userId ->
                Utils.mostrarMensaje(this, "Registro exitoso")
                onSuccess(userId.toString())
            }
        ) { exception ->
            Utils.mostrarMensaje(
                this,
                Utils.obtenerMensajesErrorEspañol(exception)
            )
            Log.e("UsuarioService", "Error al guardar el usuario", exception)
            onFailure(exception)
        }
    }

    /**
     * goToCancelar:
     * Cierra la actividad actual.
     */
    fun goToCancelar(view: View) {
        finish();
    }
}