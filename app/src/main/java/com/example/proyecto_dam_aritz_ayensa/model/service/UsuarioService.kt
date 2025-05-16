package com.example.proyecto_dam_aritz_ayensa.model.service
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn


class UsuarioService(private val usuarioDAO: UsuarioDAO,
                     private val notificacionService: NotificacionService) {
    fun saveUser(nombre: String, email: String, contraseña: String, token: String, onSuccess: (Any?) -> Unit, onFailure: (Exception) -> Unit) {
        usuarioDAO.saveUser(nombre, email, contraseña,token, onSuccess, onFailure)
    }
    fun añadirTokenAUsuario(uid: String, token: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        usuarioDAO.añadirTokenAUsuario(uid, token, onSuccess, onFailure)
    }

    fun eliminarFcmToken(uid: String, token: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        usuarioDAO.eliminarFcmToken(uid, token, onSuccess, onFailure)
    }

    fun getUser(usuarioID: String, onSuccess: (Usuario?) -> Unit, onFailure: (Exception) -> Unit) {
        usuarioDAO.getUser(usuarioID, onSuccess, onFailure)
    }

    suspend fun añadirNotificacionAUsuarios(idsUsuarios: List<String>, idNotificacion: String) {
        usuarioDAO.añadirNotificacionAUsuarios(idsUsuarios, idNotificacion)
    }

    suspend fun eliminarNotificacionesDeUsuarios(idUsuario : String, idsNotificaciones: List<String>) {
        usuarioDAO.eliminarNotificacionesDeUsuarios(idUsuario, idsNotificaciones)
    }

    suspend fun marcarNotificacionesComoLeidas(idUsuario: String, idsNotificaciones: List<String>) {
        usuarioDAO.marcarNotificacionesComoLeidas(idUsuario, idsNotificaciones)
    }

    suspend fun getUserNameById(usuarioID: String) : String? {
        return usuarioDAO.getUserNameById(usuarioID)
    }


    suspend fun getUserIdsByListId(listId: String): List<String> {
        return usuarioDAO.getUserIdsByListId(listId)
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



    fun updateUser(usuario: Usuario, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        usuarioDAO.updateUser(usuario, onSuccess, onFailure)
    }
    suspend fun añadirListaAUsuario(idLista: String, idUsuario: String) {
        usuarioDAO.añadirLista(idLista, idUsuario)
    }
    suspend fun añadirListaCompartidaAUsuario(idListaCompartida: String, idUsuario: String) {
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
    suspend fun getIdListasCompartidasByIdUsuario(idUsuario: String) : List<String> {
        return usuarioDAO.getIdListasCompartidasByIdUsuario(idUsuario)
    }


    suspend fun getMisListasSizeByIdUsuario(idUsuario: String): Int {
        return usuarioDAO.getMisListasSizeByIdUsuario(idUsuario)
    }


    fun getUserByEmail(usuarioEmail: String, onSuccess: (Usuario?) -> Unit, onFailure: (Exception) -> Unit) {
        usuarioDAO.getUserByEmail(usuarioEmail, onSuccess, onFailure)
    }
}