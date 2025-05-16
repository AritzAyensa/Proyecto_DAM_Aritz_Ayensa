package com.example.proyecto_dam_aritz_ayensa.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.os.Build
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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.adapters.ProductoAdapter
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentVistaListaBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionEmergenteDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.ProductoDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionEmergenteService
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.model.service.ProductoService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.GenericConstants
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FieldValue
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

    lateinit var buttonOpciones : Button
    lateinit var buttonCompletarCompra : Button
    lateinit var buttonEscanear : ImageButton
    lateinit var buttonAbrirMapa : ImageButton

    private lateinit var recyclerViewProductos: RecyclerView
    private lateinit var adapter: ProductoAdapter


    private lateinit var tvTituloLista: TextView
    private lateinit var lista: Lista
    private lateinit var productoParaAnadir: Producto

    private lateinit var idLista: String
    private lateinit var userId : String


    private lateinit var listaService: ListaService
    private lateinit var productoService: ProductoService
    private lateinit var notificacionesService: NotificacionService
    private lateinit var notificacionEmergenteService: NotificacionEmergenteService
    private var listaProductos: MutableList<Producto> = mutableListOf()
    private val productosSeleccionados = mutableListOf<String>()
    private var todosLosProductos = mutableListOf<Producto>()
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager
    private lateinit var progressBar : ProgressBar
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVistaListaBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())
        productoService = ProductoService(ProductoDAO())
        notificacionesService = NotificacionService(NotificacionDAO())
        notificacionEmergenteService = NotificacionEmergenteService(NotificacionEmergenteDAO())
        usuarioService = UsuarioService(UsuarioDAO(), notificacionesService)
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




        // 1. Configura RecyclerView aquí una sola vez
        binding.recyclerProductos.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProductoAdapter(
            listaProductos,
            onItemClick = {producto -> mostrarOpcionesEdicionEliminar(producto) },
            onCheckClick = { producto, isSelected ->
                // Alternar selección y reordenar
                if (isSelected) productosSeleccionados.add(producto.id)
                else            productosSeleccionados.remove(producto.id)

                listaProductos = ordenarProductos(listaProductos)
                adapter.actualizarProductos(listaProductos)
            }
        )
        recyclerViewProductos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@VistaListaFragment.adapter
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            val listaObtenida = withContext(Dispatchers.IO) {
                listaService.getListaById(idLista)
            } ?: run {
                withContext(Dispatchers.Main) {
                    Utils.mostrarMensaje(requireContext(), "Lista no encontrada")
                    progressBar.visibility = View.GONE
                }
                return@launch
            }
            lista = listaObtenida
            tvTituloLista.text = listaObtenida.titulo
            viewLifecycleOwner.lifecycle
                .repeatOnLifecycle(Lifecycle.State.STARTED) {
                    listaService
                        .productosDeListaFlow(idLista, productoService)
                        .collect { productosRaw ->
                            val ordenados = ordenarProductos(productosRaw.toMutableList())
                            listaProductos = ordenados;
                            adapter.actualizarProductos(ordenados)
                            progressBar.visibility = View.GONE

                        }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun cargarBotones() {
        buttonOpciones = binding.btnOpciones

        buttonOpciones.setOnClickListener {
            opciones()
        }

        buttonEscanear = binding.btnEscanear

        buttonEscanear.setOnClickListener {
            escanearProducto()
        }
        buttonAbrirMapa = binding.btnAbrirMapa

        buttonAbrirMapa.setOnClickListener {
            abrirMapa()
        }

        buttonCompletarCompra = binding.btnCompletarCompra

        buttonCompletarCompra.setOnClickListener {
            completarCompra()
        }
    }

    private fun configurarDropdownMenu() {
        lifecycleScope.launch {
            todosLosProductos = productoService.getProductos().toMutableList()

            val adapterDropdown = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, todosLosProductos.sortedBy { it.nombre.lowercase() })
            val autoCompleteTextView = binding.autoCompleteTextView
            autoCompleteTextView.setAdapter(adapterDropdown)

            autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->

                productoParaAnadir = parent.getItemAtPosition(position) as Producto
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        if (lista.idProductos.contains(productoParaAnadir.id)) {
                            withContext(Dispatchers.Main) {
                                Utils.mostrarMensaje(
                                    requireContext(),
                                    "El producto ya está en la lista"
                                )
                            }
                        } else {
                            listaService.añadirProductoALista(productoParaAnadir.id, idLista)
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

    @SuppressLint("InflateParams")
    private fun opciones() {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_opciones_lista, null)

        val btnBuscar = view.findViewById<Button>(R.id.btnBuscarProducto)
        val btnCrear = view.findViewById<Button>(R.id.btnCrearProducto)
        val btnCompartir = view.findViewById<Button>(R.id.btnCompartir)
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminar)

        btnCompartir.visibility =
            if (lista.idCreador == userId) View.VISIBLE else View.GONE

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyDialogTheme)
            .setView(view)
            .create()

        btnBuscar.setOnClickListener {
            abrirAnadirProducto()
            dialog.dismiss()
        }
        btnCrear.setOnClickListener {
            goToCrearProducto()
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

        dialog.show()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun completarCompra() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyDialogTheme)
            .setTitle("Completar compra")
            .setMessage("¿Estás seguro de que quieres completar esta compra?")
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirmar") { _, _ -> }

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        // 1) Eliminar productos de la lista y recargar datos
                        listaService.eliminarProductosDeLista(idLista, productosSeleccionados.toList())
                        lista = listaService.getListaById(idLista)!!
                        listaProductos = productoService
                            .getProductosByIds(lista.idProductos)
                            .toMutableList()

                        // 2) Actualizar UI
                        activity?.runOnUiThread {
                            adapter.actualizarProductos(listaProductos)
                        }

                        // 3) Construir datos de la noti interna
                        val nombreUsuario = usuarioService.getUserNameById(userId)
                        val notificacion = Notificacion(
                            tipo = GenericConstants.TIPO_COMPRA,
                            descripcion = "$nombreUsuario ha completado la compra ${lista.titulo}",
                            idProductos = productosSeleccionados
                        )

                        // 4) Guardar noti interna y asignar a usuarios
                        val idsUsuarios = usuarioService.getUserIdsByListId(lista.id)
                        val idNotificacion = notificacionesService.saveNotificacion(notificacion)
                        usuarioService.añadirNotificacionAUsuarios(idsUsuarios, idNotificacion)

                        // 5) Crear notificaciones emergentes FCM
                        idsUsuarios
                            .filter { it != userId }
                            .forEach { targetUid ->
                                val notiEmergenteData = mapOf(
                                    "toUid"     to targetUid,
                                    "fromUid"   to userId,
                                    "fromName"  to nombreUsuario,
                                    "listName"  to lista.titulo,
                                    "timestamp" to FieldValue.serverTimestamp(),
                                    "tipo"      to GenericConstants.TIPO_COMPRA
                                )
                                notificacionEmergenteService.saveNotificacionEmergente(
                                    notiEmergenteData,
                                    onSuccess = { },
                                    onError   = { }
                                )
                            }

                        // 6) Feedback y cierre de diálogo
                        withContext(Dispatchers.Main) {
                            Utils.mostrarMensaje(requireContext(), "Compra completada")
                            dialog.dismiss()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Utils.mostrarMensaje(requireContext(), e.message ?: "Error")
                            dialog.dismiss()
                        }
                    }
                }
            }
        }

        dialog.show()
    }

    private fun goToCrearProducto() {
        val bundle = Bundle().apply {
            putString("idLista", idLista)
        }
        findNavController().navigate(R.id.action_vistaListaFragment_to_crearProductoFragment, bundle)
    }

    @SuppressLint("SetTextI18n")
    private fun compartirLista(context: Context) {
        // 1. Infla el layout personalizado
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_email_input, null)
        val inputEmail = dialogView.findViewById<EditText>(R.id.input_email)
        val textoError  = dialogView.findViewById<TextView>(R.id.texto_error)

        // 2. Construye el diálogo con MaterialAlertDialogBuilder y tu tema
        val dialog = MaterialAlertDialogBuilder(context, R.style.MyDialogTheme)
            .setTitle("Compartir lista")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .create()

        // 3. Configura el botón Aceptar manualmente para validaciones
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Aceptar") { _, _ -> /* override later */ }

        dialog.setOnShowListener {
            val btnAceptar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnAceptar.setOnClickListener {
                val email = inputEmail.text.toString().trim()
                textoError.visibility = View.GONE

                when {
                    email.isEmpty() -> {
                        textoError.text = "El campo no puede estar vacío"
                        textoError.visibility = View.VISIBLE
                        inputEmail.requestFocus()
                    }
                    !Utils.comprobarCorreo(email) -> {
                        textoError.text = "Correo incorrecto"
                        textoError.visibility = View.VISIBLE
                        inputEmail.requestFocus()
                    }
                    email == sessionManager.getUserEmail() -> {
                        textoError.text = "Introduce otro correo"
                        textoError.visibility = View.VISIBLE
                        inputEmail.requestFocus()
                    }
                    else -> {
                        usuarioService.getUserByEmail(email,
                            onSuccess = { usuario ->
                                lifecycleScope.launch {
                                    if (usuario == null) {
                                        textoError.text = "Correo no encontrado"
                                        textoError.visibility = View.VISIBLE
                                        inputEmail.requestFocus()
                                        return@launch
                                    }
                                    if (usuario.idListasCompartidas.contains(idLista)) {
                                        textoError.text = "Ya has compartido la lista con este usuario anteriormente"
                                        textoError.visibility = View.VISIBLE
                                        inputEmail.requestFocus()
                                        return@launch
                                    }
                                    usuarioService.añadirListaCompartidaAUsuario(idLista, usuario.id)

                                    val nombreUsuario = usuarioService.getUserNameById(userId)
                                    val notificacionEmergenteData = mapOf(
                                        "toUid"     to usuario.id,
                                        "fromUid"   to userId,
                                        "fromName"  to nombreUsuario,
                                        "listName"  to lista.titulo,
                                        "timestamp" to FieldValue.serverTimestamp(),
                                        "tipo"      to GenericConstants.TIPO_LISTA_COMPRATIDA
                                    )
                                    notificacionEmergenteService.saveNotificacionEmergente(
                                        notificacionEmergenteData,
                                        onSuccess = { Log.i("VistaListaFragment", "Notificacion emergente creada") },
                                        onError   = { Log.e("VistaListaFragment", "Error al crear la notificacion emergente") }
                                    )

                                    val noti = Notificacion(
                                        tipo = GenericConstants.TIPO_LISTA_COMPRATIDA,
                                        descripcion = "$nombreUsuario ha compartido la lista \"${lista.titulo}\" con ${usuario.nombre}",
                                        idProductos = lista.idProductos.toMutableList()
                                    )
                                    val idsUsuarios = usuarioService.getUserIdsByListId(lista.id)
                                    val idNotificacion = notificacionesService.saveNotificacion(noti)
                                    usuarioService.añadirNotificacionAUsuarios(idsUsuarios, idNotificacion)


                                    Utils.mostrarMensaje(requireContext(),
                                        "Lista \"${lista.titulo}\" compartida con ${usuario.nombre}")
                                    dialog.dismiss()
                                }
                            },
                            onFailure = {
                                textoError.text = "Error al buscar usuario"
                                textoError.visibility = View.VISIBLE
                                inputEmail.requestFocus()
                            }
                        )
                    }
                }

                if (textoError.visibility == View.VISIBLE) {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(inputEmail, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        // 4. Forzar el teclado y ajustar tamaño
        dialog.window?.apply {
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.85).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        // 5. Mostrar
        dialog.show()
    }



    private fun eliminarLista(context: Context) {
            val dialog = MaterialAlertDialogBuilder(context, R.style.MyDialogTheme)
                .setTitle("Eliminar lista")
                .setNegativeButton("Cancelar", null)
                .create()
            if (lista.idCreador != userId){
               dialog.setTitle("Eliminar lista de listas compartidas")
            }


            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Eliminar") { _, _ -> }

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
        private fun abrirAnadirProducto() {
            val bundle = Bundle().apply {
                putString("idLista", idLista)
            }
            findNavController().navigate(R.id.action_vista_listaFragment_to_añadir_productoFragment, bundle)

        }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun escanearProducto() {
        try{
            val options = ScanOptions()
            options.setPrompt("Escanea un codigo de barras")
            options.setBeepEnabled(false)
            options.setOrientationLocked(false)
            barcodeLauncher.launch(options)
        }catch (e : Error){
            Utils.mostrarMensaje(requireContext(), e.message.toString())
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
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
                                Utils.vibrator(requireContext())
                            }else{
                                listaService.añadirProductoALista(producto.id, idLista)
                                Utils.mostrarMensaje(requireContext(), "Producto añadido: " + producto.nombre)
                                Utils.vibrator(requireContext())
                            }
                        }
                    }else{
                        Utils.mostrarMensaje(requireContext(), "Producto no encontrado: " + codigoEscaneado)
                        Utils.vibrator(requireContext())
                    }
                }
            }

        }
    }

    private fun ordenarProductos(lista: MutableList<Producto>): MutableList<Producto> {
        return lista
            .sortedWith(
                compareBy<Producto> { productosSeleccionados.contains(it.id) }
                    .thenBy { GenericConstants.PRIORIDAD_CATEGORIAS[it.categoria] ?: Double.MAX_VALUE }
            )
            .toMutableList()
    }


    @SuppressLint("SuspiciousIndentation")
    private fun mostrarOpcionesEdicionEliminar(producto: Producto) {
        MaterialAlertDialogBuilder(requireContext(), R.style.MyDialogTheme)
            .setTitle("Opciones")
            .setPositiveButton("Editar producto") { dialog, _ ->
                val bundle = Bundle().apply {
                    putString("idProducto", producto.id)
                }
                findNavController().navigate(
                    R.id.action_vistaListaFragment_to_editarProductoFragment,
                    bundle
                )
                dialog.dismiss()
            }
            .setNegativeButton("Eliminar producto") { dialog, _ ->
                lifecycleScope.launch {
                    listaService.eliminarProductoDeLista(idLista, producto.id)
                    Utils.mostrarMensaje(
                        requireContext(),
                        "${producto.nombre} eliminado de la lista"
                    )
                }
                dialog.dismiss()
            }
            .show()
    }

    fun abrirMapa() {
        if (!listaProductos.isNullOrEmpty()){
            if (!listaProductos[0].categoria.isNullOrEmpty()){
                val categoria = listaProductos[0].categoria
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_mapa, null)
                val imageView = dialogView.findViewById<ImageView>(R.id.imageViewMapa)
                val btnCerrar = dialogView.findViewById<Button>(R.id.btnCerrar)

                val builder = MaterialAlertDialogBuilder(requireContext(), R.style.MyDialogTheme)
                    .setView(dialogView)
                val dialog = builder.create()
                dialog.show()

                btnCerrar.setOnClickListener { dialog.dismiss() }

                // Cargar el SVG desde assets
                val assetManager = requireContext().assets
                val inputStream = assetManager.open("mapaEroskiRocha.svg")
                val svg = SVG.getFromInputStream(inputStream)


                val css = "[id=\"${categoria}\"] { fill: #FF0000; }"
                val renderOptions = RenderOptions.create().css(css)
                val picture = svg.renderToPicture(renderOptions)
                val drawable = PictureDrawable(picture)

                imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                imageView.setImageDrawable(drawable)
            }
        }else{
            Utils.mostrarMensaje(requireContext(), "Lista vacía")
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}