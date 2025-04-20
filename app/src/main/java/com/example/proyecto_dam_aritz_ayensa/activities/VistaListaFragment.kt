package com.example.proyecto_dam_aritz_ayensa.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.adapters.ProductoAdapter
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentVistaListaBinding
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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VistaListaFragment : Fragment() {

    private var _binding: FragmentVistaListaBinding? = null
    private val binding get() = _binding!!


    lateinit var buttonAñadirProducto : Button
    lateinit var buttonCompartirLista : Button
    lateinit var buttonEliminarLista : Button

    private lateinit var recyclerViewProductos: RecyclerView
    private lateinit var adapter: ProductoAdapter


    private lateinit var tvTituloLista: TextView
    private lateinit var lista: Lista
    private lateinit var producto: Producto

    private lateinit var idLista: String
    private lateinit var userId : String


    private lateinit var listaService: ListaService
    private lateinit var productoService: ProductoService
    private var listaProductos: List<Producto> = emptyList()
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVistaListaBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())
        productoService = ProductoService(ProductoDAO())
        usuarioService = UsuarioService(UsuarioDAO())
        sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId().toString()

        //Obtener el id de la lista
        arguments?.let {
            idLista = it.getString("idLista").toString()
        }

        recyclerViewProductos = binding.recyclerProductos
        tvTituloLista = binding.vistaListaTvTitulo

        cargarLista()
        cargarBotones()


        return binding.root
    }

    private fun cargarBotones() {
        buttonCompartirLista = binding.btnCompartirLista

        if (buttonCompartirLista != null) {
            buttonCompartirLista.setOnClickListener {
                compartirLista(requireContext())
            }
        }

        buttonAñadirProducto = binding.btnAnadirProducto
        if (buttonAñadirProducto != null) {
            buttonAñadirProducto.setOnClickListener {
                añadirProducto()
            }
        }



        buttonEliminarLista = binding.btnEliminarLista
        if (buttonEliminarLista != null) {
            buttonEliminarLista.setOnClickListener {
                eliminarLista(requireContext())
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun compartirLista(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_email_input, null)
        val inputEmail = dialogView.findViewById<EditText>(R.id.input_email)
        val textoError = dialogView.findViewById<TextView>(R.id.texto_error)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Compartir lista")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .create()

        // Configurar manualmente el botón positivo
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Aceptar") { _, _ -> } // Empty listener

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val email = inputEmail.text.toString().trim()

                when {
                    (email.isEmpty()) -> {
                        textoError.visibility = View.VISIBLE
                        textoError.text = "El campo no puede estar vacío"
                        inputEmail.requestFocus()
                    }
                    !Utils.comprobarCorreo(email) -> {
                        textoError.visibility = View.VISIBLE
                        textoError.text = "Correo incorrecto"
                        inputEmail.requestFocus()
                    }
                    email == sessionManager.getUserEmail() ->{
                        textoError.visibility = View.VISIBLE
                        textoError.text = "Introduce otro correo"
                        inputEmail.requestFocus()
                    }
                    else -> {
                        usuarioService.getUserByEmail(email,
                            onSuccess = { usuario ->
                                lifecycleScope.launch {
                                    if (usuario != null) {
                                        if(usuario.idListasCompartidas.contains(idLista)){
                                            Log.i("USUARIO", "ASDASDASD")
                                            textoError.visibility = View.VISIBLE
                                            textoError.text = "Ya has compartido la lista con este usuario anteriormente"
                                            inputEmail.requestFocus()
                                        }else{

                                            Log.i("USUARIO", "compartida")
                                            usuarioService.añadirListaCompartidaAUsuario(
                                                idLista,
                                                usuario.id
                                            )
                                            dialog.dismiss()
                                        }
                                    }else{
                                        textoError.visibility = View.VISIBLE
                                        textoError.text = "Correo no encontrado"
                                        inputEmail.requestFocus()
                                    }
                                }
                            },
                            onFailure = {
                                textoError.visibility = View.VISIBLE
                                textoError.text = "Correo no encontrado"
                                inputEmail.requestFocus()
                            }
                        )
                    }
                }

                // Mostrar teclado si hay error
                if (email.isEmpty() || !Utils.comprobarCorreo(email)) {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(inputEmail, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        // Configuración de teclado y tamaño
        dialog.window?.apply {
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.85).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        dialog.show()
    }

        private fun eliminarLista(context: Context) {
            val dialog = AlertDialog.Builder(context)
                .setTitle("Eliminar lista")
                .setNegativeButton("Cancelar", null)
                .create()
            if (lista.idCreador != userId){
               dialog.setTitle("Eliminar lista de listas compartidas")
            }


            // Configurar manualmente el botón positivo
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Eliminar") { _, _ -> } // Empty listener

            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setOnClickListener {
                    if(lista.idCreador == userId){
                        lifecycleScope.launch {
                            usuarioService.eliminarListaAUsuario(idLista, userId)
                            usuarioService.eliminarListaCompartidaAUsuarios(idLista)
                            listaService.eliminarLista(idLista)

                            dialog.dismiss()
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                    }else{
                        lifecycleScope.launch {
                            usuarioService.eliminarListaCompartidaAUsuario(idLista, userId)

                            dialog.dismiss()
                            requireActivity().supportFragmentManager.popBackStack()

                        }
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


        private fun añadirProducto() {
            val dialog = AlertDialog.Builder(requireContext() , R.style.MyDialogTheme)
                .setTitle("Seleccionar acción")
                .setPositiveButton("Escanear código") { dialog, _ ->
                    escanearProducto()
                    dialog.dismiss()
                }
                .setNeutralButton("Buscar producto") { dialog, _ ->
                    abrirAñadirProducto()
                    dialog.dismiss()
                }
                .create()

            dialog.show()
        }
        private fun abrirAñadirProducto() {
            val bundle = Bundle().apply {
                putString("idLista", idLista)
            }
            findNavController().navigate(R.id.action_vista_listaFragment_to_añadir_productoFragment, bundle)

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
                val codigoEscaneado = result.contents.toString()
                if (!codigoEscaneado.matches(Regex("\\d+"))) {
                    Utils.mostrarMensaje(requireContext(), "Código no válido")
                }else{
                    val producto = productoService.getProductoPorCodigoBarras(codigoEscaneado)
                    val lista = listaService.getListaById(idLista)
                    if(producto != null){
                        if (lista != null) {
                            if(lista.idProductos.contains(producto.id)) {
                                Utils.mostrarMensaje(requireContext(), "La lista ya contiene " + producto.nombre)
                            }else{
                                listaService.añadirProductoALista(producto.id, idLista)
                                Utils.mostrarMensaje(requireContext(), "Producto añadido: " + producto.nombre)
                            }
                        }
                    }else{
                        Utils.mostrarMensaje(requireContext(), "Producto no encontrado: " + codigoEscaneado)
                    }
                }
            }

        }
    }

        /*private fun cargarLista() {
            lifecycleScope.launch {
                lista = listaService.getListaById(idLista)!!
                if (lista.idCreador == userId){
                    buttonCompartirLista.visibility = View.VISIBLE
                }
                actualizarVista()
                listaProductos = productoService.getProductosByIds(lista.idProductos)
            }
            adapter = ProductoAdapter(listaProductos) { producto ->
                abrirProducto(producto.id)
            }

        }*/
        private fun cargarLista() {
            lifecycleScope.launch {
                try {
                    // 1. Evitar !! (operador no-nulo inseguro)
                    val listaObtenida = listaService.getListaById(idLista) ?: run {
                        Utils.mostrarMensaje(context, "Lista no encontrada")
                        return@launch
                    }

                    lista = listaObtenida

                    // 2. Actualizar UI en el hilo principal
                    withContext(Dispatchers.Main) {
                        buttonCompartirLista.visibility = if (lista.idCreador == userId) View.VISIBLE else View.GONE
                        actualizarVista()
                    }

                    // 3. Obtener productos de forma asíncrona
                    val productos = productoService.getProductosByIds(lista.idProductos).sorted()

                    // 4. Actualizar adapter en el hilo principal
                    withContext(Dispatchers.Main) {
                        if (::adapter.isInitialized) { // Si ya existe
                            adapter.actualizarProductos(productos)
                        } else {
                            adapter = ProductoAdapter(productos) { producto ->
                                abrirProducto(producto.id)
                            }
                            recyclerViewProductos.apply {
                                layoutManager = LinearLayoutManager(context)
                                adapter = this@VistaListaFragment.adapter
                            }
                        }
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Utils.mostrarMensaje(context, "Error: ${e.message}")
                    }
                }
            }
        }
    private fun abrirProducto(idProducto : String) {
        /*lifecycleScope.launch {
            if (listaService.getListaById(idLista) == null){
                Utils.mostrarMensaje(context, "Lista no encontrada")
            }else{
                val bundle = Bundle().apply {
                    putString("idLista", idLista)
                }
                findNavController().navigate(R.id.action_inicioFragment_to_vistaListaFragment, bundle)
            }
        }*/
    }

        private fun actualizarVista() {
            tvTituloLista.text = lista.titulo
        }

        private fun actualizarLista() {
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

}