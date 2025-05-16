package com.example.proyecto_dam_aritz_ayensa.activities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentCrearListaBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import kotlinx.coroutines.launch


/**
 * Fragmento: CrearListaFragment
 *
 * Permite al usuario crear una nueva lista con título, descripción y color.
 */
class CrearListaFragment : Fragment() {

    private var _binding: FragmentCrearListaBinding? = null
    private val binding get() = _binding!!

    lateinit var buttonAñadirLista : Button
    lateinit var buttonCancelar : Button
    lateinit var buttonColor : Button

    private lateinit var lisaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var notificacionService: NotificacionService
    private lateinit var sessionManager: SessionManager

    private var colorSeleccionado: String = "#FF0000"

    private lateinit var idUsuario: String

    private lateinit var inputTitulo: EditText
    private lateinit var inputDescripcion: EditText
    /**
     * Método: onCreateView
     *
     * Inicializa la vista del fragmento y configura los componentes.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrearListaBinding.inflate(inflater, container, false)
        lisaService = ListaService(ListaDAO())

        notificacionService = NotificacionService(NotificacionDAO())
        usuarioService = UsuarioService(UsuarioDAO(), notificacionService)
        sessionManager = SessionManager(requireContext())
        idUsuario = sessionManager.getUserId().toString()



        buttonAñadirLista = binding.crearListaBtnCrearLista
        buttonCancelar = binding.crearListaBtnCancelar
        buttonColor = binding.crearListaBtnColor


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

        buttonColor.setOnClickListener {
            val popupMenu = PopupMenu(context, buttonColor)
            popupMenu.menuInflater.inflate(R.menu.color_menu, popupMenu.menu)

            // Establecer el listener para las opciones del menú
            popupMenu.setOnMenuItemClickListener { menuItem ->
                colorSeleccionadoTarea(menuItem)
                true
            }
            popupMenu.show()
        }

        return binding.root
    }
    /**
     * Método: crearLista
     *
     * Valida los campos y crea una nueva lista si es posible.
     */
    private fun crearLista() {
        val textTitulo = inputTitulo.text.toString().trim()

        val textDescripcion = inputDescripcion.text.toString().trim()

        if (textTitulo.isNotEmpty() && textDescripcion.isNotEmpty()) {
            val lista = Lista()
            lista.titulo = textTitulo
            lista.descripcion = textDescripcion
            lista.color = colorSeleccionado
            lista.idCreador = idUsuario

            lifecycleScope.launch {
                try {
                    if (usuarioService.getMisListasSizeByIdUsuario(idUsuario) < 6) {
                        val idLista = lisaService.saveLista(lista)
                        usuarioService.añadirListaAUsuario(idLista, idUsuario)
                        cancelar()
                        Utils.mostrarMensaje(requireContext(), "Lista creada correctamente")
                    }else {
                        Utils.mostrarMensaje(requireContext(), "No puedes crear mas de 6 listas")
                    }

                } catch (e: Exception) {
                    Log.e("CrearListaFragment", "Error: ${e.message}")
                    Utils.mostrarMensaje(requireContext(), "Error al crear la lista")
                }
            }


        } else {
            Utils.mostrarMensaje(requireContext(),"Ingrese un titulo y una descripción")
        }
    }
    /**
     * Método: seleccionarColor
     *
     * Asigna el color seleccionado al botón y a la lista.
     */
    private fun colorSeleccionadoTarea(menuItem: MenuItem) {
        val color = when (menuItem.itemId) {
            R.id.menuColor_it_colorRojo -> "#FF0000"
            R.id.menuColor_it_colorAzul -> "#0000FF"
            R.id.menuColor_it_colorVerde -> "#008000"
            R.id.menuColor_it_colorAmarillo -> "#FFFF00"
            R.id.menuColor_it_colorNaranja -> "#FFA500"
            else -> "#FF0000"
        }

        colorSeleccionado = color
        binding.crearListaBtnColor.setBackgroundColor(Color.parseColor(color))
    }
    /**
     * Método: cancelar
     *
     * Cierra el fragmento actual y vuelve al anterior.
     */
    private fun cancelar() {
       parentFragmentManager.popBackStack()
    }
    /**
     * Método: onDestroyView
     *
     * Libera los recursos del binding al destruir la vista.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}