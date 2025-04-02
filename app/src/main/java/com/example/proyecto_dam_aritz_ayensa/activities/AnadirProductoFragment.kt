package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentAnadirProductoBinding
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentVistaListaBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import kotlinx.coroutines.launch

class AnadirProductoFragment : Fragment() {
    private var _binding: FragmentAnadirProductoBinding? = null
    private val binding get() = _binding!!
    private lateinit var lista: Lista

    private lateinit var idLista: String
    private lateinit var userId : String


    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager


    private lateinit var buttonAñadirProducto : Button
    private lateinit var buttonCrearProducto : Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnadirProductoBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())
        usuarioService = UsuarioService(UsuarioDAO())
        sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId().toString()

        //Obtener el id de la lista
        arguments?.let {
            idLista = it.getString("idLista").toString()
        }

        cargarBotones()

        return binding.root
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