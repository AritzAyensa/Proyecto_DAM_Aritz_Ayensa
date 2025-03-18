package com.example.proyecto_dam_aritz_ayensa.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_dam_aritz_ayensa.MainActivity
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.HashUtil
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager
    private lateinit var rememberCheck: CheckBox
    private lateinit var inputCorreo: EditText
    private lateinit var inputContra: EditText
    private val auth = FirebaseAuth.getInstance()
    private lateinit var usuario: Usuario


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        usuarioService = UsuarioService(UsuarioDAO())

        // Verificar y solicitar permisos de notificación
        verificarYSolicitarPermisos()

        rememberCheck = findViewById(R.id.check_Remember)
        inputCorreo = findViewById(R.id.login_et_usuario)
        inputContra = findViewById(R.id.login_et_contraUser)
        usuarioService = UsuarioService(UsuarioDAO())
        sessionManager = SessionManager(this)
        println("Estado de REMEMBER_CHECK en inicio: ${sessionManager.isChecked()}")

        if (sessionManager.isLoggedIn()) {
            val usuarioID: String? = sessionManager.getUserId()
            println("ID usuario = $usuarioID")
            startActivity(Intent(this@LoginActivity, BottomNavigationActivity::class.java))
            finish()
        } else if (sessionManager.isChecked()) {
            inputCorreo.setText(sessionManager.getUserEmail())
            inputContra.setText(sessionManager.getUserPassword())
            rememberCheck.isChecked = true
        }

        rememberCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val emailUser = inputCorreo.text.toString()
                val passwordUser = inputContra.text.toString()
                if (emailUser.isNotEmpty() && passwordUser.isNotEmpty()) {
                    sessionManager.saveCredentials(emailUser, passwordUser)
                    println("Credenciales guardadas")
                } else {
                    Utils.mostrarMensaje(this, "Ingrese el correo y la contraseña")
                    rememberCheck.isChecked = false
                }
            } else {
                sessionManager.clearCredentials()
                println("Credenciales eliminadas")
            }
        }
    }


    fun goToRegister(view: View?) {
        // Crear el Intent para abrir la actividad de registro
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(intent) // Inicia la actividad de registro
    }


    fun goToForgotPassword(view: View?) {
        // Crear el Intent para abrir la actividad de registro
        val intent = Intent(this@LoginActivity, CambiarContrasenaActivity::class.java)
        startActivity(intent) // Inicia la actividad de registro
    }


    fun goToMainDesdeLogin(view: View?) {
        val textCorreoUser = inputCorreo.text.toString()
        val textContraUser = inputContra.text.toString()

        if (textCorreoUser.isNotEmpty() && textContraUser.isNotEmpty()) {
            cargarDatosUsuario(textCorreoUser) { usuarioObtenido ->
                if (usuarioObtenido != null) {
                    usuario = usuarioObtenido
                    verificarCredencialesFirebase(textCorreoUser, textContraUser)
                } else {
                    Utils.mostrarMensaje(this, "Usuario no encontrado")
                }
            }
        } else {
            Utils.mostrarMensaje(this, "Ingrese el correo o contraseña")
        }
    }

    private fun cargarDatosUsuario(email: String, callback: (Usuario?) -> Unit) {
        usuarioService.getUserByEmail(
            email,
            onSuccess = { usuarioObtenido ->
                callback(usuarioObtenido)
            },
            onFailure = { exception ->
                Log.e("UserService", "Error al obtener el usuario", exception)
                callback(null) // Devolvemos null en caso de error
            }
        )
    }


    private fun verificarCredencialesFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    Log.d(
                        "Auth",
                        "Credenciales correctas en Firebase, verificando cambio de contraseña..."
                    )

                    // Verificamos si el usuario cambió su contraseña
                    comprobarCambioDePassword(email, password)
                } else {
                    Log.e("Auth", "Error en autenticación Firebase", authTask.exception)
                    Utils.mostrarMensaje(this, "Correo o contraseña incorrectos")
                }
            }
    }

    private fun comprobarCambioDePassword(email: String, password: String) {
        val hashedPassword = HashUtil.hashPassword(password)
        if (usuario.contrasena == hashedPassword) {
            Log.d("LOGIN", "El usuario no ha cambiado la contraseña")
        } else {
            usuarioService.updatePassword(usuario.id, hashedPassword)
            Log.d("LOGIN", "El usuario ha cambiado la contraseña")
        }
        iniciarSesion()
    }

    private fun iniciarSesion() {
        startActivity(Intent(this, MainActivity::class.java))
        sessionManager.saveUserSession(usuario.id)
        finish()
    }


    /**
     * PERMISO NOTIFICACIONES
     * Verificar y solicitar permisos de notificación
     */
    private fun verificarYSolicitarPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Verifica que la version de Android sea 33 o superior (en versiones anteriores no se dan permisos)
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Si no se ha dado permiso, lo pide
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1
                )
            }
        }
    }

    /**
     * PERMISO NOTIFICACIONES
     * Manejo de la respuesta al solicitar permisos
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) { // Código de solicitud de permiso
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, ahora puedes enviar notificaciones
                Utils.mostrarMensaje(this, "Permiso concedido")
            } else {
                // Permiso denegado, informa al usuario
                Utils.mostrarMensaje(this, "Permiso para notificaciones denegado")
            }
        }
    }
}