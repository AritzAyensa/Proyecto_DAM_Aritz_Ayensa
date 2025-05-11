package com.example.proyecto_dam_aritz_ayensa.activities.ui.inicio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.adapters.ListaAdapter
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentInicioBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null

    lateinit var buttonAñadirLista : Button
    private lateinit var recyclerViewMisListas: RecyclerView
    private lateinit var recyclerViewListasCompartidas: RecyclerView
    private lateinit var adapter: ListaAdapter


    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var notificacionesService: NotificacionService
    private lateinit var sessionManager: SessionManager
    private lateinit var progressBar : ProgressBar
    private lateinit var progressBar2 : ProgressBar


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())
        notificacionesService = NotificacionService(NotificacionDAO())
        usuarioService = UsuarioService(UsuarioDAO(), notificacionesService)
        sessionManager = SessionManager(requireContext())



        recyclerViewMisListas = binding.recyclerMisListas
        recyclerViewMisListas.layoutManager = LinearLayoutManager(context)
        recyclerViewListasCompartidas = binding.recyclerListasCompartidas
        recyclerViewListasCompartidas.layoutManager = LinearLayoutManager(context)
        progressBar = binding.loadingSpinner
        progressBar2 = binding.loadingSpinner2
        cargarMisListas()
        cargarListasCompartidas()
        cargarBotones()
        return binding.root
    }


    private fun abrirAñadirLista() {
        findNavController().navigate(R.id.action_inicioFragment_to_crearListaFragment)
    }

    private fun abrirLista(idLista : String) {
        lifecycleScope.launch {
            if (listaService.getListaById(idLista) == null){
                Utils.mostrarMensaje(context, "Lista no encontrada")
            }else{
                val bundle = Bundle().apply {
                    putString("idLista", idLista)
                }
                findNavController().navigate(R.id.action_inicioFragment_to_vistaListaFragment, bundle)
            }
        }
    }
    private fun cargarBotones() {
        buttonAñadirLista = binding.btnAnadirLista
        if (buttonAñadirLista != null) {
            buttonAñadirLista.setOnClickListener {
                abrirAñadirLista()
            }
        }
    }

    private fun cargarMisListas() {
        Log.i("Inicio", "Cargando listas en tiempo real")
        progressBar.visibility = View.VISIBLE

        usuarioService.getMisListasByUsuarioIdFlow(sessionManager.getUserId().toString(), listaService)
            .onEach { listas ->
                Log.i("Inicio", listas.size.toString())
                adapter = ListaAdapter(listas) { lista ->
                    abrirLista(lista.id)
                }
                recyclerViewMisListas.adapter = adapter
                progressBar.visibility = View.GONE
            }
            .catch { e ->
                Log.e("Error", "Error al cargar listas: ${e.message}")
                progressBar.visibility = View.GONE
            }
            .launchIn(lifecycleScope)
    }

    private fun cargarListasCompartidas() {
        Log.i("Inicio", "Cargando listas compartidas en tiempo real")
        progressBar2.visibility = View.VISIBLE

        usuarioService.getListasCompartidasByUsuarioIdFlow(sessionManager.getUserId().toString(), listaService)
            .onEach { listas ->
                Log.i("Inicio", listas.size.toString())
                adapter = ListaAdapter(listas) { lista ->
                    abrirLista(lista.id)
                }
                recyclerViewListasCompartidas.adapter = adapter
                progressBar2.visibility = View.GONE
            }
            .catch { e ->
                Log.e("Error", "Error al cargar listas compartidas: ${e.message}")
                progressBar2.visibility = View.GONE
            }
            .launchIn(lifecycleScope)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}