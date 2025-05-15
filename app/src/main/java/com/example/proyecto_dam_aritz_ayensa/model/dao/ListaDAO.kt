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


class ListaDAO {

    // Instancia de Firestore para interactuar con la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Instancia de FirebaseAuth para manejar la autenticación de usuarios
    private val auth = FirebaseAuth.getInstance()

    // Referencia a la colección "usuarios" en Firestore
    private val listasCollection = db.collection("listas")


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

    suspend fun eliminarLista(idLista: String) {
        if (idLista.isNotBlank()) {
            listasCollection.document(idLista).delete().await()
        } else {
            throw IllegalArgumentException("El ID de la lista no puede estar vacío")
        }
    }

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
                    idProductos = document.get("idProductos") as? List<String> ?: emptyList()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error getting lista", e)
            null
        }
    }
    suspend fun añadirProducto(idProducto: String, idLista: String) {
        listasCollection
            .document(idLista)
            .update("idProductos", FieldValue.arrayUnion(idProducto))
            .await()
    }

    fun productosDeListaFlow(
        idLista: String,
        productoService: ProductoService
    ): Flow<List<Producto>> = callbackFlow {
        val docRef = listasCollection.document(idLista)

        // Escucha en tiempo real el documento de la lista
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                // Suponemos que idProductos es un List<String>
                val ids = snapshot.get("idProductos") as? List<*>
                    ?: emptyList<String>()

                // En background obtén los productos
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



}