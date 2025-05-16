package com.example.proyecto_dam_aritz_ayensa.model.dao


import android.net.Uri
import android.util.Log
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.utils.HashUtil
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * DAO para gestionar operaciones de almacenamiento en Firebase Storage.
 */
class StorageDAO {

    // Instancia de Firestore para interactuar con la base de datos
    private val fs = FirebaseStorage.getInstance()


    /**
     * Método: subirFotoPerfil
     *
     * Sube una imagen de perfil para un usuario específico a Firebase Storage.
     *
     * @param userId ID del usuario al que pertenece la foto de perfil.
     * @param imageUri URI de la imagen a subir.
     * @param onSuccess Función callback que recibe la URL de descarga si la subida es exitosa.
     * @param onFailure Función callback que recibe la excepción si ocurre un error.
     */
    fun subirFotoPerfil(userId: String, imageUri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef = fs.reference
        val imageRef = storageRef.child("profile_images/$userId.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl
                    .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
                    .addOnFailureListener { e -> onFailure(e) }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    /**
     * Método: getFotoPerfilUrl
     *
     * Obtiene la URL de la foto de perfil de un usuario desde Firebase Storage.
     *
     * @param userId ID del usuario cuya foto de perfil se desea obtener.
     * @param onSuccess Función callback que recibe la URL de descarga si la operación es exitosa.
     * @param onFailure Función callback que recibe la excepción si ocurre un error.
     */
    fun getFotoPerfilUrl(userId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef = fs.reference
        val imageRef = storageRef.child("profile_images/$userId.jpg")

        imageRef.downloadUrl
            .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
            .addOnFailureListener { e -> onFailure(e) }
    }

}