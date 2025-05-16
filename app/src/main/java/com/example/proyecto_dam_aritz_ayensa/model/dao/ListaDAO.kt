package com.example.proyecto_dam_aritz_ayensa.model.dao


import android.util.Log
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
import com.example.proyecto_dam_aritz_ayensa.model.service.ProductoService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
/**
 * Clase: ListaDAO
 *
 * DAO para gestionar listas en Firestore (crear, eliminar, actualizar, consultar).
 */

class ListaDAO {

    // Instancia de Firestore para interactuar con la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Instancia de FirebaseAuth para manejar la autenticación de usuarios
    private val auth = FirebaseAuth.getInstance()

    // Referencia a la colección "usuarios" en Firestore
    private val listasCollection = db.collection("listas")


    /**
     * Método: saveLista
     *
     * Guarda una nueva lista en Firestore y devuelve su ID generado.
     *
     * @param lista Objeto Lista con los datos a guardar.
     * @return ID generado de la lista.
     */
    suspend fun saveLista(lista: Lista): String {
        val docRef = listasCollection.document()

        val listaData = hashMapOf(
            "id" to docRef.id,
            "titulo" to lista.titulo,
            "descripcion" to lista.descripcion,
            "color" to lista.color,
            "idCreador" to lista.idCreador
        )
        Log.e("ListaDAO", "Lista creada")
        docRef.set(listaData).await()
        return docRef.id
    }
    /**
     * Método: eliminarLista
     *
     * Elimina una lista de Firestore dado su ID.
     *
     * @param idLista ID de la lista a eliminar.
     */
    suspend fun eliminarLista(idLista: String) {
        if (idLista.isNotBlank()) {
            listasCollection.document(idLista).delete().await()
        } else {
            throw IllegalArgumentException("El ID de la lista no puede estar vacío")
        }
    }
    /**
     * Método: eliminarProductosDeLista
     *
     * Elimina varios productos de una lista mediante sus IDs.
     *
     * @param idLista ID de la lista.
     * @param idsAEliminar Lista de IDs de productos a eliminar.
     */
    suspend fun eliminarProductosDeLista(idLista: String, idsAEliminar: List<String>) {
        if (idLista.isBlank()) {
            throw IllegalArgumentException("El ID de la lista no puede estar vacío")
        }
        if (idsAEliminar.isEmpty()) throw IllegalArgumentException("La lista esta vacía")

        try {
            listasCollection.document(idLista)
                .update("idProductos", FieldValue.arrayRemove(*idsAEliminar.toTypedArray()))
                .await()
        } catch (e: Exception) {
            throw IllegalArgumentException("Error al completar compra")
        }
    }
    /**
     * Método: eliminarProductosSeleccionadosDeLista
     *
     * Elimina varios productos seleccionados de una lista.
     *
     * @param idLista ID de la lista.
     * @param idsAEliminar Lista de IDs de productos seleccionados a eliminar.
     */
    suspend fun eliminarProductosSeleccionadosDeLista(idLista: String, idsAEliminar: List<String>) {
        if (idLista.isBlank()) {
            throw IllegalArgumentException("El ID de la lista no puede estar vacío")
        }
        if (idsAEliminar.isEmpty()) throw IllegalArgumentException("La lista esta vacía")

        try {
            listasCollection.document(idLista)
                .update("idProductosSeleccionados", FieldValue.arrayRemove(*idsAEliminar.toTypedArray()))
                .await()
        } catch (e: Exception) {
            throw IllegalArgumentException("Error al completar compra")
        }
    }
    /**
     * Método: eliminarProductoDeLista
     *
     * Elimina un producto específico de una lista.
     *
     * @param idLista ID de la lista.
     * @param idProducto ID del producto a eliminar.
     */
    suspend fun eliminarProductoDeLista(idLista: String, idProducto: String) {
        require(idLista.isNotBlank()) { "El ID de la lista no puede estar vacío" }
        require(idProducto.isNotBlank()) { "El ID del producto no puede estar vacío" }

        try {
            listasCollection.document(idLista)
                .update("idProductos", FieldValue.arrayRemove(idProducto))
                .await()
        } catch (e: Exception) {
            throw IllegalArgumentException("Error al eliminar el producto de la lista", e)
        }
    }

