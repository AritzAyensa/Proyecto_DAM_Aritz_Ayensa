package com.example.proyecto_dam_aritz_ayensa.model.service
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn


class NotificacionService(private val notificacionDAO: NotificacionDAO) {
    suspend fun saveNotificacion(notificacion: Notificacion):String {
        return notificacionDAO.saveNotificacion(notificacion)
    }

    suspend fun getNotificacionesPorUsuario(idUsuario: String) : List<Notificacion>{
        return notificacionDAO.getNotificacionesPorUsuario(idUsuario)
    }

    suspend fun getNotificacionesByIds(idNotificaciones: List<String>) : List<Notificacion>{
        return notificacionDAO.getNotificacionesByIds(idNotificaciones)
    }


    suspend fun eliminarNotificaciones(notificaciones: List<String>, idUsuario: String){
        notificacionDAO.eliminarNotificaciones(notificaciones, idUsuario)
    }

    fun getNotificacionesCountFlow(userId: String): Flow<Int> {
        return notificacionDAO.notificacionesCountFlow(userId)
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }
}