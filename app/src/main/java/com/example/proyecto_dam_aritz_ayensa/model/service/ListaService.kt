package com.example.proyecto_dam_aritz_ayensa.model.service
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
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


    suspend fun eliminarProductoDeLista(idLista: String, idProducto: String) {
        return listaDAO.eliminarProductoDeLista(idLista, idProducto)
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


    fun productosDeListaFlow(idLista: String, productoService: ProductoService): Flow<List<Producto>> {
        return listaDAO.productosDeListaFlow(idLista, productoService)
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }
}