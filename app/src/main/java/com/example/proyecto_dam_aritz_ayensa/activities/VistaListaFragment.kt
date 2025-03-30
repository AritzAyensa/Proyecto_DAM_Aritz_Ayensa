package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentCrearListaBinding
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentVistaListaBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import kotlinx.coroutines.launch


class VistaListaFragment : Fragment() {

    private var _binding: FragmentVistaListaBinding? = null
    private val binding get() = _binding!!


    lateinit var buttonAñadirProducto : Button
    lateinit var buttonCompartirLista : Button
    lateinit var buttonEliminarLista : Button


    private lateinit var tvTituloLista: TextView
    private lateinit var lista: Lista

    private lateinit var idLista: String


    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVistaListaBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())
        usuarioService = UsuarioService(UsuarioDAO())
        sessionManager = SessionManager(requireContext())

        //Obtener el id de la lista
        arguments?.let {
            idLista = it.getString("idLista").toString()
        }

        tvTituloLista = binding.vistaListaTvTitulo

        cargarLista()
        cargarBotones()



        return binding.root
    }

    private fun cargarBotones() {
        buttonAñadirProducto = binding.btnAnadirProducto
        if (buttonAñadirProducto != null) {
            buttonAñadirProducto.setOnClickListener {
                añadirProducto()
            }
        }

        buttonCompartirLista = binding.btnCompartirLista
        if (buttonCompartirLista != null) {
            buttonCompartirLista.setOnClickListener {
                compartirLista()
            }
        }

        buttonEliminarLista = binding.btnEliminarLista
        if (buttonEliminarLista != null) {
            buttonEliminarLista.setOnClickListener {
                eliminarLista()
            }
        }
    }

    private fun compartirLista() {

    }
    private fun eliminarLista() {

    }

    private fun añadirProducto() {

    }

    private fun cargarLista() {
        lifecycleScope.launch {
            lista = listaService.getListaById(idLista)!!
            actualizarVista()
        }
    }

    private fun actualizarVista() {
        tvTituloLista.text = lista.titulo
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}