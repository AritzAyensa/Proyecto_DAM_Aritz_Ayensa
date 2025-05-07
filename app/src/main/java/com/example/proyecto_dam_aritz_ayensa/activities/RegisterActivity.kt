package com.example.proyecto_dam_aritz_ayensa.activities

import android.content.Intent
import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import com.google.firebase.messaging.FirebaseMessaging

class RegisterActivity : AppCompatActivity() {

    private lateinit var usuarioService: UsuarioService
    private lateinit var notificacionService: NotificacionService
    private lateinit var inputCorreo: EditText
    private lateinit var inputContraseña: EditText
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        notificacionService = NotificacionService(NotificacionDAO())
        usuarioService = UsuarioService(UsuarioDAO(), notificacionService)
        inputCorreo = findViewById(R.id.register_et_usuario)
        inputContraseña = findViewById(R.id.register_et_contraUser)
        sessionManager = SessionManager(this)

    }
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

    fun crearUsuario(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (!Utils.comprobarCorreo(email)) {
            Utils.mostrarMensaje(this, "Introduce un correo válido")
            onFailure(Exception("Correo inválido"))
            return
        } else if (password.length < 6) {
            Utils.mostrarMensaje(this, "La contraseña debe tener al menos 6 caracteres")
            onFailure(Exception("Contraseña corta"))
            return
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


    fun goToCancelar(view: View) {
        finish();
    }
}