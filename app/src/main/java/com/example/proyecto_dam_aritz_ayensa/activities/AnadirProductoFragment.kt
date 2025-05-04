package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.adapters.ProductoAdapter
import com.example.proyecto_dam_aritz_ayensa.adapters.ProductoParaAnadirAdapter
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentAnadirProductoBinding
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentVistaListaBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.ProductoDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService
import com.example.proyecto_dam_aritz_ayensa.model.service.ProductoService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.widget.addTextChangedListener
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.utils.GenericConstants


class AnadirProductoFragment : Fragment() {
    private var _binding: FragmentAnadirProductoBinding? = null
    private val binding get() = _binding!!
    private lateinit var lista: Lista

    private var idLista: String = ""
    private lateinit var userId : String

    private val categorias = listOf("Fruta", "Verdura", "Carne", "Pescado","Limpieza","Higiene", "Otros")
    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var notificacionService: NotificacionService
    private lateinit var productoService: ProductoService
    private lateinit var sessionManager: SessionManager
    private var productos: List<Producto> = emptyList()
    private var texto: String = ""
    private var categoriaSeleccionada: String = ""

    private lateinit var recyclerViewProductos: RecyclerView

    private lateinit var progressBar : ProgressBar
    private lateinit var buttonCrearProducto : Button
    private lateinit var adapter: ProductoParaAnadirAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnadirProductoBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())
        notificacionService = NotificacionService(NotificacionDAO())
        usuarioService = UsuarioService(UsuarioDAO(),notificacionService)
        productoService = ProductoService(ProductoDAO())
        sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId().toString()
        progressBar = binding.loadingSpinner
        recyclerViewProductos = binding.recyclerProductos
        //Obtener el id de la lista
        arguments?.let {
            if(idLista.isNullOrBlank()){
                idLista = it.getString("idLista").toString()
            }

        }
        configurarDropdownMenu()
        configurarBuscador()
        /*cargarBotones()*/
        cargarProductos()
        return binding.root
    }

    private fun cargarProductos() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            try {
                // Cargar lista actual
                lista = listaService.getListaById(idLista)!!
                productos = productoService.getProductos().sorted()

                withContext(Dispatchers.Main) {
                    if (::adapter.isInitialized) {
                        adapter.actualizarProductos(productos)
                    } else {
                        adapter = ProductoParaAnadirAdapter(productos) { productoId ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    if (lista.idProductos.contains(productoId)) {
                                        withContext(Dispatchers.Main) {
                                            Utils.mostrarMensaje(
                                                requireContext(),
                                                "El producto ya está en la lista"
                                            )
                                        }
                                    } else {
                                        listaService.añadirProductoALista(idLista, productoId)
                                        // Actualizar lista local
                                        lista = listaService.getListaById(idLista)!!
                                        withContext(Dispatchers.Main) {
                                            Utils.mostrarMensaje(
                                                requireContext(),
                                                "Producto añadido"
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Utils.mostrarMensaje(
                                            requireContext(),
                                            "Error: ${e.message}"
                                        )
                                    }
                                }
                            }
                        }
                        recyclerViewProductos.apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = this@AnadirProductoFragment.adapter
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Utils.mostrarMensaje(context, "Error: ${e.message}")
                }
            }
            progressBar.visibility = View.GONE
        }
    }

    /*private fun cargarBotones() {
        buttonCrearProducto = binding.btnCrearProducto
        if (buttonCrearProducto != null) {
            buttonCrearProducto.setOnClickListener {
                goToCrearProducto()
            }
        }
    }*/
    private fun configurarDropdownMenu() {

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, GenericConstants.PRIORIDAD_CATEGORIAS.keys.sorted().toList())
        val autoCompleteTextView = binding.autoCompleteTextView
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->

            categoriaSeleccionada = parent.getItemAtPosition(position) as String
            buscarProductos()
        }
    }

    private fun configurarBuscador() {
        val etBuscar = binding.etBuscarProducto
        etBuscar.addTextChangedListener { editable ->
            texto = editable.toString()
            buscarProductos( )
        }
    }


    private fun buscarProductos() {
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
            }

            productos = productoService.getProductosByNombreYCategoria(texto, categoriaSeleccionada).sorted()

            withContext(Dispatchers.Main) {
                adapter.actualizarProductos(productos)
                progressBar.visibility = View.GONE
            }
        }
    }





    /*private fun goToCrearProducto() {
        val bundle = Bundle().apply {
            putString("idLista", idLista)
        }
        findNavController().navigate(R.id.action_añadir_productoFragment_to_crearProductoFragment, bundle)
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}