package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils

class EditarPerfilActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var usuarioService: UsuarioService

    // Datos del usuario
    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText

    private lateinit var userId : String

    // Botones
    private lateinit var btnCancelar: Button
    private lateinit var btnGuardar: Button

    // Datos
    private lateinit var nombre: String
    private lateinit var email: String
    private lateinit var contrasena: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)
        usuarioService = UsuarioService(UsuarioDAO())
        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId().toString()

        // Datos del usuario
        etNombre = findViewById(R.id.et_nombreUsuario)
        etCorreo = findViewById(R.id.et_CorreoUsuario)

        // Botones
        btnCancelar = findViewById(R.id.btnCancelar)
        btnGuardar = findViewById(R.id.btnGuardar)

        // Cargar datos del usuario en la vista
        cargarDatosUsuario()



        nombre = etNombre.text.toString().trim()
        email = etCorreo.text.toString().trim()
    }



    private fun validarDatos(): Int {
        nombre = etNombre.text.toString().trim()
        email = etCorreo.text.toString().trim()

        val esNombreValido = Utils.validarNombreUsuario(nombre)
        val esCorreoValido = Utils.comprobarCorreo(email)

        if (!esNombreValido)
            Utils.mostrarMensaje(this, "El usuario no es válido")
        if (!esCorreoValido)
            Utils.mostrarMensaje(this, "El correo electrónico no es válido")

        if (esNombreValido && esCorreoValido) {
            return 2
        } else {
            return -1
        }
    }
    private fun cargarDatosUsuario() {
        val usuario = usuarioService.getUser(userId,
            onSuccess = { usuario ->
                if (usuario != null) {
                    Log.d("UserService", "Usuario obtenido: ${usuario.nombre}")
                    val userId = usuario.id
                    etNombre.setText(usuario.nombre)
                    etCorreo.setText(usuario.email)
                }
            },
            onFailure = { exception ->
                Log.e("UserService", "Error al obtener el usuario", exception)
            })
    }

    fun guardar(view: View) {
        if (validarDatos() == 2) {
            usuarioService.getUserByEmail(email,
                onSuccess = { usuarioExistente ->
                    if (usuarioExistente != null && usuarioExistente.id != userId) {
                        Utils.mostrarMensaje(this, "El correo ya está registrado por otro usuario")
                    } else {
                        val usuario = Usuario(userId, nombre, email)
                        usuarioService.updateUser(usuario,
                            onSuccess = {
                                Utils.mostrarMensaje(this, "Datos Guardados")
                                finish()
                            },
                            onFailure = {
                                Utils.mostrarMensaje(this, "Error, Inténtelo más tarde")
                            }
                        )
                    }
                },
                onFailure = {
                    Utils.mostrarMensaje(this, "Error al verificar el correo. Intente más tarde")
                }
            )
        }
    }

    fun cancelar(view: View) {
        finish()
    }
}