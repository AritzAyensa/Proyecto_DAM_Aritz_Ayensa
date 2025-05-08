package com.example.proyecto_dam_aritz_ayensa.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.HashUtil
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {
    private lateinit var usuarioService: UsuarioService
    private lateinit var notificacionService: NotificacionService
    private lateinit var sessionManager: SessionManager
    private lateinit var rememberCheck: CheckBox
    private lateinit var inputCorreo: EditText
    private lateinit var inputContra: EditText
    private val auth = FirebaseAuth.getInstance()
    private lateinit var usuario: Usuario


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        notificacionService = NotificacionService(NotificacionDAO())
        usuarioService = UsuarioService(UsuarioDAO(), notificacionService)

        // Verificar y solicitar permisos de notificación
        verificarYSolicitarPermisos()

        rememberCheck = findViewById(R.id.check_Remember)
        inputCorreo = findViewById(R.id.login_et_usuario)
        inputContra = findViewById(R.id.login_et_contraUser)
        sessionManager = SessionManager(this)
        println("Estado de REMEMBER_CHECK en inicio: ${sessionManager.isChecked()}")

        if (!sessionManager.isChecked()) {
            FirebaseAuth.getInstance().signOut()
        }
        if (sessionManager.isLoggedIn() && hayConexionInternet()) {
            val usuarioID: String? = sessionManager.getUserId()
            println("ID usuario = $usuarioID")
            startActivity(Intent(this@LoginActivity, BottomNavigationActivity::class.java))
            finish()
        } else if (sessionManager.isChecked()) {
            inputCorreo.setText(sessionManager.getUserEmail())
            inputContra.setText("")
            rememberCheck.isChecked = true
        }

        rememberCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val emailUser = inputCorreo.text.toString()
                val passwordUser = inputContra.text.toString()
                if (emailUser.isNotEmpty() && passwordUser.isNotEmpty()) {
                    sessionManager.saveCredentials(emailUser)
                } else {
                    Utils.mostrarMensaje(this, "Ingrese el correo y la contraseña")
                    rememberCheck.isChecked = false
                }
            } else {
                sessionManager.clearCredentials()
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

        if (!hayConexionInternet()) {
            Utils.mostrarMensaje(this, "No tienes conexión a Internet")
            return
        }

        val email = inputCorreo.text.toString().trim()
        val password = inputContra.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Utils.mostrarMensaje(this, "Ingrese correo y contraseña")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    /*if (auth.currentUser?.isEmailVerified == true) { // Verificar correo*/
                        cargarDatosUsuario(email) { usuarioObtenido ->
                            if (usuarioObtenido != null) {
                                usuario = usuarioObtenido
                                if (rememberCheck.isChecked) {
                                    sessionManager.saveUserSession(usuario.id)
                                } else {
                                    sessionManager.saveCredentials(usuario.email)
                                    sessionManager.clearSession()
                                }
                            } else {
                                Utils.mostrarMensaje(this, "Usuario no encontrado")
                            }
                        }
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result

                                usuarioService.añadirTokenAUsuario(sessionManager.getUserId().toString(), token,
                                    onSuccess = {
                                        Log.e("Login", "Token añadido")
                                    },
                                    onFailure = { e ->
                                        Log.e("Login", "Error al añadir el token FCM: ${e.message}")
                                    }
                                )
                            } else {
                                Log.e("Login", "No se pudo obtener el token FCM", task.exception)
                            }
                        }

                        // Opcional: Obtener datos adicionales desde Firestore
                        iniciarSesion()
                    /*} else {
                        Utils.mostrarMensaje(this, "Verifica tu correo electrónico")
                    }*/
                } else {

                    val error = task.exception?.message ?: "Error desconocido"
                    Log.e("Auth", "Error en Firebase: $error")
                    Utils.mostrarMensaje(this, Utils.obtenerMensajesErrorEspañol(task.exception)?: "Error al guardar el usuario")

                }
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
                    // comprobarCambioDePassword(email, password)
                } else {
                    Log.e("Auth", "Error en autenticación Firebase", authTask.exception)
                    Utils.mostrarMensaje(this, "Correo o contraseña incorrectos")
                }
            }
    }

    /*private fun comprobarCambioDePassword(email: String, password: String) {
        val hashedPassword = HashUtil.hashPassword(password)
        if (usuario.contrasena == hashedPassword) {
            Log.d("LOGIN", "El usuario no ha cambiado la contraseña")
        } else {
            usuarioService.updatePassword(usuario.id, hashedPassword)
            Log.d("LOGIN", "El usuario ha cambiado la contraseña")
        }
        iniciarSesion()
    }*/

    private fun iniciarSesion() {

        startActivity(Intent(this, BottomNavigationActivity::class.java))
        finish()
    }


    /**
     * PERMISO NOTIFICACIONES
     * Verificar y solicitar permisos de notificación
     */
    private fun verificarYSolicitarPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Si no se ha dado permiso, lo pide
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    1
                )
            }
        }
    }

    private fun hayConexionInternet(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
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

        if (requestCode == 1) {
            var cameraGranted = false
            var notificationsGranted = false

            for (i in permissions.indices) {
                when (permissions[i]) {
                    Manifest.permission.CAMERA -> {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            cameraGranted = true
                        } else {
                            Utils.mostrarMensaje(this, "Permiso de cámara denegado")
                        }
                    }
                    Manifest.permission.POST_NOTIFICATIONS -> {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            notificationsGranted = true
                        } else {
                            Utils.mostrarMensaje(this, "Permiso de notificaciones denegado")
                        }
                    }
                }
            }

            if (!cameraGranted && !notificationsGranted) {
                Utils.mostrarMensaje(this, "Se requieren permisos para continuar. La aplicación se cerrará.")
                finishAffinity() // Cierra todas las actividades y sale de la app
            }
        }
    }

}