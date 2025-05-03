package com.example.proyecto_dam_aritz_ayensa.model.service
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista

/**
 * Clase UsuarioService
 *
 * Esta clase actúa como una capa de servicio que encapsula la lógica de negocio relacionada con los usuarios.
 * Utiliza una instancia de UsuarioDAO para interactuar con la capa de acceso a datos.
 *
 * @property usuarioDAO Instancia de UsuarioDAO para realizar operaciones de base de datos.
 */
class ListaService(private val listaDAO: ListaDAO) {
    /**
     * Método: saveUser
     *
     * Guarda un nuevo usuario en la base de datos después de verificar si ya existe un usuario con el mismo correo electrónico.
     *
     * @param usuario Objeto Usuario que contiene la información del usuario a guardar.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     */
    suspend fun saveLista(lista: Lista):String {
        return listaDAO.saveLista(lista)
    }

    suspend fun eliminarLista(idLista: String) {
        return listaDAO.eliminarLista(idLista)
    }


    suspend fun eliminarProductosDeLista(idLista: String, idsAEliminar: List<String>) {
        return listaDAO.eliminarProductosDeLista(idLista, idsAEliminar)
    }

    suspend fun getListaById(idLista: String) : Lista? {
        return listaDAO.getListaById(idLista)
    }

    suspend fun getMisListasByUsuarioId(idListas: List<String>):List<Lista> {
        return listaDAO.getMisListasByUsuarioId(idListas)
    }

    suspend fun añadirProductoALista(idProducto: String, idLista: String) {
        // Delegar la operación al DAO
        listaDAO.añadirProducto(idProducto, idLista)
    }
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