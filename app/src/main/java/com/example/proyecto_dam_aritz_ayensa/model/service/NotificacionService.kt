package com.example.proyecto_dam_aritz_ayensa.model.service
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion


class NotificacionService(private val notificacionDAO: NotificacionDAO) {
    private var usuarioService = UsuarioService(UsuarioDAO())
    suspend fun saveNotificacion(notificacion: Notificacion):String {
        return notificacionDAO.saveNotificacion(notificacion)
    }

    suspend fun getNotificacionesPorUsuario(idUsuario: String) : List<Notificacion>{
        return notificacionDAO.getNotificacionesPorUsuario(idUsuario)
    }


    suspend fun eliminarNotificaciones(notificaciones: List<String>, idUsuario: String){
        notificacionDAO.eliminarNotificaciones(notificaciones, idUsuario)
    }
}