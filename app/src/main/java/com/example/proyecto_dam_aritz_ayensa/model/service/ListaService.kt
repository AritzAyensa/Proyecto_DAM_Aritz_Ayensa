package com.example.proyecto_dam_aritz_ayensa.model.service
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

class ListaService(private val listaDAO: ListaDAO) {
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

    suspend fun eliminarProductosSeleccionadosDeLista(idLista: String, idsAEliminar: List<String>) {
        return listaDAO.eliminarProductosSeleccionadosDeLista(idLista, idsAEliminar)
    }


    suspend fun eliminarProductoSeleccionadoDeLista(idLista: String, idProducto: String) {
        return listaDAO.eliminarProductoSeleccionadoDeLista(idLista, idProducto)
    }

    suspend fun getListaById(idLista: String) : Lista? {
        return listaDAO.getListaById(idLista)
    }

    suspend fun getMisListasByListasId(idListas: List<String>):List<Lista> {
        return listaDAO.getMisListasByUsuarioId(idListas)
    }

    suspend fun añadirProductoALista(idProducto: String, idLista: String) {
        listaDAO.añadirProducto(idProducto, idLista)
    }


    suspend fun añadirProductoSeleccionado(idProducto: String, idLista: String) {
        listaDAO.añadirProductoSeleccionado(idProducto, idLista)
    }


    fun productosDeListaFlow(idLista: String, productoService: ProductoService): Flow<List<Producto>> {
        return listaDAO.productosDeListaFlow(idLista, productoService)
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    fun productosSeleccionadosDeListaFlow(idLista: String): Flow<List<String>> {
        return listaDAO.productosSeleccionadosDeListaFlow(idLista)
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }
}