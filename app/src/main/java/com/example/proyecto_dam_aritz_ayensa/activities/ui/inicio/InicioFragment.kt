package com.example.proyecto_dam_aritz_ayensa.activities.ui.inicio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.activities.BottomNavigationActivity
import com.example.proyecto_dam_aritz_ayensa.activities.CrearListaFragment
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
import kotlinx.coroutines.launch

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null

    lateinit var buttonAñadirLista : Button
    lateinit var buttonRecargarMisListas : ImageButton
    lateinit var buttonRecargarListasCompartidas : ImageButton
    private lateinit var recyclerViewMisListas: RecyclerView
    private lateinit var recyclerViewListasCompartidas: RecyclerView
    private lateinit var adapter: ListaAdapter


    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var notificacionesService: NotificacionService
    private lateinit var sessionManager: SessionManager
    private lateinit var progressBar : ProgressBar
    private lateinit var progressBar2 : ProgressBar

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
        buttonRecargarMisListas = binding.inicioBtnRecargarListas
        if (buttonRecargarMisListas != null) {
            buttonRecargarMisListas.setOnClickListener {
                cargarMisListas()
            }
        }
        buttonRecargarListasCompartidas = binding.inicioBtnRecargarListasCompartidas
        if (buttonRecargarListasCompartidas != null) {
            buttonRecargarListasCompartidas.setOnClickListener {
                cargarListasCompartidas()
            }
        }
        buttonAñadirLista = binding.btnAnadirLista
        if (buttonAñadirLista != null) {
            buttonAñadirLista.setOnClickListener {
                abrirAñadirLista()
            }
        }
    }
    private fun cargarMisListas() {

        Log.i("Inicio", "Cargando listas")
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            val idListas =
                usuarioService.getIdMisListasByIdUsuario(sessionManager.getUserId().toString())
            val misListas = listaService.getMisListasByUsuarioId(idListas)

            Log.i("Inicio", misListas.size.toString())
            Log.i("Numero listas", usuarioService.getMisListasSizeByIdUsuario(sessionManager.getUserId().toString()).toString())
            adapter = ListaAdapter(misListas) { lista ->
                abrirLista(lista.id)
            }
            recyclerViewMisListas.adapter = adapter
            progressBar.visibility = View.GONE
        }
    }
    private fun cargarListasCompartidas() {
        Log.i("Inicio", "Cargando listas compartidas")
        lifecycleScope.launch {
            progressBar2.visibility = View.VISIBLE
            val idListasCompartidas =
                usuarioService.getIdListasCompartidasByIdUsuario(sessionManager.getUserId().toString())
            val listasCompartidas = listaService.getMisListasByUsuarioId(idListasCompartidas)

            Log.i("Inicio", listasCompartidas.size.toString())
            Log.i("Numero listas", usuarioService.getMisListasSizeByIdUsuario(sessionManager.getUserId().toString()).toString())
            adapter = ListaAdapter(listasCompartidas) { lista ->
                abrirLista(lista.id)
            }

            recyclerViewListasCompartidas.adapter = adapter
            progressBar2.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}