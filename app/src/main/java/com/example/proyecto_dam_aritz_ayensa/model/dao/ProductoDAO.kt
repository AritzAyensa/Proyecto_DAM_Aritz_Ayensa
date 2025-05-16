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

/**
 * DAO para gestionar productos en Firestore.
 */
class ProductoDAO {

    // Instancia de Firestore para interactuar con la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Instancia de FirebaseAuth para manejar la autenticación de usuarios
    private val auth = FirebaseAuth.getInstance()

    // Referencia a la colección "usuarios" en Firestore
    private val productosCollection = db.collection("productos")


    /**
     * Método: saveProducto
     *
     * Guarda un nuevo producto en la colección "productos" de Firestore.
     *
     * @param producto Objeto Producto con los datos a guardar.
     * @return ID del documento creado.
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
    /**
     * Método: updateProducto
     *
     * Actualiza los datos de un producto existente en Firestore.
     *
     * @param producto Objeto Producto con datos actualizados (debe tener un ID válido).
     * @throws IllegalArgumentException si el ID del producto está vacío.
     * @throws Exception si ocurre un error durante la actualización.
     */
    suspend fun updateProducto(producto: Producto) {
        if (producto.id.isBlank()) {
            throw IllegalArgumentException("El ID del producto no puede estar vacío")
        }

        val productoData = mapOf(
            "id" to producto.id,
            "nombre" to producto.nombre,
            "precioAproximado" to producto.precioAproximado,
            "categoria" to producto.categoria,
            "codigoBarras" to producto.codigoBarras,
            "idCreador" to producto.idCreador
        )

        try {
            productosCollection
                .document(producto.id)
                .set(productoData)
                .await()
            Log.d("ProductoDAO", "Producto actualizado correctamente")
        } catch (e: Exception) {
            Log.e("ProductoDAO", "Error al actualizar el producto", e)
            throw e
        }
    }

    /**
    * Método: getProductosByIds
    *
    * Obtiene una lista de productos a partir de una lista de IDs.
    * Realiza consultas por lotes para optimizar el rendimiento.
    *
    * @param idProductos Lista de IDs de productos a buscar.
    * @return Lista de objetos Producto correspondientes a los IDs proporcionados.
    */
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

    /**
     * Método: getProductoById
     *
     * Obtiene un producto a partir de su ID.
     *
     * @param idProducto ID del producto a buscar.
     * @return Objeto Producto correspondiente al ID proporcionado, o null si no se encuentra.
     */
    suspend fun getProductoById(idProducto: String): Producto? {
        if (idProducto.isBlank()) return null

        return try {
            val snapshot = productosCollection
                .document(idProducto)
                .get()
                .await()

            snapshot.toObject(Producto::class.java)?.apply {
                // Si necesitas asignar manualmente el ID del documento al objeto
                // this.id = snapshot.id
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Método: getProductosByNombreYCategoria
     *
     * Obtiene una lista de productos filtrados por nombre y categoría.
     *
     * @param nombre Nombre del producto a buscar.
     * @param categoria Categoría del producto a buscar.
     * @return Lista de objetos Producto que coinciden con los filtros proporcionados.
     */


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
    /**
     * Método: calcularPrecioTotal
     *
     * Calcula el precio total aproximado de una lista de productos a partir de sus IDs.
     *
     * @param idsProductos Lista de IDs de productos a calcular el precio total.
     * @return Precio total aproximado de los productos, o 0.0 si ocurre un error.
     */
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

    /**
     * Método: getProductos
     *
     * Obtiene todos los productos de la colección "productos" en Firestore.
     *
     * @return Lista de objetos Producto correspondientes a todos los documentos de la colección.
     */

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

    /**
     * Método: getProductoPorCodigoBarras
     *
     * Obtiene un producto a partir de su código de barras.
     * Se limita la consulta a un solo resultado para optimizar el rendimiento.
     *
     * @param codigoBarras Código de barras del producto a buscar.
     * @return Objeto Producto correspondiente al código de barras proporcionado, o null si no se encuentra.
     */
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
    /**
     * Método: getAllCodigosBarras
     *
     * Obtiene todos los códigos de barras de los productos almacenados en Firestore.
     *
     * @return Lista de códigos de barras de todos los productos.
     */
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