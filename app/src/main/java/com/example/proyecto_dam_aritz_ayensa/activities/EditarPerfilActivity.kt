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

    // Abre cámara o galería
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






    // Valida y guarda usuario + sube foto
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

    fun cancelar(view: View) {
        finish()
    }

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
