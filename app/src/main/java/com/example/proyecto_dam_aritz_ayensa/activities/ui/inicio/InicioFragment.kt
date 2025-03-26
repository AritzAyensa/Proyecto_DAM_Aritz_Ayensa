package com.example.proyecto_dam_aritz_ayensa.activities.ui.inicio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.activities.CrearListaFragment
import com.example.proyecto_dam_aritz_ayensa.adapters.ListaAdapter
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentInicioBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import kotlinx.coroutines.launch

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null

    lateinit var buttonAñadirLista : Button
    lateinit var buttonRecargarMisListas : ImageButton
    private lateinit var recyclerViewMisListas: RecyclerView
    private lateinit var adapter: ListaAdapter


    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())
        usuarioService = UsuarioService(UsuarioDAO())
        sessionManager = SessionManager(requireContext())
        buttonRecargarMisListas = binding.inicioBtnRecargarListas
        if (buttonRecargarMisListas != null) {
            buttonRecargarMisListas.setOnClickListener {
                cargarMisListas()
            }
        }
        buttonAñadirLista = binding.btnAnadirLista
        if (buttonAñadirLista != null) {
            buttonAñadirLista.setOnClickListener {
                abrirAñadirLista()
            }
        }


        recyclerViewMisListas = binding.recyclerMisListas
        recyclerViewMisListas.layoutManager = LinearLayoutManager(context)
        cargarMisListas()
        return binding.root
    }

    private fun abrirAñadirLista() {
        findNavController().navigate(R.id.action_inicioFragment_to_crearListaFragment)
    }
    private fun cargarMisListas() {

        Log.i("Inicio", "Cargando listas")
        lifecycleScope.launch {
            val idListas =
                usuarioService.getIdMisListasByIdUsuario(sessionManager.getUserId().toString())
            val misListas = listaService.getMisListasByUsuarioId(idListas)

            Log.i("Inicio", misListas.size.toString())
            Log.i("Numero listas", usuarioService.getMisListasSizeByIdUsuario(sessionManager.getUserId().toString()).toString())
            adapter = ListaAdapter(misListas) { lista ->
                // Maneja clics en los elementos
                Toast.makeText(context, "Clic en: ${lista.titulo}", Toast.LENGTH_SHORT).show()
            }
            recyclerViewMisListas.adapter = adapter
        }
    }
    private fun cargarListasCompartidas() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun recargarMisListas(view: View) {}

}