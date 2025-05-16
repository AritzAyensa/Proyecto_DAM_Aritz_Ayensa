package com.example.proyecto_dam_aritz_ayensa.activities.ui.cuenta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.activities.BottomNavigationActivity
import com.example.proyecto_dam_aritz_ayensa.activities.EditarPerfilActivity
import com.example.proyecto_dam_aritz_ayensa.activities.LoginActivity
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentCuentaBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.StorageDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.model.service.StorageService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging

class CuentaFragment : Fragment() {

    private var _binding: FragmentCuentaBinding? = null
    lateinit var buttonCerrarSesion : Button
    private lateinit var userId : String
    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var fotoPerfil: ImageView

    lateinit var buttonEditar : ImageButton
    private lateinit var usuarioService: UsuarioService
    private lateinit var storageService: StorageService
    private lateinit var notificacionesService: NotificacionService
    private lateinit var sessionManager: SessionManager
    private lateinit var bottomNav : BottomNavigationView
    private val binding get() = _binding!!
    /**
     * Método: onCreateView
     *
     * Infla el layout del fragmento, inicializa servicios,
     * obtiene referencias de vistas y configura listeners
     * para botones de cerrar sesión y editar perfil.
     *
     * @param inflater LayoutInflater para inflar la vista
     * @param container ViewGroup contenedor de la UI
     * @param savedInstanceState Bundle con estado previo
     * @return View raíz del fragmento
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCuentaBinding.inflate(inflater, container, false)
        bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        notificacionesService = NotificacionService(NotificacionDAO())
        storageService = StorageService(StorageDAO())
        usuarioService = UsuarioService(UsuarioDAO(), notificacionesService)
        sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId().toString()

        tvNombre = binding.nombreUsuario
        tvCorreo = binding.correoUsuario
        fotoPerfil = binding.fotoPerfil

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
    /**
     * Método: onResume
     *
     * Llamado cuando el fragmento vuelve a primer plano.
     * Recarga los datos del usuario para reflejar cambios.
     */
    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }
    /**
     * Método: cargarDatosUsuario
     *
     * Bloquea la navegación inferior, obtiene los datos
     * del usuario actual desde Firestore y muestra nombre,
     * correo y foto de perfil en la UI.
     *
     * Desbloquea la navegación una vez completada la carga,
     * incluso si ocurre un error.
     */
    private fun cargarDatosUsuario() {
        val activity = requireActivity() as BottomNavigationActivity
        activity.blockNavigation()

        usuarioService.getUser(userId,
            onSuccess = { usuario ->
                if (usuario != null) {
                    Log.d("UserService", "Usuario obtenido: ${usuario.nombre}")
                    tvNombre.text = usuario.nombre
                    tvCorreo.text = usuario.email

                    storageService.getFotoPerfilUrl(usuario.id,
                        onSuccess = { imageUrl ->
                            Glide.with(requireContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.perfil)
                                .circleCrop()
                                .into(fotoPerfil)

                            activity.unblockNavigation()
                        },
                        onFailure = { exception ->
                            Log.e("StorageService", "Error al obtener la foto de perfil", exception)
                            activity.unblockNavigation()
                        }
                    )
                } else {
                    activity.unblockNavigation()
                }
            },
            onFailure = { exception ->
                Log.e("UserService", "Error al obtener el usuario", exception)
                activity.unblockNavigation()
            }
        )
    }

    /**
     * Método: cerrarSesion
     *
     * Elimina el token FCM asociado al usuario en Firestore,
     * cierra la sesión localmente y redirige a la pantalla de login.
     * Registra errores de token sin interrumpir la navegación.
     */
    private fun cerrarSesion() {

        if (context != null) {
            val uid = sessionManager.getUserId()
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result

                    usuarioService.eliminarFcmToken(uid.toString(), token,
                        onSuccess = {
                            sessionManager.logout()
                            sessionManager.clearSession()
                            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            requireActivity().finish()
                        },
                        onFailure = { e ->
                            Log.e("Logout", "Error al eliminar el token FCM: ${e.message}")
                        }
                    )
                } else {
                    Log.e("Logout", "No se pudo obtener el token FCM", task.exception)
                }
            }
        } else {
            Log.e("CuentaFragment", "Contexto nulo al cerrar sesión")
        }
    }
    /**
     * Método: goToEditarPerfil
     *
     * Navega a la actividad de edición de perfil permitiendo
     * al usuario actualizar sus datos.
     */
    private fun goToEditarPerfil() {
        activity?.let { safeActivity ->
            val intent = Intent(safeActivity, EditarPerfilActivity::class.java)
            safeActivity.startActivity(intent)
        }
    }
    /**
     * Método: onDestroyView
     *
     * Limpia el binding para evitar fugas de memoria cuando
     * la vista del fragmento se destruye.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}