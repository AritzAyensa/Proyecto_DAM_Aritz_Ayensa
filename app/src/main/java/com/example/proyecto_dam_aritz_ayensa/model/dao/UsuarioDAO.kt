package com.example.proyecto_dam_aritz_ayensa.model.dao


import android.util.Log
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.utils.HashUtil
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class UsuarioDAO {

    // Instancia de Firestore para interactuar con la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Instancia de FirebaseAuth para manejar la autenticación de usuarios
    private val auth = FirebaseAuth.getInstance()

    // Referencia a la colección "usuarios" en Firestore
    private val usuariosCollection = db.collection("usuarios")


    /**
     * Método: saveUser
     *
     * Guarda un nuevo usuario en Firebase Authentication y Firestore.
     * Si la creación del usuario en Authentication es exitosa, se almacena la información del usuario en Firestore.
     *
     * @param usuario Objeto Usuario que contiene la información del usuario a guardar.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     */
    fun saveUser(
        nombre: String,
        email: String,
        contraseña: String,
        token: String, // 🔹 ahora también recibe el token
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



    fun eliminarFcmToken(uid: String, token: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
            usuariosCollection.document(uid).update("fcmTokens", FieldValue.arrayRemove(token))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }




    suspend fun getIdMisListasByIdUsuario(idUsuario: String) : List<String>{
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




    suspend fun getMisListasSizeByIdUsuario(idUsuario: String): Int {
        return try {
            val document = usuariosCollection
                .document(idUsuario)
                .get()
                .await()

            if (document.exists()) {
                // Obtener la lista y verificar que no sea nula
                val idListas = document.get("idListasCompartidas") as? List<String>
                idListas?.size ?: 0 // Si es nulo, retorna 0
            } else {
                0 // Usuario no encontrado
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener tamaño de listas compartidas", e)
            -1 // Opcional: Retornar -1 en caso de error
        }
    }


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


    suspend fun getListasCompartidasSizeByIdUsuario(idUsuario: String): Int {
        return try {
            val document = usuariosCollection
                .document(idUsuario)
                .get()
                .await()

            if (document.exists()) {
                // Obtener la lista y verificar que no sea nula
                val idListas = document.get("idListas") as? List<String>
                idListas?.size ?: 0 // Si es nulo, retorna 0
            } else {
                0 // Usuario no encontrado
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener tamaño de listas", e)
            -1 // Opcional: Retornar -1 en caso de error
        }
    }

    suspend fun añadirNotificacionAUsuarios(
        idsUsuarios: List<String>,
        idNotificacion: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val usuariosCollection = db.collection("usuarios")

        for (idUsuario in idsUsuarios) {
            val usuarioRef = usuariosCollection.document(idUsuario)

            // Añadir el ID de notificación al map con valor false (no leída)
            usuarioRef.update("idsNotificaciones.$idNotificacion", false).await()
        }
    }

    suspend fun eliminarNotificacionesDeUsuarios(idUsuario : String, idsNotificaciones: List<String>) {
        val usuarioRef = usuariosCollection.document(idUsuario)

        // Para cada ID de notificación, elimina el campo correspondiente del mapa
        for (idNotificacion in idsNotificaciones) {
            usuarioRef.update("idsNotificaciones.$idNotificacion", FieldValue.delete()).await()
        }

    }

    suspend fun marcarNotificacionComoLeida(idUsuario: String, idNotificacion: String) {
        val db = FirebaseFirestore.getInstance()
        val usuarioRef = db.collection("usuarios").document(idUsuario)

        usuarioRef.update("idsNotificaciones.$idNotificacion", true).await()
    }

    suspend fun marcarNotificacionesComoLeidas(idUsuario: String, idsNotificaciones: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val usuarioRef = db.collection("usuarios").document(idUsuario)

        val updates = idsNotificaciones.associate { "idsNotificaciones.$it" to true }
        usuarioRef.update(updates).await()
    }


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

                // Llama al servicio para obtener las notificaciones
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
     * Obtiene un usuario específico de Firestore utilizando su ID.
     *
     * @param usuarioID ID del usuario que se desea obtener.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe un objeto Usuario o null si no se encuentra el usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     */
    fun getUser(usuarioID: String, onSuccess: (Usuario?) -> Unit, onFailure: (Exception) -> Unit) {
        // Obtener el documento del usuario en Firestore usando su ID
        usuariosCollection.document(usuarioID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Mapear los datos del documento a un objeto Usuario
                    val nombre = document.getString("nombre") ?: ""
                    val email = document.getString("email") ?: ""
                    val contrasena = document.getString("contrasena") ?: ""

                    // Crear un objeto Usuario con los datos obtenidos
                    val usuario = Usuario(id = usuarioID, nombre = nombre, email = email)
                    onSuccess(usuario)
                } else {
                    // Si el documento no existe, retornar null
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                // Manejar errores y ejecutar el callback onFailure
                onFailure(exception)
            }
    }

    suspend fun getUserById(usuarioID: String): Usuario? {
        return try {
            val document = usuariosCollection.document(usuarioID).get().await()
            if (document.exists()) {
                val nombre = document.getString("nombre") ?: ""
                val email = document.getString("email") ?: ""
                val idListas = document.get("idListas") as? List<String> ?: emptyList()
                val idListasCompartidas = document.get("idListasCompartidas") as? List<String> ?: emptyList()
                Usuario(usuarioID, nombre, email, idListas, idListasCompartidas)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("UsuarioDAO", "Error al obtener usuario $usuarioID", e)
            null
        }
    }

    suspend fun getUserNameById(usuarioID: String): String? {
        return try {
            val document = usuariosCollection.document(usuarioID).get().await()
            document.getString("nombre")
        } catch (e: Exception) {
            Log.e("UsuarioDAO", "Error al obtener nombre de usuario $usuarioID", e)
            null
        }
    }


    /*suspend fun getUserIdByEmail(email: String): String? {
        if (email.isBlank()) return null // Validación inicial

        return try {
            val query = usuariosCollection
                .whereEqualTo("email", email)

            val snapshot = query.get().await()
            snapshot.documents.firstOrNull()?.id // Devuelve null si no hay resultados
        } catch (e: Exception) {
            Log.e("Firestore", "Error al buscar usuario", e)
            null
        }
    }*/
    /**
     * Método: getAllUsers
     *
     * Obtiene todos los usuarios almacenados en Firestore.
     *
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe una lista de objetos Usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     */
    suspend fun getAllUsers(): List<Usuario> {
        return try {
            val querySnapshot = usuariosCollection.get().await()
            val usuarios = querySnapshot.toObjects(Usuario::class.java)
            usuarios
        } catch (e: Exception) {
            emptyList() // Devuelve una lista vacía en caso de error
        }
    }

    /**
     * Método: updateUser
     *
     * Actualiza la información de un usuario existente en Firestore.
     *
     * @param usuario Objeto Usuario con la información actualizada.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     */
    fun updateUser(usuario: Usuario, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Crear un mapa con los datos actualizados del usuario
        val usuarioData = hashMapOf(
            "nombre" to usuario.nombre,
            "email" to usuario.email
        )

        // Actualizar el documento del usuario en Firestore
        usuariosCollection.document(usuario.id).update(usuarioData as Map<String, Any>)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }


    suspend fun añadirLista(idLista: String, usuarioId: String) {
        usuariosCollection
            .document(usuarioId)
            .update("idListas", FieldValue.arrayUnion(idLista))
            .await() // Espera a que la operación termine
    }

    // Método para añadir lista compartida (versión mejorada)
    suspend fun añadirListaCompartida(idListaCompartida: String, usuarioId: String) {
        usuariosCollection
            .document(usuarioId)
            .update("idListasCompartidas", FieldValue.arrayUnion(idListaCompartida))
            .await()
    }


    suspend fun eliminarLista(idLista: String, usuarioId: String) {
        usuariosCollection
            .document(usuarioId)
            .update("idListas", FieldValue.arrayRemove(idLista))
            .await() // Espera a que la operación termine
    }

    // Método para añadir lista compartida (versión mejorada)
    suspend fun eliminarListaCompartida(idListaCompartida: String, usuarioId: String) {
        usuariosCollection
            .document(usuarioId)
            .update("idListasCompartidas", FieldValue.arrayRemove(idListaCompartida))
            .await()
    }

    suspend fun eliminarListaCompartidaDeUsuarios(idListaCompartida: String) {
        val usuariosSnapshot = usuariosCollection.get().await()
        for (document in usuariosSnapshot.documents) {
            document.reference.update("idListasCompartidas", FieldValue.arrayRemove(idListaCompartida)).await()
        }
    }


    /**
     * Método: deleteUser
     *
     * Elimina un usuario de Firestore utilizando su ID.
     *
     * @param usuarioID ID del usuario que se desea eliminar.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     */
    fun deleteUser(usuarioID: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Eliminar el documento del usuario en Firestore
        usuariosCollection.document(usuarioID).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    /**
     * Método: getUserByEmail
     *
     * Obtiene un usuario de Firestore utilizando su dirección de correo electrónico.
     *
     * @param usuarioEmail Dirección de correo electrónico del usuario que se desea obtener.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe un objeto Usuario o null si no se encuentra el usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
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


    /**
     * Método: updatePassword
     *
     * Actualiza la contraseña de un usuario en Firestore.
     *
     * @param userId ID del usuario cuya contraseña se desea actualizar.
     * @param password Nueva contraseña que se desea almacenar.
     */
    fun updatePassword(userId: String, password: String) {
        // Actualizar el campo "contrasena" del usuario en Firestore
        usuariosCollection.document(userId)
            .update("contrasena", password)
            .addOnSuccessListener {
                // Registrar éxito en la actualización
                Log.d("Firestore", "Contraseña cifrada actualizada")
            }
            .addOnFailureListener { e ->
                // Registrar error en la actualización
                Log.e("Firestore", "Error al actualizar la contraseña", e)
            }
    }
}