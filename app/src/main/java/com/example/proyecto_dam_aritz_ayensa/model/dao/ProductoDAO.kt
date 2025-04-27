package com.example.proyecto_dam_aritz_ayensa.model.dao


import android.util.Log
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.utils.HashUtil
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await


class ProductoDAO {

    // Instancia de Firestore para interactuar con la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Instancia de FirebaseAuth para manejar la autenticación de usuarios
    private val auth = FirebaseAuth.getInstance()

    // Referencia a la colección "usuarios" en Firestore
    private val productosCollection = db.collection("productos")


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
    suspend fun saveProducto(producto: Producto): String {
        val docRef = productosCollection.document()

        val productoData = hashMapOf(
            "id" to docRef.id,
            "nombre" to producto.nombre,
            "precioAproximado" to producto.precioAproximado,
            "categoria" to producto.categoria,
            "codigoBarras" to producto.codigoBarras,
            "prioridad" to producto.prioridad,
            "idCreador" to producto.idCreador
        )
        Log.e("ProductoDAO", "Producto creado")
        docRef.set(productoData).await()
        return docRef.id
    }
    suspend fun getProductosByIds(idProductos: List<String>): List<Producto> {
        if (idProductos.isEmpty()) return emptyList()

        val productos = mutableListOf<Producto>()
        val chunks = idProductos.chunked(10)

        for (chunk in chunks) {
            val querySnapshot = productosCollection
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()

            productos.addAll(querySnapshot.toObjects(Producto::class.java))
        }

        return productos
    }

    suspend fun getProductosByNombreYCategoria(nombre: String, categoria: String): List<Producto> {
        return try {
            val query = if (categoria.isNotBlank()) {
                productosCollection.whereEqualTo("categoria", categoria)
            } else {
                productosCollection
            }

            val querySnapshot = query.get().await()
            val productos = querySnapshot.toObjects(Producto::class.java)

            if (nombre.isNotBlank()) {
                productos.filter {
                    it.nombre.contains(nombre, ignoreCase = true)
                }
            } else {
                productos
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener productos por nombre y categoría", e)
            emptyList()
        }
    }




    suspend fun getProductos(): List<Producto> {
        return try {
            val querySnapshot = productosCollection
                .get()
                .await()

            querySnapshot.toObjects(Producto::class.java)

        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener productos", e)
            emptyList()
        }
    }
    suspend fun getProductoPorCodigoBarras(codigoBarras: String): Producto? {
        return try {
            val querySnapshot = productosCollection
                .whereEqualTo("codigoBarras", codigoBarras)
                .limit(1)  // Optimización ya que los códigos deberían ser únicos
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                querySnapshot.documents.first().toObject(Producto::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error al buscar producto por código", e)
            null
        }
    }

    suspend fun getAllCodigosBarras(): List<String> {
        try {
            // Obtener todos los documentos de la colección "productos"
            val querySnapshot = productosCollection.get().await()

            // Mapear cada documento a su código de barras y filtrar nulos (si aplica)
            return querySnapshot.toObjects(Producto::class.java).mapNotNull { it.codigoBarras }

        } catch (e: Exception) {
            Log.e("ProductoDAO", "Error al obtener códigos de barras", e)
            return emptyList()
        }
    }
    /*suspend fun eliminarLista(idLista: String) {
        if (idLista.isNotBlank()) {
            listasCollection.document(idLista).delete().await()
        } else {
            throw IllegalArgumentException("El ID de la lista no puede estar vacío")
        }
    }


    suspend fun getMisListasByUsuarioId(idListas: List<String>): List<Lista> {
        if (idListas.isEmpty()) return emptyList()

        // Firestore permite máximo 10 elementos en "whereIn". Dividimos en chunks de 10.
        val chunkedIds = idListas.chunked(6)
        val listas = mutableListOf<Lista>()

        for (chunk in chunkedIds) {
            val querySnapshot = listasCollection
                .whereIn(FieldPath.documentId(), chunk) // Buscar por ID de documento
                .get()
                .await()

            listas.addAll(querySnapshot.toObjects(Lista::class.java))
        }

        return listas
    }

    suspend fun getListaById(idLista: String): Lista? {
        if (idLista.isBlank()) return null

        return try {
            val document = listasCollection.document(idLista).get().await()

            if (document.exists()) {
                Lista(
                    id = document.getString("id") ?: "",
                    titulo = document.getString("titulo") ?: "",
                    descripcion = document.getString("descripcion") ?: "",
                    color = document.getString("color") ?: "#FFFFFF",
                    idCreador = document.getString("idCreador") ?: "",
                    idsUsuariosCompartidos = document.get("idsUsuariosCompartidos") as? List<String> ?: emptyList(),
                    idsProductos = document.get("idsProductos") as? List<String> ?: emptyList()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error getting lista", e)
            null
        }
    }*/



    /**
     * Método: getUser
     *
     * Obtiene un usuario específico de Firestore utilizando su ID.
     *
     * @param usuarioID ID del usuario que se desea obtener.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe un objeto Usuario o null si no se encuentra el usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     *//*
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
                    val usuario = Usuario(id = usuarioID, nombre = nombre, email = email, contrasena = contrasena)
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

    *//**
     * Método: getAllUsers
     *
     * Obtiene todos los usuarios almacenados en Firestore.
     *
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe una lista de objetos Usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     *//*
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

    *//**
     * Método: updateUser
     *
     * Actualiza la información de un usuario existente en Firestore.
     *
     * @param usuario Objeto Usuario con la información actualizada.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     *//*
    fun updateUser(usuario: Usuario, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Crear un mapa con los datos actualizados del usuario
        val usuarioData = hashMapOf(
            "nombre" to usuario.nombre,
            "email" to usuario.email,
            "contrasena" to usuario.contrasena
        )

        // Actualizar el documento del usuario en Firestore
        usuariosCollection.document(usuario.id).update(usuarioData as Map<String, Any>)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    *//**
     * Método: deleteUser
     *
     * Elimina un usuario de Firestore utilizando su ID.
     *
     * @param usuarioID ID del usuario que se desea eliminar.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     *//*
    fun deleteUser(usuarioID: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Eliminar el documento del usuario en Firestore
        usuariosCollection.document(usuarioID).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    *//**
     * Método: getUserByEmail
     *
     * Obtiene un usuario de Firestore utilizando su dirección de correo electrónico.
     *
     * @param usuarioEmail Dirección de correo electrónico del usuario que se desea obtener.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe un objeto Usuario o null si no se encuentra el usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     *//*
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
                        email = usuarioEmail,
                        contrasena = contrasena
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

    *//**
     * Método: updatePassword
     *
     * Actualiza la contraseña de un usuario en Firestore.
     *
     * @param userId ID del usuario cuya contraseña se desea actualizar.
     * @param password Nueva contraseña que se desea almacenar.
     *//*
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
    }*/
}