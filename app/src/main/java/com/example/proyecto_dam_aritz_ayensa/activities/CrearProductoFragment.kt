package com.example.proyecto_dam_aritz_ayensa.activities

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentCrearProductoBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.ProductoDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.model.service.ProductoService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.GenericConstants
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch


class CrearProductoFragment : Fragment() {
    private var _binding: FragmentCrearProductoBinding? = null
    private val binding get() = _binding!!
    private lateinit var lista: Lista

    private lateinit var idLista: String
    private lateinit var userId : String
    private var codigoBarras : String = ""

    private lateinit var spinner: Spinner
    private lateinit var inputNombre: EditText
    private lateinit var inputPrecio: EditText

    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var notificacionService: NotificacionService
    private lateinit var productoService: ProductoService
    private lateinit var sessionManager: SessionManager


    private lateinit var buttonCrearProducto : Button
    private lateinit var buttonEscanearProducto : Button
    private lateinit var buttonCancelar : Button

    private var categoriaSeleccionada: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearProductoBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())

        notificacionService = NotificacionService(NotificacionDAO())
        usuarioService = UsuarioService(UsuarioDAO(), notificacionService)
        productoService = ProductoService(ProductoDAO())
        sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId().toString()


        inputNombre = binding.crearProductoEtNombre
        inputPrecio = binding.crearProductoEtPrecio

        //Obtener el id de la lista
        arguments?.let {
            idLista = it.getString("idLista").toString()
        }

        cargarBotones()
        configurarDropdownMenu()

        return binding.root
    }

    private fun cargarBotones() {
        buttonCrearProducto = binding.btnCrearProducto
        if (buttonCrearProducto != null) {
            buttonCrearProducto.setOnClickListener {
                crearProducto()
            }
        }
        buttonEscanearProducto = binding.btnEscanearCodigo
        if (buttonEscanearProducto != null) {
            buttonEscanearProducto.setOnClickListener {
                escanearProducto()
            }
        }
        buttonCancelar = binding.crearProductoBtnCancelar
        if (buttonCancelar != null) {
            buttonCancelar.setOnClickListener {
                cancelar()
            }
        }
    }

    private fun crearProducto() {
        try{
            val textNombre = inputNombre.text.toString().trim()
            var precio: Double = 0.0

            if (!inputPrecio.text.isNullOrEmpty()){
                precio = inputPrecio.text.toString().toDouble()
            }

            if (textNombre.isNotEmpty() && precio > 0 && categoriaSeleccionada.isNotEmpty() && GenericConstants.PRIORIDAD_CATEGORIAS.keys.contains(categoriaSeleccionada)) {
                val producto = Producto() 
                producto.nombre = textNombre
                producto.precioAproximado = precio
                producto.categoria = categoriaSeleccionada
                producto.idCreador = userId
                producto.codigoBarras = codigoBarras

                lifecycleScope.launch {
                    try {
                        var idProducto: String = productoService.saveProducto(producto)
                        listaService.añadirProductoALista(idProducto, idLista)
                        cancelar()
                        Utils.mostrarMensaje(requireContext(), "Producto creado correctamente")
                    } catch (e: Exception) {
                        Log.e("CrearProductoFragment", "Error: ${e.message}")
                        Utils.mostrarMensaje(requireContext(), "Error al crear el producto")
                    }
                }
            } else {
                Utils.mostrarMensaje(requireContext(), "Complete correctamente todos los campos")
            }
        }catch (e : Error){
            Utils.mostrarMensaje(requireContext(), e.message.toString())
        }
    }
    private fun configurarDropdownMenu() {
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, GenericConstants.PRIORIDAD_CATEGORIAS.keys.sorted().toList())
        val autoCompleteTextView = binding.autoCompleteTextView
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            categoriaSeleccionada = parent.getItemAtPosition(position) as String
        }
    }

    private fun escanearProducto() {
        try{
            val options = ScanOptions()
            options.setPrompt("Escanea un codigo de barras")
            options.setOrientationLocked(false)
            barcodeLauncher.launch(options)
        }catch (e : Error){
            Utils.mostrarMensaje(requireContext(), e.message.toString())
        }
    }
    private val barcodeLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Utils.mostrarMensaje(requireContext(), "Cancelado")
        } else {
            lifecycleScope.launch {
                val codigosBarras = productoService.getAllCodigosBarras()
                val codigoEscaneado = result.contents.toString()
                if (!codigoEscaneado.matches(Regex("\\d+"))) {
                    Utils.mostrarMensaje(requireContext(), "Código no válido")
                }else{
                    if(! codigosBarras.contains(codigoEscaneado)){
                        codigoBarras = codigoEscaneado
                        Utils.mostrarMensaje(requireContext(), "Codigo escaneado: " + codigoEscaneado)
                    }else{
                        Utils.mostrarMensaje(requireContext(), "Otro producto tiene este codigo: " + codigoEscaneado)
                    }
                }
            }

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