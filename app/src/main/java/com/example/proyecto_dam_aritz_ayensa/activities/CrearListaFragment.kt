package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentCrearListaBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [CrearListaFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CrearListaFragment : Fragment() {

    private var _binding: FragmentCrearListaBinding? = null
    private val binding get() = _binding!!

    lateinit var buttonAñadirLista : Button
    lateinit var buttonCancelar : Button
    private lateinit var lisaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager


    private lateinit var inputTitulo: EditText
    private lateinit var inputDescripcion: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrearListaBinding.inflate(inflater, container, false)
        lisaService = ListaService(ListaDAO())
        usuarioService = UsuarioService(UsuarioDAO())
        sessionManager = SessionManager(requireContext())

        buttonAñadirLista = binding.crearListaBtnCrearLista
        buttonCancelar = binding.crearListaBtnCancelar


        inputTitulo = binding.crearListaEtTitulo
        inputDescripcion = binding.crearListaEtDescripcion

        if (buttonAñadirLista != null) {
            buttonAñadirLista.setOnClickListener {
                crearLista()
            }
        }
        if (buttonCancelar != null) {
            buttonCancelar.setOnClickListener {
                cancelar()
            }
        }
        return binding.root
    }
    private fun crearLista() {
        val textTitulo = inputTitulo.text.toString().trim()

        val textDescripcion = inputDescripcion.text.toString().trim()

        if (textTitulo.isNotEmpty() && textDescripcion.isNotEmpty()) {
            val lista = Lista()
            lista.titulo = textTitulo
            lista.descripcion = textDescripcion
            lista.idCreador = sessionManager.getUserId().toString()

            /*lifecycleScope.launch {
                val idLista = lisaService.saveLista(lista)
                val idCreador = lista.idCreador
                Log.i("Info lista", "$idLista, $idCreador")

                usuarioService.añadirListaAUsuario(idLista, sessionManager.getUserId().toString())

                Log.i("Termino", "$idLista, $idCreador")
            }*/
            lifecycleScope.launch {
                try {
                    val idLista = lisaService.saveLista(lista)
                    usuarioService.añadirListaAUsuario(idLista, sessionManager.getUserId().toString())
                    cancelar()
                    Utils.mostrarMensaje(requireContext(), "Lista creada correctamente")
                } catch (e: Exception) {
                    Log.e("CrearListaFragment", "Error: ${e.message}")
                    Utils.mostrarMensaje(requireContext(), "Error al crear la lista")
                }
            }
        } else {
            Utils.mostrarMensaje(requireContext(),"Ingrese un titulo y una descripción")
        }
    }

    private fun cancelar() {
       parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}