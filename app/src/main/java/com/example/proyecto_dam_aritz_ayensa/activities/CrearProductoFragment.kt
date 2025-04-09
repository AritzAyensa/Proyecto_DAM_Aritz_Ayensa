package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentAnadirProductoBinding
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentCrearListaBinding
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentCrearProductoBinding
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
import kotlinx.coroutines.launch
import java.text.DecimalFormat


class CrearProductoFragment : Fragment() {
    private var _binding: FragmentCrearProductoBinding? = null
    private val binding get() = _binding!!
    private lateinit var lista: Lista

    private lateinit var idLista: String
    private lateinit var userId : String

    private lateinit var spinner: Spinner
    private lateinit var inputNombre: EditText
    private lateinit var inputPrecio: EditText
    private lateinit var inputPrioridad: EditText

    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var productoService: ProductoService
    private lateinit var sessionManager: SessionManager


    private lateinit var buttonCrearProducto : Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearProductoBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())
        usuarioService = UsuarioService(UsuarioDAO())
        productoService = ProductoService(ProductoDAO())
        sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId().toString()


        spinner = binding.spinnerCategorias
        inputNombre = binding.crearProductoEtNombre
        inputPrecio = binding.crearProductoEtPrecio
        inputPrioridad = binding.crearProductoEtPrioridad

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
                crearProducto()
            }
        }
    }

    private fun crearProducto() {
        try{
            val textNombre = inputNombre.text.toString().trim()

            val precio: Double = inputPrecio.text.toString().toDouble()
            val prioridad: Double = inputPrioridad.text.toString().toDouble()

            if (textNombre.isNotEmpty() && precio > 0  && prioridad > 0) {
                val producto = Producto()
                producto.nombre = textNombre
                producto.precioAproximado = precio
                producto.prioridad = prioridad
                producto.categoria = spinner.selectedItem.toString()
                producto.idCreador = userId

                lifecycleScope.launch {
                    try {
                        var idProducto: String = productoService.saveProducto(producto).toString()
                        listaService.añadirProductoALista(idProducto, idLista)
                        cancelar()
                        Utils.mostrarMensaje(requireContext(), "Producto creado correctamente")
                    } catch (e: Exception) {
                        Log.e("CrearProductoFragment", "Error: ${e.message}")
                        Utils.mostrarMensaje(requireContext(), "Error al crear el producto")
                    }
                }
            } else {
                Utils.mostrarMensaje(requireContext(), "Complete todos los campos")
            }
        }catch (e : Error){
            Utils.mostrarMensaje(requireContext(), e.message.toString())
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