    /**
     * Método: eliminarProductoSeleccionadoDeLista
     *
     * Elimina un producto seleccionado específico de una lista.
     *
     * @param idLista ID de la lista.
     * @param idProducto ID del producto seleccionado a eliminar.
     */
    suspend fun eliminarProductoSeleccionadoDeLista(idLista: String, idProducto: String) {
        require(idLista.isNotBlank()) { "El ID de la lista no puede estar vacío" }
        require(idProducto.isNotBlank()) { "El ID del producto no puede estar vacío" }

        try {
            listasCollection.document(idLista)
                .update("idProductosSeleccionados", FieldValue.arrayRemove(idProducto))
                .await()
        } catch (e: Exception) {
            throw IllegalArgumentException("Error al eliminar el producto de la lista", e)
        }
    }


    /**
     * Método: getMisListasByUsuarioId
     *
     * Recupera las listas que coinciden con los IDs dados.
     *
     * @param idListas Lista de IDs de listas a recuperar.
     * @return Lista de objetos Lista.
     */
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
    /**
     * Método: getListaById
     *
     * Obtiene una lista por su ID.
     *
     * @param idLista ID de la lista a buscar.
     * @return Objeto Lista o null si no existe.
     */
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
                    idProductos = document.get("idProductos") as? List<String> ?: emptyList(),
                    idProductosSeleccionados = document.get("idProductosSeleccionados") as? List<String> ?: emptyList()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error getting lista", e)
            null
        }
    }
    /**
     * Método: añadirProducto
     *
     * Añade un producto a la lista especificada.
     *
     * @param idProducto ID del producto a añadir.
     * @param idLista ID de la lista donde añadir.
     */
    suspend fun añadirProducto(idProducto: String, idLista: String) {
        listasCollection
            .document(idLista)
            .update("idProductos", FieldValue.arrayUnion(idProducto))
            .await()
    }

    /**
     * Método: añadirProductoSeleccionado
     *
     * Añade un producto seleccionado a la lista.
     *
     * @param idProducto ID del producto a añadir.
     * @param idLista ID de la lista.
     */
    suspend fun añadirProductoSeleccionado(idProducto: String, idLista: String) {
        listasCollection
            .document(idLista)
            .update("idProductosSeleccionados", FieldValue.arrayUnion(idProducto))
            .await()
    }

    /**
     * Método: productosDeListaFlow
     *
     * Devuelve un Flow que emite la lista de productos asociados a una lista en tiempo real.
     *
     * @param idLista ID de la lista.
     * @param productoService Servicio para obtener productos por IDs.
     * @return Flow con la lista de productos.
     */
    fun productosDeListaFlow(
        idLista: String,
        productoService: ProductoService
    ): Flow<List<Producto>> = callbackFlow {
        val docRef = listasCollection.document(idLista)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val ids = snapshot.get("idProductos") as? List<*>
                    ?: emptyList<String>()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val idStrings = ids.filterIsInstance<String>()
                        val productos = productoService.getProductosByIds(idStrings)
                        trySend(productos).isSuccess
                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Método: productosSeleccionadosDeListaFlow
     *
     * Devuelve un Flow que emite los IDs de productos seleccionados de una lista en tiempo real.
     *
     * @param idLista ID de la lista.
     * @return Flow con lista de IDs de productos seleccionados.
     */
    fun productosSeleccionadosDeListaFlow(
        idLista: String
    ): Flow<List<String>> = callbackFlow {
        val docRef = listasCollection.document(idLista)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val ids = snapshot.get("idProductosSeleccionados") as? List<*>
                    ?: emptyList<String>()

                // Filtramos solo strings y los enviamos
                val idStrings = ids.filterIsInstance<String>()
                trySend(idStrings).isSuccess
            }
        }

        awaitClose { listener.remove() }
    }




}