package com.example.proyecto_dam_aritz_ayensa.activities

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentCrearProductoBinding
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentEditarProductoBinding
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

/**
 * Clase: EditarProductoFragment
 *
 * Fragment para editar producto: nombre, precio, categoría y código de barras.
 * Permite escanear código y usar dropdown para categorías.
 */
class EditarProductoFragment : Fragment() {
    private var _binding: FragmentEditarProductoBinding? = null
    private val binding get() = _binding!!

    private lateinit var idProducto: String
    private lateinit var producto: Producto
    private lateinit var userId : String
    private var codigoBarras : String = ""

    private lateinit var spinner: Spinner
    private lateinit var inputNombre: EditText
    private lateinit var inputPrecio: EditText
    private lateinit var autoCompleteTextView: AutoCompleteTextView

    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var notificacionService: NotificacionService
    private lateinit var productoService: ProductoService
    private lateinit var sessionManager: SessionManager


    private lateinit var buttonCrearProducto : Button
    private lateinit var buttonEscanearProducto : Button
    private lateinit var buttonCancelar : Button

    private var categoriaSeleccionada: String = ""
    /**
     * onCreateView:
     * Infla layout, inicializa servicios, obtiene idProducto, carga datos y botones.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditarProductoBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())

        notificacionService = NotificacionService(NotificacionDAO())
        usuarioService = UsuarioService(UsuarioDAO(), notificacionService)
        productoService = ProductoService(ProductoDAO())
        sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId().toString()


        inputNombre = binding.editarProductoEtNombre
        inputPrecio = binding.editarProductoEtPrecio


        arguments?.let {
            idProducto = it.getString("idProducto").toString()
        }

        cargarProducto()
        cargarBotones()
        configurarDropdownMenu()

        return binding.root
    }
    /**
     * cargarBotones:
     * Configura listeners para botones editar, escanear código y cancelar.
     */
    private fun cargarBotones() {
        buttonCrearProducto = binding.btnEditarProducto
        if (buttonCrearProducto != null) {
            buttonCrearProducto.setOnClickListener {
                editarProducto()
            }
        }
        buttonEscanearProducto = binding.btnEscanearCodigo
        if (buttonEscanearProducto != null) {
            buttonEscanearProducto.setOnClickListener {
                escanearProducto()
            }
        }
        buttonCancelar = binding.editarProductoBtnCancelar
        if (buttonCancelar != null) {
            buttonCancelar.setOnClickListener {
                cancelar()
            }
        }
    }
    /**
     * cargarProducto:
     * Carga datos del producto y los muestra en la UI.
     */
    private fun cargarProducto() {
        try{
            lifecycleScope.launch {
                try {
                    producto = productoService.getProductoById(idProducto)!!

                    inputNombre.setText(producto.nombre)
                    inputPrecio.setText(producto.precioAproximado.toString())
                    autoCompleteTextView = binding.autoCompleteTextView
                    autoCompleteTextView.setText(producto.categoria)
                    categoriaSeleccionada = producto.categoria

                    if (!producto.codigoBarras.isEmpty()){
                        buttonEscanearProducto.text = "Cambiar código"
                    }
                } catch (e: Exception) {
                    Log.e("EditarProductoFragment", "Error: ${e.message}")
                    Utils.mostrarMensaje(requireContext(), "Error al cargar el producto")
                    cancelar()
                }


            }
        }catch (e : Error) {
            Log.e("EditarProductoFragment", "Error: ${e.message}")
            cancelar()
        }
    }

    /**
     * editarProducto:
     * Valida campos, actualiza producto y muestra mensaje éxito o error.
     */
    private fun editarProducto() {
        try{
            val textNombre = inputNombre.text.toString().trim()


            if (textNombre.isNotEmpty() && !inputPrecio.text.isNullOrEmpty() &&  inputPrecio.text.toString().toDouble() > 0 && categoriaSeleccionada.isNotEmpty() && GenericConstants.PRIORIDAD_CATEGORIAS.keys.contains(categoriaSeleccionada)) {

                producto.nombre = textNombre
                producto.precioAproximado = inputPrecio.text.toString().toDouble()
                producto.categoria = categoriaSeleccionada
                producto.idCreador = userId
                producto.codigoBarras = codigoBarras

                lifecycleScope.launch {
                    try {
                        productoService.updateProducto(producto)
                        Utils.mostrarMensaje(requireContext(), "Producto actualizado correctamente")
                        cancelar()
                    } catch (e: Exception) {
                        Log.e("EditarProductoFragment", "Error: ${e.message}")
                        Utils.mostrarMensaje(requireContext(), "Error al editar el producto")
                        cancelar()
                    }
                }
            } else {
                Utils.mostrarMensaje(requireContext(), "Complete correctamente todos los campos")
            }
        }catch (e : Error){
            Log.e("EditarProductoFragment", "Error: ${e.message}")
            cancelar()
        }
    }
    /**
     * configurarDropdownMenu:
     * Configura dropdown con categorías disponibles.
     */
    private fun configurarDropdownMenu() {
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, GenericConstants.PRIORIDAD_CATEGORIAS.keys.sorted().toList())
        val autoCompleteTextView = binding.autoCompleteTextView
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            categoriaSeleccionada = parent.getItemAtPosition(position) as String
        }
    }
    /**
     * escanearProducto:
     * Inicia escaneo de código de barras.
     */
    private fun escanearProducto() {
        try{
            val options = ScanOptions()
            options.setPrompt("Escanea un codigo de barras")
            options.setOrientationLocked(false)
            barcodeLauncher.launch(options)
            if (!codigoBarras.isEmpty()){
                buttonEscanearProducto.text = "Cambiar código"
            }
        }catch (e : Error){
            Utils.mostrarMensaje(requireContext(), e.message.toString())
        }
    }

    /**
     * barcodeLauncher:
     * Maneja resultado del escaneo, valida y asigna código si es único.
     */
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
    /**
     * cancelar:
     * Cierra el fragmento actual.
     */
    private fun cancelar() {
        parentFragmentManager.popBackStack()
    }
    /**
     * onDestroyView:
     * Limpia binding para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}