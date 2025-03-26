package com.example.proyecto_dam_aritz_ayensa.activities

import android.content.Intent
import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.utils.Utils

class RegisterActivity : AppCompatActivity() {

    private lateinit var usuarioService: UsuarioService
    private lateinit var inputCorreo: EditText
    private lateinit var inputContraseña: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usuarioService = UsuarioService(UsuarioDAO())
        inputCorreo = findViewById(R.id.register_et_usuario)
        inputContraseña = findViewById(R.id.register_et_contraUser)

    }
    fun registrarUsuario(view: View) {
        val textCorreoUser = inputCorreo.text.toString().trim()

        val textContraUser = inputContraseña.text.toString().trim()

        if (textCorreoUser.isNotEmpty() && textContraUser.isNotEmpty()) {
            crearUsuario(textCorreoUser, textContraUser,
                onSuccess = {
                    finish()
                },
                onFailure = {
                    // No se hace nada porque el mensaje de error ya se mostró
                    Log.e("RegisterActivity", "No se pudo registrar el usuario")
                }
            )
        } else {
            Utils.mostrarMensaje(this,"Ingrese el correo o contraseña")
        }

    }


    fun crearUsuario(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (!Utils.comprobarCorreo(email)) {
            Utils.mostrarMensaje(this, "Introduce un correo válido")
            onFailure(Exception("Correo inválido"))
            return
        } else if (password.length < 6) {
            Utils.mostrarMensaje(this, "La contraseña debe tener al menos 6 caracteres")
            onFailure(Exception("Contraseña corta"))
            return
        }

        val emailStrip = email.split("@")
        val nombrePorDefecto = emailStrip[0]

        usuarioService.saveUser(
            nombrePorDefecto, email,password,
            onSuccess = {
                Log.d("UsuarioService", "Usuario guardado correctamente")
                Utils.mostrarMensaje(this, "Registro exitoso. Ahora puedes iniciar sesión.")
                onSuccess()
            },
            onFailure = { exception ->
                Utils.mostrarMensaje(this, exception.message ?: "Error al guardar el usuario")
                Log.e("UsuarioService", "Error al guardar el usuario", exception)
                onFailure(exception)
            }
        )
    }

    fun goToCancelar(view: View) {
        finish();
    }
}