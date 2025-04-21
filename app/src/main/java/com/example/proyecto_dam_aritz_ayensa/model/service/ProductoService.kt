package com.example.proyecto_dam_aritz_ayensa.model.service
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.ProductoDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto


class ProductoService(private val productoDAO: ProductoDAO) {
    private var usuarioService = UsuarioService(UsuarioDAO())

    suspend fun saveProducto(producto: Producto) : String {
        return productoDAO.saveProducto(producto)

    }
    suspend fun getProductos(): List<Producto> {
        return productoDAO.getProductos()
    }

    suspend fun getProductosByIds(ids: List<String>): List<Producto> {
        return productoDAO.getProductosByIds(ids)

    }

    suspend fun getAllCodigosBarras(): List<String> {
        return productoDAO.getAllCodigosBarras()

    }
    suspend fun getProductoPorCodigoBarras(codigoBarras: String): Producto? {
        return productoDAO.getProductoPorCodigoBarras(codigoBarras)

    }

    /*suspend fun eliminarLista(idLista: String) {
        return listaDAO.eliminarLista(idLista)
    }

    suspend fun getListaById(idLista: String) : Lista? {
        return listaDAO.getListaById(idLista)
    }

    suspend fun getMisListasByUsuarioId(idListas: List<String>):List<Lista> {
        return listaDAO.getMisListasByUsuarioId(idListas)
    }*/

   /* *//**
     * Método: getUser
     *
     * Obtiene un usuario específico de la base de datos utilizando su ID.
     *
     * @param usuarioID ID del usuario que se desea obtener.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe un objeto Usuario o null si no se encuentra el usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     *//*
    fun getUser(usuarioID: String, onSuccess: (Usuario?) -> Unit, onFailure: (Exception) -> Unit) {
        // Delegar la operación al DAO
        usuarioDAO.getUser(usuarioID, onSuccess, onFailure)
    }

    *//**
     * Método: getAllUsers
     *
     * Obtiene todos los usuarios almacenados en la base de datos.
     *
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe una lista de objetos Usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     *//*
    fun getAllUsers(onSuccess: (List<Usuario>) -> Unit, onFailure: (Exception) -> Unit) {
        // Delegar la operación al DAO
        usuarioDAO.getAllUsers(onSuccess, onFailure)
    }

    *//**
     * Método: updateUser
     *
     * Actualiza la información de un usuario existente en la base de datos.
     *
     * @param usuario Objeto Usuario con la información actualizada.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     *//*
    fun updateUser(usuario: Usuario, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Delegar la operación al DAO
        usuarioDAO.updateUser(usuario, onSuccess, onFailure)
    }

    *//**
     * Método: deleteUser
     *
     * Elimina un usuario de la base de datos utilizando su ID.
     *
     * @param usuarioID ID del usuario que se desea eliminar.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     *//*
    fun deleteUser(usuarioID: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Delegar la operación al DAO
        usuarioDAO.deleteUser(usuarioID, onSuccess, onFailure)
    }

    *//**
     * Método: getUserByEmail
     *
     * Obtiene un usuario de la base de datos utilizando su dirección de correo electrónico.
     *
     * @param usuarioEmail Dirección de correo electrónico del usuario que se desea obtener.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe un objeto Usuario o null si no se encuentra el usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     *//*
    fun getUserByEmail(usuarioEmail: String, onSuccess: (Usuario?) -> Unit, onFailure: (Exception) -> Unit) {
        // Delegar la operación al DAO
        usuarioDAO.getUserByEmail(usuarioEmail, onSuccess, onFailure)
    }

    *//**
     * Método: updatePassword
     *
     * Actualiza la contraseña de un usuario en la base de datos.
     *
     * @param id ID del usuario cuya contraseña se desea actualizar.
     * @param password Nueva contraseña que se desea almacenar.
     *//*
    fun updatePassword(id: String, password: String) {
        // Delegar la operación al DAO
        usuarioDAO.updatePassword(id, password)
    }*/
}