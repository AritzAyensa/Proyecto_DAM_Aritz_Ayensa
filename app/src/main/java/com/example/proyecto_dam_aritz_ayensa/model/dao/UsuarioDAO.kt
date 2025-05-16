package com.example.proyecto_dam_aritz_ayensa.model.dao


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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class UsuarioDAO {

    private val db = FirebaseFirestore.getInstance()

    private val auth = FirebaseAuth.getInstance()

    private val usuariosCollection = db.collection("usuarios")


/**
 * Método: saveUser
 *
 * Crea un usuario en FirebaseAuth y registra sus datos en Firestore.
 * La contraseña se almacena hasheada y se añade un token FCM inicial.
 *
 * @param nombre Nombre del usuario.
 * @param email Email del usuario.
 * @param contraseña Contraseña en texto plano.
 * @param token Token FCM para notificaciones push.
 * @param onSuccess Callback con el ID del usuario si la operación es exitosa.
 * @param onFailure Callback con la excepción si ocurre un error.
 */
    fun saveUser(
        nombre: String,
        email: String,
        contraseña: String,
        token: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, contraseña)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val userId = user?.uid

                    val hashedPassword = HashUtil.hashPassword(contraseña)

                    val usuarioData = hashMapOf(
                        "id" to userId,
                        "nombre" to nombre,
                        "email" to email,
                        "idListas" to listOf<String>(),
                        "idListasCompartidas" to listOf<String>(),
                        "idsNotificaciones" to mapOf<String, Boolean>(),
                        "fcmTokens" to listOf(token)
                    )

                    if (userId != null) {
                        usuariosCollection.document(userId).set(usuarioData)
                            .addOnSuccessListener {
                                Log.d("UsuarioService", "Usuario registrado con éxito")
                                onSuccess(userId)
                            }
                            .addOnFailureListener { e ->
                                user.delete()
                                onFailure(e)
                            }
                    } else {
                        onFailure(NullPointerException("User ID is null"))
                    }
                } else {
                    onFailure(task.exception ?: Exception("Error desconocido"))
                }
            }
    }
    /**
     * Método: añadirTokenAUsuario
     *
     * Añade un token FCM a la lista de tokens del usuario si no existe ya.
     *
     * @param userId ID del usuario.
     * @param token Token FCM a añadir.
     * @param onSuccess Callback si la operación es exitosa.
     * @param onFailure Callback con la excepción si ocurre un error.
     */

    fun añadirTokenAUsuario(
        userId: String,
        token: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userDoc = usuariosCollection.document(userId)
        userDoc.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val tokens = document.get("fcmTokens") as? List<*> ?: emptyList<Any>()
                    if (!tokens.contains(token)) {
                        userDoc.update("fcmTokens", FieldValue.arrayUnion(token))
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onFailure(e) }
                    } else {
                        // Ya lo tiene, no hace falta actualizar
                        onSuccess()
                    }
                } else {
                    onFailure(Exception("Usuario no encontrado"))
                }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }


    /**
     * Método: eliminarFcmToken
     *
     * Elimina un token FCM del usuario.
     *
     * @param uid ID del usuario.
     * @param token Token FCM a eliminar.
     * @param onSuccess Callback si la operación es exitosa.
     * @param onFailure Callback con la excepción si ocurre un error.
     */
    fun eliminarFcmToken(uid: String, token: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
            usuariosCollection.document(uid).update("fcmTokens", FieldValue.arrayRemove(token))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
    /**
     * Método: getUserIdsByListId
     *
     * Obtiene IDs de usuarios que tengan una lista propia o compartida específica.
     *
     * @param listId ID de la lista a buscar.
     * @return Lista con IDs de usuarios que contienen esa lista.
     */
    suspend fun getUserIdsByListId(listId: String): List<String> {


        // Consulta 1: usuarios donde idListas contiene listId
        val snap1 = usuariosCollection
            .whereArrayContains("idListas", listId)
            .get()
            .await()

        // Consulta 2: usuarios donde idListasCompartidas contiene listId
        val snap2 = usuariosCollection
            .whereArrayContains("idListasCompartidas", listId)
            .get()
            .await()

        val ids = mutableSetOf<String>()
        (snap1.documents + snap2.documents).forEach { doc ->
            doc.id.let { ids.add(it) }
        }
        return ids.toList()
    }



    /**
     * Método: getMisListasSizeByIdUsuario
     *
     * Obtiene el número de listas propias de un usuario.
     *
     * @param idUsuario ID del usuario.
     * @return Número de listas propias o -1 si hay error.
     */
    suspend fun getMisListasSizeByIdUsuario(idUsuario: String): Int {
        return try {
            val document = usuariosCollection
                .document(idUsuario)
                .get()
                .await()

            if (document.exists()) {
                val idListas = document.get("idListas") as? List<String>
                idListas?.size ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener tamaño de listas compartidas", e)
            -1
        }
    }

    /**
     * Método: añadirNotificacionAUsuarios
     *
     * Añade una notificación (con estado no leída) a múltiples usuarios.
     *
     * @param idsUsuarios Lista de IDs de usuarios.
     * @param idNotificacion ID de la notificación a añadir.
     */
    suspend fun añadirNotificacionAUsuarios(
        idsUsuarios: List<String>,
        idNotificacion: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val usuariosCollection = db.collection("usuarios")

        for (idUsuario in idsUsuarios) {
            val usuarioRef = usuariosCollection.document(idUsuario)

            usuarioRef.update("idsNotificaciones.$idNotificacion", false).await()
        }
    }
    /**
     * Método: eliminarNotificacionesDeUsuarios
     *
     * Elimina notificaciones específicas de un usuario.
     *
     * @param idUsuario ID del usuario.
     * @param idsNotificaciones Lista de IDs de notificaciones a eliminar.
     */
    suspend fun eliminarNotificacionesDeUsuarios(idUsuario : String, idsNotificaciones: List<String>) {
        val usuarioRef = usuariosCollection.document(idUsuario)

        for (idNotificacion in idsNotificaciones) {
            usuarioRef.update("idsNotificaciones.$idNotificacion", FieldValue.delete()).await()
        }

    }

    /**
     * Método: marcarNotificacionesComoLeidas
     *
     * Marca como leídas las notificaciones indicadas para un usuario.
     *
     * @param idUsuario ID del usuario.
     * @param idsNotificaciones Lista de IDs de notificaciones a marcar como leídas.
     */
    suspend fun marcarNotificacionesComoLeidas(idUsuario: String, idsNotificaciones: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val usuarioRef = db.collection("usuarios").document(idUsuario)

        val updates = idsNotificaciones.associate { "idsNotificaciones.$it" to true }
        usuarioRef.update(updates).await()
    }

    /**
     * Método: notificacionesSinLeerCountFlow
     *
     * Retorna un flujo que emite el número de notificaciones no leídas de un usuario en tiempo real.
     *
     * @param userId ID del usuario.
     * @return Flow con el conteo de notificaciones sin leer.
     */
    fun notificacionesSinLeerCountFlow(userId: String): Flow<Int> = callbackFlow {
        val docRef = usuariosCollection.document(userId)

        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else if (snapshot != null && snapshot.exists()) {
                val notificacionesMap = snapshot.get("idsNotificaciones") as? Map<*, *>
                val noLeidas = notificacionesMap?.values?.count { it == false } ?: 0
                trySend(noLeidas).isSuccess
            }
        }

        awaitClose { registration.remove() }
    }
    /**
     * Método: notificacionesUsuarioFlow
     *
     * Retorna un flujo que emite la lista completa de notificaciones del usuario en tiempo real.
     *
     * @param userId ID del usuario.
     * @param notificacionService Servicio para obtener datos de notificaciones.
     * @return Flow con lista de objetos Notificacion.
     */
    fun notificacionesUsuarioFlow(
        userId: String,
        notificacionService: NotificacionService
    ): Flow<List<Notificacion>> = callbackFlow {
        val docRef = usuariosCollection.document(userId)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val map = snapshot.get("idsNotificaciones") as? Map<*, *>
                val ids = map?.keys?.filterIsInstance<String>() ?: emptyList()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val notificaciones = notificacionService.getNotificacionesByIds(ids)
                        trySend(notificaciones).isSuccess
                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }
        }

        awaitClose { listener.remove() }
    }


    /**
     * Método: getUser
     *
     * Obtiene un usuario por su ID.
     *
     * @param usuarioID ID del usuario a obtener.
     * @param onSuccess Callback con el objeto Usuario o null si no existe.
     * @param onFailure Callback con la excepción si ocurre un error.
     */
    fun getUser(usuarioID: String, onSuccess: (Usuario?) -> Unit, onFailure: (Exception) -> Unit) {
        usuariosCollection.document(usuarioID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: ""
                    val email = document.getString("email") ?: ""

                    val usuario = Usuario(id = usuarioID, nombre = nombre, email = email)
                    onSuccess(usuario)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    /**
     * Método: getUserNameById
     *
     * Obtiene el nombre de un usuario dado su ID.
     *
     * @param usuarioID ID del usuario.
     * @return Nombre del usuario o null si ocurre un error.
     */
    suspend fun getUserNameById(usuarioID: String): String? {
        return try {
            val document = usuariosCollection.document(usuarioID).get().await()
            document.getString("nombre")
        } catch (e: Exception) {
            Log.e("UsuarioDAO", "Error al obtener nombre de usuario $usuarioID", e)
            null
        }
    }

    /**
     * Método: updateUser
     *
     * Actualiza los datos básicos del usuario (solo nombre).
     *
     * @param usuario Objeto Usuario con datos actualizados.
     * @param onSuccess Callback si la actualización fue exitosa.
     * @param onFailure Callback con la excepción si ocurre un error.
     */
    fun updateUser(usuario: Usuario, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val usuarioData = hashMapOf(
            "nombre" to usuario.nombre
        )

        usuariosCollection.document(usuario.id).update(usuarioData as Map<String, Any>)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    /**
     * Método: añadirLista
     *
     * Añade una lista propia al usuario.
     *
     * @param idLista ID de la lista.
     * @param usuarioId ID del usuario.
     */
    suspend fun añadirLista(idLista: String, usuarioId: String) {
        usuariosCollection
            .document(usuarioId)
            .update("idListas", FieldValue.arrayUnion(idLista))
            .await() // Espera a que la operación termine
    }
    /**
     * Método: añadirListaCompartida
     *
     * Añade una lista compartida al usuario.
     *
     * @param idListaCompartida ID de la lista compartida.
     * @param usuarioId ID del usuario.
     */

    suspend fun añadirListaCompartida(idListaCompartida: String, usuarioId: String) {
        usuariosCollection
            .document(usuarioId)
            .update("idListasCompartidas", FieldValue.arrayUnion(idListaCompartida))
            .await()
    }

    /**
     * Método: eliminarLista
     *
     * Elimina una lista propia del usuario.
     *
     * @param idLista ID de la lista.
     * @param usuarioId ID del usuario.
     */
    suspend fun eliminarLista(idLista: String, usuarioId: String) {
        usuariosCollection
            .document(usuarioId)
            .update("idListas", FieldValue.arrayRemove(idLista))
            .await() // Espera a que la operación termine
    }
    /**
     * Método: eliminarListaCompartida
     *
     * Elimina una lista compartida del usuario.
     *
     * @param idListaCompartida ID de la lista compartida.
     * @param usuarioId ID del usuario.
     */
    suspend fun eliminarListaCompartida(idListaCompartida: String, usuarioId: String) {
        usuariosCollection
            .document(usuarioId)
            .update("idListasCompartidas", FieldValue.arrayRemove(idListaCompartida))
            .await()
    }
    /**
     * Método: eliminarListaCompartidaDeUsuarios
     *
     * Elimina una lista compartida de todos los usuarios que la tengan.
     *
     * @param idListaCompartida ID de la lista compartida a eliminar.
     */
    suspend fun eliminarListaCompartidaDeUsuarios(idListaCompartida: String) {
        val usuariosSnapshot = usuariosCollection.get().await()
        for (document in usuariosSnapshot.documents) {
            document.reference.update("idListasCompartidas", FieldValue.arrayRemove(idListaCompartida)).await()
        }
    }

    /**
     * Método: getIdMisListasByIdUsuario
     *
     * Obtiene las IDs de las listas propias de un usuario.
     *
     * @param idUsuario ID del usuario.
     * @return Lista de IDs de listas propias.
     */
    suspend fun getIdMisListasByIdUsuario(idUsuario: String) : List<String> {
        val document = usuariosCollection
            .document(idUsuario)
            .get()
            .await()

        return if (document.exists()) {
            document.get("idListas") as? List<String> ?: emptyList()
        } else {
            emptyList()
        }
    }
    /**
     * Método: getIdListasCompartidasByIdUsuario
     *
     * Obtiene las IDs de las listas compartidas de un usuario.
     *
     * @param idUsuario ID del usuario.
     * @return Lista de IDs de listas compartidas.
     */
    suspend fun getIdListasCompartidasByIdUsuario(idUsuario: String) : List<String>{
        val document = usuariosCollection
            .document(idUsuario)
            .get()
            .await()

        return if (document.exists()) {
            document.get("idListasCompartidas") as? List<String> ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Método: getUserByEmail
     *
     * Busca un usuario por su email.
     *
     * @param usuarioEmail Email del usuario a buscar.
     * @param onSuccess Callback con el Usuario encontrado o null si no existe.
     * @param onFailure Callback con la excepción si ocurre un error.
     */
    fun getUserByEmail(
        usuarioEmail: String,
        onSuccess: (Usuario?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usuariosCollection
            .whereEqualTo("email", usuarioEmail)
            .get()
            .addOnSuccessListener { qs ->
                val doc = qs.documents.firstOrNull()
                if (doc == null) {
                    onSuccess(null)
                } else {
                    // Firestore rellena automáticamente todos los campos
                    val usuario = doc.toObject(Usuario::class.java)
                        // inyecta el id del doc en la propiedad id
                        ?.copy(id = doc.id)
                    onSuccess(usuario)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

}