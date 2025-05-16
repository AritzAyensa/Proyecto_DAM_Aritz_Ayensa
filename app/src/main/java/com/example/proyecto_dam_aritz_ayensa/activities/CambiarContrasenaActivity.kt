package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import com.google.firebase.auth.FirebaseAuth
/**
 * Clase: CambiarContrasenaActivity
 *
 * Permite al usuario solicitar un correo para restablecer su contraseña.
 * Utiliza Firebase Authentication para enviar el enlace de restablecimiento.
 */
class CambiarContrasenaActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var sendResetLinkButton: Button

    /**
     * Método: onCreate
     *
     * Inicializa la actividad y configura el listener del botón.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cambiar_contrasena)

        // Inicializar componentes de la UI
        emailEditText = findViewById(R.id.emailEditText)
        sendResetLinkButton = findViewById(R.id.sendResetLinkButton)

        // Configurar listener para el botón
        sendResetLinkButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                Utils.mostrarMensaje(this, "Por favor ingrese un correo electrónico válido.")
            }
        }
    }
    /**
    * Método: sendPasswordResetEmail
    *
    * Envía un correo de restablecimiento de contraseña utilizando FirebaseAuth.
    */
    private fun sendPasswordResetEmail(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utils.mostrarMensaje(this, "Se ha enviado el correo de restablecimiento.")
                } else {
                    Utils.mostrarMensaje(this, "Hubo un error al enviar el correo.")
                }
            }
    }
}