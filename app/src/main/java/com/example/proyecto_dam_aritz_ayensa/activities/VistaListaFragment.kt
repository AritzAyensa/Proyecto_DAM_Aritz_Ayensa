package com.example.proyecto_dam_aritz_ayensa.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.adapters.NotificacionAdapter
import com.example.proyecto_dam_aritz_ayensa.adapters.ProductoAdapter
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentVistaListaBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.ProductoDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class VistaListaFragment : Fragment() {

    private var _binding: FragmentVistaListaBinding? = null
    private val binding get() = _binding!!


    /*lateinit var buttonAñadirProducto : Button
    lateinit var buttonCompartirLista : Button
    lateinit var buttonEliminarLista : Button*/
    lateinit var buttonOpciones : Button
    lateinit var buttonCompletarCompra : Button
    lateinit var buttonEscanear : ImageButton

    private lateinit var recyclerViewProductos: RecyclerView
    private lateinit var adapter: ProductoAdapter


    private lateinit var tvTituloLista: TextView
    private lateinit var lista: Lista
    private lateinit var productoParaAñadir: Producto

    private lateinit var idLista: String
    private lateinit var userId : String


    private lateinit var listaService: ListaService
    private lateinit var productoService: ProductoService
    private lateinit var notificacionesService: NotificacionService
    private var listaProductos: MutableList<Producto> = mutableListOf()
    private val productosSeleccionadas = mutableListOf<String>()
    private var todosLosProductos = mutableListOf<Producto>()
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager
    private lateinit var progressBar : ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVistaListaBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())
        productoService = ProductoService(ProductoDAO())
        usuarioService = UsuarioService(UsuarioDAO())
        notificacionesService = NotificacionService(NotificacionDAO())
        sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId().toString()
        progressBar = binding.loadingSpinner

        //Obtener el id de la lista
        arguments?.let {
            idLista = it.getString("idLista").toString()
        }

        recyclerViewProductos = binding.recyclerProductos
        tvTituloLista = binding.vistaListaTvTitulo

        cargarBotones()
        configurarDropdownMenu()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        cargarLista()
    }


    private fun cargarBotones() {
        buttonOpciones = binding.btnOpciones

        if (buttonOpciones != null) {
            buttonOpciones.setOnClickListener {
                opciones()
            }
        }

        buttonEscanear = binding.btnEscanear

        if (buttonEscanear != null) {
            buttonEscanear.setOnClickListener {
                escanearProducto()
            }
        }

        buttonCompletarCompra = binding.btnCompletarCompra

        if (buttonCompletarCompra != null) {
            buttonCompletarCompra.setOnClickListener {
                completarCompra()
            }
        }
    }

    private fun configurarDropdownMenu() {
        lifecycleScope.launch {
            todosLosProductos = productoService.getProductos().toMutableList()

            val adapterDropdown = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, todosLosProductos)
            val autoCompleteTextView = binding.autoCompleteTextView
            autoCompleteTextView.setAdapter(adapterDropdown)

            autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->

                productoParaAñadir = parent.getItemAtPosition(position) as Producto
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        if (lista.idProductos.contains(productoParaAñadir.id)) {
                            withContext(Dispatchers.Main) {
                                Utils.mostrarMensaje(
                                    requireContext(),
                                    "El producto ya está en la lista"
                                )
                            }
                        } else {
                            listaService.añadirProductoALista(productoParaAñadir.id, idLista)
                            // Actualizar lista local
                            lista = listaService.getListaById(idLista)!!
                            listaProductos = productoService.getProductosByIds(lista.idProductos).toMutableList()
                            activity?.runOnUiThread {
                                adapter.actualizarProductos(listaProductos)
                            }


                            withContext(Dispatchers.Main) {
                                Utils.mostrarMensaje(
                                    requireContext(),
                                    "Producto añadido"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Utils.mostrarMensaje(
                                requireContext(),
                                "Error: ${e.message}"
                            )
                        }
                    }
                }
            }
        }

    }

    private fun opciones() {
        var dialog : Dialog = Dialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_opciones_lista, null)
        // Obtén referencias a los botones:
        val btnAñadir = view.findViewById<Button>(R.id.btnAñadirCrear)
        val btnCompartir = view.findViewById<Button>(R.id.btnCompartir)
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminar)

        // Activa o desactiva compartir según condición:
        btnCompartir.visibility =
            if (lista.idCreador == userId) View.VISIBLE else View.GONE

        // Define listeners:
        btnAñadir.setOnClickListener {
            abrirAñadirProducto()
            dialog.dismiss()
        }
        btnCompartir.setOnClickListener {
            compartirLista(requireContext())
            dialog.dismiss()
        }
        btnEliminar.setOnClickListener {
            eliminarLista(requireContext())
            dialog.dismiss()
        }

        // Construye el AlertDialog usando el view personalizado
        dialog = AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
            .setView(view)
            .create()
        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun completarCompra() {
        try{
            lifecycleScope.launch(Dispatchers.IO) {
                listaService.eliminarProductosDeLista(idLista, productosSeleccionadas.toList())
                lista = listaService.getListaById(idLista)!!
                listaProductos = productoService.getProductosByIds(lista.idProductos).toMutableList()
                activity?.runOnUiThread {
                    adapter.actualizarProductos(listaProductos)
                }


                var nombreUsuario = usuarioService.getUserNameById(userId)
                var notificacion = Notificacion()
                notificacion.tipo = GenericConstants.TIPO_COMPRA

                notificacion.descripcion = nombreUsuario + " ha completado la compra " + lista.titulo
                notificacion.idsUsuarios += lista.idsUsuariosCompartidos
                notificacion.idsUsuarios += userId
                notificacion.idProductos += lista.idProductos

                val fechaActual = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                notificacion.fecha = fechaActual.format(formatter)

                notificacionesService.saveNotificacion(notificacion)
                withContext(Dispatchers.Main) {
                    Utils.mostrarMensaje(requireContext(), "Compra completada")
                }
            }
        }catch (e : Error){
            Utils.mostrarMensaje(requireContext(), e.message.toString())
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
                                            textoError.visibility = View.VISIBLE
                                            textoError.text = "Ya has compartido la lista con este usuario anteriormente"
                                            inputEmail.requestFocus()
                                        }else{

                                            Log.i("USUARIO", "compartida")
                                            usuarioService.añadirListaCompartidaAUsuario(
                                                idLista,
                                                usuario.id
                                            )
                                            var nombreUsuario = usuarioService.getUserNameById(userId)
                                            var notificacion = Notificacion()
                                            notificacion.tipo = GenericConstants.TIPO_LISTA_COMPRATIDA

                                            notificacion.descripcion = nombreUsuario + " ha compartido la lista " + lista.titulo + " con " + usuario.nombre
                                            notificacion.idsUsuarios += usuario.id
                                            notificacion.idsUsuarios += userId

                                            val fechaActual = LocalDate.now()
                                            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                            notificacion.fecha = fechaActual.format(formatter)


                                            notificacionesService.saveNotificacion(notificacion)
                                            Utils.mostrarMensaje(requireContext(), "Lista "+ lista.titulo + " compartida con " + usuario.nombre)
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

        private fun ordenarProductos(lista: MutableList<Producto>): MutableList<Producto> {
            return lista
                .sortedWith(compareBy<Producto> { productosSeleccionadas.contains(it.id) }
                    .thenBy { it.prioridad }).toMutableList()
        }
        @SuppressLint("NotifyDataSetChanged")
        private fun cargarLista() {
            lifecycleScope.launch {
                progressBar.visibility = View.VISIBLE
                try {
                    // 1. Evitar !! (operador no-nulo inseguro)
                    val listaObtenida = listaService.getListaById(idLista) ?: run {
                        Utils.mostrarMensaje(context, "Lista no encontrada")
                        return@launch
                    }

                    lista = listaObtenida

                    // 2. Actualizar UI en el hilo principal
                    /*withContext(Dispatchers.Main) {
                        buttonCompartirLista.visibility = if (lista.idCreador == userId) View.VISIBLE else View.GONE
                        actualizarVista()
                    }*/
                    // 3. Obtener productos de forma asíncrona
                    listaProductos = ordenarProductos(productoService.getProductosByIds(lista.idProductos).toMutableList())

                    // 4. Actualizar adapter en el hilo principal
                    withContext(Dispatchers.Main) {
                            /*adapter = ProductoAdapter(listaProductos,) { producto ->
                                abrirProducto(producto.id)
                            }*/
                            adapter = ProductoAdapter(
                                listaProductos,
                                onItemClick = { Utils.mostrarMensaje(requireContext(), "click") },
                                onCheckClick = { producto, isSelected ->
                                    if (isSelected) productosSeleccionadas.add(producto.id)
                                    else            productosSeleccionadas.remove(producto.id)
                                    listaProductos = ordenarProductos(listaProductos)
                                    /*adapter.actualizarProductos(listaProductos)*/
                                    adapter.actualizarProductos(listaProductos)
                                }
                            )
                            recyclerViewProductos.apply {
                                layoutManager = LinearLayoutManager(context)
                                adapter = this@VistaListaFragment.adapter
                            }
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Utils.mostrarMensaje(context, "Error: ${e.message}")
                    }
                }

                progressBar.visibility = View.GONE
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}