package com.example.proyecto_dam_aritz_ayensa.activities.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
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
import com.example.proyecto_dam_aritz_ayensa.utils.GenericConstants
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager
    private lateinit var productoService: ProductoService
    private lateinit var notificacionService: NotificacionService
    private lateinit var recyclerViewNotificaciones: RecyclerView
    private var bloqueado = false

    private lateinit var btnEliminarNotificacion: Button

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

        recyclerViewNotificaciones = binding.recyclerNotificaciones


        cargarNotificaciones()

        return binding.root
    }

    private fun cargarNotificaciones() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            usuarioService.notificacionesUsuarioFlow(userId)
                .flowWithLifecycle(lifecycle)
                .collectLatest { notificaciones ->
                    if (bloqueado) return@collectLatest

                    progressBar.visibility = View.GONE
                    adapter = NotificacionAdapter(
                        notificaciones.sorted(),
                        onItemClick = { notif -> mostrarDetalleNotificacion(notif) },
                        onCheckClick = { notif, isSelected ->
                            if (isSelected) notificacionesSeleccionadas.add(notif.id)
                            else            notificacionesSeleccionadas.remove(notif.id)
                        }
                    )

                    recyclerViewNotificaciones.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = this@NotificationsFragment.adapter
                    }

                    lifecycleScope.launch {
                        usuarioService.marcarNotificacionesComoLeidas(userId, notificaciones.map { it.id })
                    }
                }
        }
    }


    private fun eliminarNotificacion() {
        if (notificacionesSeleccionadas.isEmpty()) {
            Utils.mostrarMensaje(requireContext(), "Seleccione al menos una notificación")
            return
        }

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyDialogTheme)
            .setTitle("Eliminar notificaciones seleccionadas")
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Eliminar") { _, _ -> }

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                lifecycleScope.launch {
                    bloqueado = true
                    usuarioService.eliminarNotificacionesDeUsuarios(userId, notificacionesSeleccionadas)
                    notificacionesSeleccionadas.clear()
                    delay(500)
                    bloqueado = false

                    // Fuerza recarga manual
                    usuarioService.notificacionesUsuarioFlow(userId)
                        .firstOrNull()?.let { notificaciones ->
                            adapter = NotificacionAdapter(
                                notificaciones,
                                onItemClick = { notif -> mostrarDetalleNotificacion(notif) },
                                onCheckClick = { notif, isSelected ->
                                    if (isSelected) notificacionesSeleccionadas.add(notif.id)
                                    else notificacionesSeleccionadas.remove(notif.id)
                                }
                            )
                            recyclerViewNotificaciones.adapter = adapter
                        }

                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }



    fun mostrarDetalleNotificacion(notificacion: Notificacion) {
        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.MyDialogTheme)

        val contentLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val scrollView = ScrollView(requireContext()).apply {
            addView(contentLayout)
        }

        val descripcionView = TextView(requireContext()).apply {
            text = "${notificacion.descripcion}"
        }

        val fechaView = TextView(requireContext()).apply {
            val date = notificacion.fecha?.toDate()
            val formato = SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault())
            text = if (date != null) {
                "Fecha: ${formato.format(date)}"
            } else {
                "Fecha no disponible"
            }
        }

        contentLayout.addView(descripcionView)
        contentLayout.addView(fechaView)

        if (notificacion.tipo == GenericConstants.TIPO_COMPRA) {
            lifecycleScope.launch {
                val productos = productoService.getProductosByIds(notificacion.idProductos)
                val totalPrecio = productos.sumOf { it.precioAproximado }

                val productosTitulo = TextView(requireContext()).apply {
                    text = "Productos:"
                    setPadding(0, 20, 0, 10)
                }

                val precioTotalView = TextView(requireContext()).apply {
                    text = "Precio total: %.2f €".format(totalPrecio)
                    setPadding(0, 10, 0, 10)
                }

                withContext(Dispatchers.Main) {
                    contentLayout.addView(productosTitulo)
                    productos.forEach { producto ->
                        val productoView = TextView(requireContext()).apply {
                            text = "- ${producto.nombre}"
                        }
                        contentLayout.addView(productoView)
                    }
                    contentLayout.addView(precioTotalView)
                }
            }
        }

        builder.setView(scrollView)
            .setPositiveButton("Cerrar", null)
            .show()
    }






    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}