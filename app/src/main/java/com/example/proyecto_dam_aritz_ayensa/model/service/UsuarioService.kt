package com.example.proyecto_dam_aritz_ayensa.model.service
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

/**
 * Clase UsuarioService
 *
 * Esta clase actúa como una capa de servicio que encapsula la lógica de negocio relacionada con los usuarios.
 * Utiliza una instancia de UsuarioDAO para interactuar con la capa de acceso a datos.
 *
 * @property usuarioDAO Instancia de UsuarioDAO para realizar operaciones de base de datos.
 */
class UsuarioService(private val usuarioDAO: UsuarioDAO,
                     private val notificacionService: NotificacionService) {
    fun saveUser(nombre: String, email: String, contraseña: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Elimina la verificación previa de usuarioExistente
        usuarioDAO.saveUser(nombre, email, contraseña, onSuccess, onFailure)
    }

    /**
     * Método: getUser
     *
     * Obtiene un usuario específico de la base de datos utilizando su ID.
     *
     * @param usuarioID ID del usuario que se desea obtener.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe un objeto Usuario o null si no se encuentra el usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     */
    fun getUser(usuarioID: String, onSuccess: (Usuario?) -> Unit, onFailure: (Exception) -> Unit) {
        // Delegar la operación al DAO
        usuarioDAO.getUser(usuarioID, onSuccess, onFailure)
    }

    suspend fun getUserById(usuarioID: String) : Usuario? {
        return usuarioDAO.getUserById(usuarioID)
    }


    suspend fun añadirNotificacionAUsuarios(idsUsuarios: List<String>, idNotificacion: String) {
        usuarioDAO.añadirNotificacionAUsuarios(idsUsuarios, idNotificacion)
    }

    suspend fun eliminarNotificacionesDeUsuarios(idUsuario : String, idsNotificaciones: List<String>) {
        usuarioDAO.eliminarNotificacionesDeUsuarios(idUsuario, idsNotificaciones)
    }
    suspend fun marcarNotificacionComoLeida(idUsuario: String, idNotificacion: String) {
        usuarioDAO.marcarNotificacionComoLeida(idUsuario, idNotificacion)
    }
    suspend fun marcarNotificacionesComoLeidas(idUsuario: String, idsNotificaciones: List<String>) {
        usuarioDAO.marcarNotificacionesComoLeidas(idUsuario, idsNotificaciones)
    }

    suspend fun getUserNameById(usuarioID: String) : String? {
        return usuarioDAO.getUserNameById(usuarioID)
    }
    fun notificacionesSinLeerCountFlow(userId: String): Flow<Int> {
        return usuarioDAO.notificacionesSinLeerCountFlow(userId)
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }
    fun notificacionesUsuarioFlow(userId: String): Flow<List<Notificacion>> {
        return usuarioDAO.notificacionesUsuarioFlow(userId, notificacionService)
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }


    /**
     * Método: getAllUsers
     *
     * Obtiene todos los usuarios almacenados en la base de datos.
     *
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe una lista de objetos Usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     */
    suspend fun getAllUsers() : List<Usuario> {
        // Delegar la operación al DAO
        return usuarioDAO.getAllUsers()
    }

    /**
     * Método: updateUser
     *
     * Actualiza la información de un usuario existente en la base de datos.
     *
     * @param usuario Objeto Usuario con la información actualizada.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     */
    fun updateUser(usuario: Usuario, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Delegar la operación al DAO
        usuarioDAO.updateUser(usuario, onSuccess, onFailure)
    }
    suspend fun añadirListaAUsuario(idLista: String, idUsuario: String) {
        // Delegar la operación al DAO
        usuarioDAO.añadirLista(idLista, idUsuario)
    }
    suspend fun añadirListaCompartidaAUsuario(idListaCompartida: String, idUsuario: String) {
        // Delegar la operación al DAO
        usuarioDAO.añadirListaCompartida(idListaCompartida, idUsuario)
    }


    suspend fun eliminarListaAUsuario(idLista: String, idUsuario: String) {
        usuarioDAO.eliminarLista(idLista, idUsuario)
    }
    suspend fun eliminarListaCompartidaAUsuario(idListaCompartida: String, idUsuario: String) {
        usuarioDAO.eliminarListaCompartida(idListaCompartida, idUsuario)
    }

    suspend fun eliminarListaCompartidaAUsuarios(idListaCompartida: String) {
        usuarioDAO.eliminarListaCompartidaDeUsuarios(idListaCompartida)
    }

    suspend fun getIdMisListasByIdUsuario(idUsuario: String) : List<String> {
       return usuarioDAO.getIdMisListasByIdUsuario(idUsuario)
    }

    suspend fun getMisListasSizeByIdUsuario(idUsuario: String): Int {
        return usuarioDAO.getMisListasSizeByIdUsuario(idUsuario)
    }


    suspend fun getIdListasCompartidasByIdUsuario(idUsuario: String) : List<String> {
        return usuarioDAO.getIdListasCompartidasByIdUsuario(idUsuario)
    }

    suspend fun getListasCompartidasSizeByIdUsuario(idUsuario: String): Int {
        return usuarioDAO.getListasCompartidasSizeByIdUsuario(idUsuario)
    }

    /**
     * Método: deleteUser
     *
     * Elimina un usuario de la base de datos utilizando su ID.
     *
     * @param usuarioID ID del usuario que se desea eliminar.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     */
    fun deleteUser(usuarioID: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Delegar la operación al DAO
        usuarioDAO.deleteUser(usuarioID, onSuccess, onFailure)
    }

    /**
     * Método: getUserByEmail
     *
     * Obtiene un usuario de la base de datos utilizando su dirección de correo electrónico.
     *
     * @param usuarioEmail Dirección de correo electrónico del usuario que se desea obtener.
     * @param onSuccess Función de callback que se ejecuta si la operación es exitosa. Recibe un objeto Usuario o null si no se encuentra el usuario.
     * @param onFailure Función de callback que se ejecuta si ocurre un error durante la operación.
     */
    fun getUserByEmail(usuarioEmail: String, onSuccess: (Usuario?) -> Unit, onFailure: (Exception) -> Unit) {
        // Delegar la operación al DAO
        usuarioDAO.getUserByEmail(usuarioEmail, onSuccess, onFailure)
    }

    /**
     * Método: updatePassword
     *
     * Actualiza la contraseña de un usuario en la base de datos.
     *
     * @param id ID del usuario cuya contraseña se desea actualizar.
     * @param password Nueva contraseña que se desea almacenar.
     */
    fun updatePassword(id: String, password: String) {
        // Delegar la operación al DAO
        usuarioDAO.updatePassword(id, password)
    }
}