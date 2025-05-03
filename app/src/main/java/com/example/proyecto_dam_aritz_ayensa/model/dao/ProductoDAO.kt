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

    suspend fun calcularPrecioTotal(idsProductos: List<String>): Double {
        return try {
            val productosSnapshot = productosCollection
                .whereIn("id", idsProductos)
                .get()
                .await()

            val productos = productosSnapshot.toObjects(Producto::class.java)
            productos.sumOf { it.precioAproximado ?: 0.0 }
        } catch (e: Exception) {
            Log.e("Firestore", "Error al calcular precio total", e)
            0.0
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
}