package com.example.proyecto_dam_aritz_ayensa.model.dao


import android.util.Log
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.utils.HashUtil
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
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
    fun saveUser(nombre: String,email: String,contraseña: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        auth.createUserWithEmailAndPassword(email, contraseña)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Obtener el usuario desde task.result (no de auth.currentUser)
                    val user = task.result?.user
                    val userId = user?.uid

                    // Cifrar la contraseña
                    val hashedPassword = HashUtil.hashPassword(contraseña)

                    // Crear el mapa de datos
                    val usuarioData = hashMapOf(
                        "id" to userId,
                        "nombre" to nombre,
                        "email" to email,
                        "idListas" to listOf<String>(),
                        "idListasCompartidas" to listOf<String>(),

                    )

                    if (userId != null) {
                        usuariosCollection.document(userId).set(usuarioData)
                            .addOnSuccessListener {
                                Log.d("UsuarioService", "Usuario registrado con éxito")
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                // Rollback: Eliminar usuario de Auth si Firestore falla
                                user?.delete()
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


    suspend fun getMisListasSizeByIdUsuario(idUsuario: String): Int {
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

    suspend fun getUserById(usuarioID: String): Result<Usuario?> {
        return try {
            // Obtener documento de Firestore
            val document = usuariosCollection.document(usuarioID).get().await()

            if (document.exists()) {
                val nombre = document.getString("nombre") ?: ""
                val email = document.getString("email") ?: ""
                val usuario = Usuario(usuarioID, nombre, email)
                Result.success(usuario)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Método: getAllUsers
     *
     * Obtiene todos los usuarios almacenados en Firestore.
     *
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe una lista de objetos Usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     */
    fun getAllUsers(onSuccess: (List<Usuario>) -> Unit, onFailure: (Exception) -> Unit) {
        // Obtener todos los documentos de la colección "usuarios"
        usuariosCollection.get()
            .addOnSuccessListener { querySnapshot ->
                // Convertir los documentos a objetos Usuario
                val users = querySnapshot.toObjects(Usuario::class.java)
                onSuccess(users)
            }
            .addOnFailureListener { exception ->
                // Manejar errores y ejecutar el callback onFailure
                onFailure(exception)
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
    fun getUserByEmail(usuarioEmail: String, onSuccess: (Usuario?) -> Unit, onFailure: (Exception) -> Unit) {
        // Buscar el usuario en Firestore por su email
        usuariosCollection.whereEqualTo("email", usuarioEmail).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Obtener el primer documento de los resultados (asumiendo que el email es único)
                    val document = querySnapshot.documents.first()

                    // Mapear los datos del documento a un objeto Usuario
                    val nombre = document.getString("nombre") ?: ""
                    val contrasena = document.getString("contrasena") ?: ""

                    val usuario = Usuario(
                        id = document.id,
                        nombre = nombre,
                        email = usuarioEmail
                    )
                    onSuccess(usuario)
                } else {
                    // Si no se encuentra ningún usuario, retornar null
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                // Manejar errores y ejecutar el callback onFailure
                onFailure(exception)
            }
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