package com.example.proyecto_dam_aritz_ayensa.activities.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.activities.EditarPerfilActivity
import com.example.proyecto_dam_aritz_ayensa.activities.LoginActivity
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentCuentaBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager

class CuentaFragment : Fragment() {

    private var _binding: FragmentCuentaBinding? = null
    lateinit var buttonCerrarSesion : Button
    private lateinit var userId : String
    // Datos del usuario
    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView

    lateinit var buttonEditar : ImageButton
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCuentaBinding.inflate(inflater, container, false)

        usuarioService = UsuarioService(UsuarioDAO())
        sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId().toString()

        tvNombre = binding.nombreUsuario
        tvCorreo = binding.correoUsuario

        buttonCerrarSesion = binding.btnCerrarSesion
        buttonCerrarSesion = binding.btnCerrarSesion
        if (buttonCerrarSesion != null) {
            buttonCerrarSesion.setOnClickListener {
                cerrarSesion()
            }
        }
        buttonEditar = binding.btnEditar
        if (buttonEditar != null) {
            buttonEditar.setOnClickListener {
                goToEditarPerfil()
            }
        }
        cargarDatosUsuario()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }
    private fun cargarDatosUsuario() {
        val usuario = usuarioService.getUser(userId,
            onSuccess = { usuario ->
                if (usuario != null) {
                    Log.d("UserService", "Usuario obtenido: ${usuario.nombre}")
                    val userId = usuario.id
                    tvNombre.text = usuario.nombre
                    tvCorreo.text = usuario.email
                }
            },
            onFailure = { exception ->
                Log.e("UserService", "Error al obtener el usuario", exception)
            })
    }


    private fun cerrarSesion() {
        sessionManager.logout()

        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Verifica que el contexto no sea nulo
        if (context != null) {
            startActivity(intent)
            requireActivity().finish()
        } else {
            Log.e("CuentaFragment", "Contexto nulo al cerrar sesión")
        }
    }

    private fun goToEditarPerfil() {
        activity?.let { safeActivity ->
            val intent = Intent(safeActivity, EditarPerfilActivity::class.java)
            safeActivity.startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}