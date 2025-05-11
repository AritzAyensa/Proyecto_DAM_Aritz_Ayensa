package com.example.proyecto_dam_aritz_ayensa.model.service
import android.net.Uri
import com.example.proyecto_dam_aritz_ayensa.model.dao.StorageDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import java.util.UUID


class StorageService(private val storageDAO: StorageDAO) {

    fun subirFotoPerfil(userId: String, imageUri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        return storageDAO.subirFotoPerfil(userId,imageUri , onSuccess, onFailure)
    }

    fun getFotoPerfilUrl(userId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        return storageDAO.getFotoPerfilUrl(userId, onSuccess, onFailure)
    }


}