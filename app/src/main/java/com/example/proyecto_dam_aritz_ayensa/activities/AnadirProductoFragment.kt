package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

class AnadirProductoFragment : Fragment() {
    private var _binding: FragmentAnadirProductoBinding? = null
    private val binding get() = _binding!!
    private lateinit var lista: Lista

    private lateinit var idLista: String
    private lateinit var userId : String


    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var productoService: ProductoService
    private lateinit var sessionManager: SessionManager

    private lateinit var recyclerViewProductos: RecyclerView

    private lateinit var buttonAñadirProducto : Button
    private lateinit var buttonCrearProducto : Button
    private lateinit var adapter: ProductoParaAnadirAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnadirProductoBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())
        usuarioService = UsuarioService(UsuarioDAO())
        productoService = ProductoService(ProductoDAO())
        sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId().toString()

        recyclerViewProductos = binding.recyclerProductos
        //Obtener el id de la lista
        arguments?.let {
            idLista = it.getString("idLista").toString()
        }

        cargarBotones()
        cargarProductos()
        return binding.root
    }

    private fun cargarProductos() {
        lifecycleScope.launch {
            try {
                // Cargar lista actual
                lista = listaService.getListaById(idLista)!!
                val productos = productoService.getProductos().sorted()

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
        }
    }

    private fun cargarBotones() {
        buttonCrearProducto = binding.btnCrearProducto
        if (buttonCrearProducto != null) {
            buttonCrearProducto.setOnClickListener {
                goToCrearProducto()
            }
        }

        buttonAñadirProducto = binding.btnAnadirProducto
        if (buttonAñadirProducto != null) {
            buttonAñadirProducto.setOnClickListener {
            }
        }
    }

    private fun goToCrearProducto() {
        val bundle = Bundle().apply {
            putString("idLista", idLista)
        }
        findNavController().navigate(R.id.action_añadir_productoFragment_to_crearProductoFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}