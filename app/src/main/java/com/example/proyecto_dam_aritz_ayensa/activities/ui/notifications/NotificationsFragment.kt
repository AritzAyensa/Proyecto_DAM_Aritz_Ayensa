package com.example.proyecto_dam_aritz_ayensa.activities.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.adapters.NotificacionAdapter
import com.example.proyecto_dam_aritz_ayensa.adapters.ProductoParaAnadirAdapter
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentNotificationsBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.ProductoDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.model.service.ProductoService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager
    private lateinit var productoService: ProductoService
    private lateinit var notificacionService: NotificacionService
    private lateinit var recyclerViewNotificaciones: RecyclerView


    private lateinit var btnEliminarNotificacion: Button
    private lateinit var btnLeerNotificacion: Button

    private var notificaciones: List<Notificacion> = emptyList()
    private val notificacionesSeleccionadas = mutableListOf<String>()

    private lateinit var progressBar : ProgressBar
    private lateinit var adapter: NotificacionAdapter
    private lateinit var userId : String
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())
        notificacionService = NotificacionService(NotificacionDAO())
        usuarioService = UsuarioService(UsuarioDAO(), notificacionService)
        productoService = ProductoService(ProductoDAO())
        sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId().toString()
        progressBar = binding.loadingSpinner

        btnEliminarNotificacion = binding.btnEliminarNotificaciones
        btnEliminarNotificacion.setOnClickListener{
            eliminarNotificacion()
        }


        btnLeerNotificacion = binding.btnLeerNotificaciones
        btnLeerNotificacion.setOnClickListener{
            leerNotificacion()
        }
        recyclerViewNotificaciones = binding.recyclerNotificaciones


        cargarNotificaciones()

        return binding.root
    }

    private fun cargarNotificaciones() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            usuarioService.notificacionesUsuarioFlow(userId)
                .flowWithLifecycle(lifecycle)
                .collect { notificaciones ->
                    progressBar.visibility = View.GONE

                    adapter = NotificacionAdapter(
                        notificaciones,
                        onItemClick = { Utils.mostrarMensaje(requireContext(), "click") },
                        onCheckClick = { notif, isSelected ->
                            if (isSelected) notificacionesSeleccionadas.add(notif.id)
                            else            notificacionesSeleccionadas.remove(notif.id)
                        }
                    )

                    recyclerViewNotificaciones.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = this@NotificationsFragment.adapter
                    }
                }
        }
    }


    private fun eliminarNotificacion() {
        if (notificacionesSeleccionadas.isEmpty()) {
            Utils.mostrarMensaje(requireContext(), "Seleccione al menos una notificación")
            return
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Eliminar notificaciones seleccionadas")
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Eliminar") { _, _ -> }

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                lifecycleScope.launch {
                    usuarioService.eliminarNotificacionesDeUsuarios(userId, notificacionesSeleccionadas)
                    dialog.dismiss()
                }
            }
        }

        dialog.window?.apply {
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.85).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        dialog.show()
    }

    private fun leerNotificacion() {
        if (notificacionesSeleccionadas.isEmpty()){
            Utils.mostrarMensaje(requireContext(), "Seleccione al menos una notificacion")
        }else{
            lifecycleScope.launch {
                usuarioService.marcarNotificacionesComoLeidas(userId, notificacionesSeleccionadas)
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}