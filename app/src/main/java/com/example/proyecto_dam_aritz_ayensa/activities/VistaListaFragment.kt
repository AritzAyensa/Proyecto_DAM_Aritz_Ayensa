package com.example.proyecto_dam_aritz_ayensa.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.databinding.DialogEmailInputBinding
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentCrearListaBinding
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentVistaListaBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.ListaDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista
import com.example.proyecto_dam_aritz_ayensa.model.entity.Usuario
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch


class VistaListaFragment : Fragment() {

    private var _binding: FragmentVistaListaBinding? = null
    private val binding get() = _binding!!


    lateinit var buttonAñadirProducto : Button
    lateinit var buttonCompartirLista : Button
    lateinit var buttonEliminarLista : Button


    private lateinit var tvTituloLista: TextView
    private lateinit var lista: Lista

    private lateinit var idLista: String
    private lateinit var userId : String


    private lateinit var listaService: ListaService
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVistaListaBinding.inflate(inflater, container, false)

        listaService = ListaService(ListaDAO())
        usuarioService = UsuarioService(UsuarioDAO())
        sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId().toString()

        //Obtener el id de la lista
        arguments?.let {
            idLista = it.getString("idLista").toString()
        }

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

        }

        private fun cargarLista() {
            lifecycleScope.launch {
                lista = listaService.getListaById(idLista)!!
                if (lista.idCreador == userId){
                    buttonCompartirLista.visibility = View.VISIBLE
                }
                actualizarVista()
            }
        }

        private fun actualizarVista() {
            tvTituloLista.text = lista.titulo
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

}