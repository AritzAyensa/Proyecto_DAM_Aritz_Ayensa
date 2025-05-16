package com.example.proyecto_dam_aritz_ayensa.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.StorageDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.model.service.StorageService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
/**
 * Clase: EditarPerfilActivity
 *
 * Actividad para editar el perfil del usuario, modificar nombre y foto de perfil.
 * Permite tomar foto con cámara o seleccionar imagen de galería.
 */
class EditarPerfilActivity : AppCompatActivity() {
    // Códigos de petición
    companion object {
        private const val REQ_CAMERA = 1001
        private const val REQ_GALLERY = 1002
        private const val REQ_PERM_CAMERA = 2001
    }

    private lateinit var sessionManager: SessionManager
    private lateinit var usuarioService: UsuarioService
    private lateinit var notificacionService: NotificacionService
    private lateinit var storageService: StorageService

    private lateinit var etNombre: EditText
    private lateinit var ivFotoPerfil: ImageView
    private lateinit var btnCancelar: Button
    private lateinit var btnGuardar: Button

    private var fotoBitmap: Bitmap? = null
    private var fotoUriSeleccionada: Uri? = null
    private lateinit var userId: String
    private lateinit var  takePictureLauncher : ActivityResultLauncher<Void?>
    private lateinit var  pickGalleryLauncher : ActivityResultLauncher<String?>
    /**
     * Método: onCreate
     *
     * Inicializa servicios, vistas, carga datos del usuario y registra launchers para cámara y galería.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        // Inicializar servicios y vistas
        sessionManager     = SessionManager(this)
        userId             = sessionManager.getUserId().toString()
        notificacionService = NotificacionService(NotificacionDAO())
        storageService     = StorageService(StorageDAO())
        usuarioService     = UsuarioService(UsuarioDAO(), notificacionService)

        etNombre    = findViewById(R.id.et_nombreUsuario)
        ivFotoPerfil= findViewById(R.id.fotoPerfil)
        btnCancelar = findViewById(R.id.btnCancelar)
        btnGuardar  = findViewById(R.id.btnGuardar)

        cargarDatosUsuario()
        // Contratos para tomar foto y elegir de galería
        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap: Bitmap? ->
            bitmap?.let {
                fotoBitmap = it
                ivFotoPerfil.setImageBitmap(it)
            }
        }

        pickGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                fotoUriSeleccionada = it
                Glide.with(this).load(it).into(ivFotoPerfil)
            }
        }

    }
    /**
     * Método: cargarDatosUsuario
     *
     * Obtiene y carga datos del usuario y su foto de perfil.
     */
    private fun cargarDatosUsuario() {
        usuarioService.getUser(userId,
            onSuccess = { usuario ->
                usuario?.let {
                    etNombre.setText(it.nombre)

                    // Cargar URL existente
                    storageService.getFotoPerfilUrl(it.id,
                        onSuccess = { url ->
                            Glide.with(this)
                                .load(url)
                                .placeholder(R.drawable.perfil)
                                .circleCrop()
                                .into(ivFotoPerfil)
                        },
                        onFailure = {
                            Log.e("StorageService", "No pudo descargar foto", it)
                        })
                }
            },
            onFailure = {
                Log.e("UserService", "Error al obtener usuario", it)
            })
    }
    /**
     * Método: cambiarFotoPerfil
     *
     * Muestra diálogo para seleccionar entre cámara o galería para cambiar foto.
     */
    fun cambiarFotoPerfil(view: View) {
        MaterialAlertDialogBuilder(this, R.style.MyDialogTheme)
            .setTitle("Seleccionar fuente")
            .setPositiveButton("Cámara") { dialog, _ ->
                if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), REQ_PERM_CAMERA)
                } else {
                    abrirCamara()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Galería") { dialog, _ ->
                abrirGaleria()
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Método: guardar
     *
     * Valida y actualiza nombre, sube la foto seleccionada o tomada y finaliza actividad.
     */
    fun guardar(view: View) {
        val nombre = etNombre.text.toString().trim()
        if (!Utils.validarNombreUsuario(nombre)) {
            Utils.mostrarMensaje(this, "Datos no válidos")
            return
        }

        // actualizar usuario…
        usuarioService.updateUser(Usuario(userId, nombre),
            onSuccess = {
                // subir foto solo al guardar
                fotoBitmap?.let { bmp ->
                    val f = File(cacheDir, "perfil.jpg")
                    FileOutputStream(f).use { out ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    storageService.subirFotoPerfil(userId, f.toUri(), {/*…*/}, {/*…*/})
                } ?: fotoUriSeleccionada?.let { uri ->
                    storageService.subirFotoPerfil(userId, uri, {/*…*/}, {/*…*/})
                }

                Utils.mostrarMensaje(this, "Perfil actualizado")
                finish()
            },
            onFailure = {
                Utils.mostrarMensaje(this, "Error al actualizar")
            })
    }
    /**
     * Método: cancelar
     *
     * Finaliza la actividad sin guardar cambios.
     */

    fun cancelar(view: View) {
        finish()
    }

    /**
     * Método: abrirCamara
     *
     * Inicia intent para tomar foto con cámara si hay app disponible.
     */
    private fun abrirCamara() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            // comprobar que exista app de cámara
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQ_CAMERA)
            } else {
                Utils.mostrarMensaje(this, "No hay aplicación de cámara")
            }
        }
    }
    /**
     * Método: abrirGaleria
     *
     * Inicia intent para seleccionar imagen de la galería.
     */
    private fun abrirGaleria() {
        Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).apply {
            type = "image/*"
        }.also {
            startActivityForResult(it, REQ_GALLERY)
        }
    }
    /**
     * Método: onRequestPermissionsResult
     *
     * Maneja resultado de permiso para cámara, abre cámara si es concedido.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PERM_CAMERA &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            abrirCamara()
        } else {
            Utils.mostrarMensaje(this, "Permiso de cámara denegado")
        }
    }
    /**
     * Método: onActivityResult
     *
     * Procesa resultado de cámara o galería y actualiza la imagen de perfil.
     */
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQ_CAMERA -> {
                // thumbnail en data.extras["data"]
                fotoBitmap = data?.extras?.get("data") as? Bitmap
                ivFotoPerfil.setImageBitmap(fotoBitmap)
            }
            REQ_GALLERY -> {
                data?.data?.let { uri ->
                    fotoUriSeleccionada = uri
                    ivFotoPerfil.setImageURI(uri)
                }
            }
        }
    }

}
