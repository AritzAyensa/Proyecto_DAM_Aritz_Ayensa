package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.databinding.ActivityBottomNavigationBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class BottomNavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavigationBinding
    private lateinit var navController: NavController

    // Expuesto para fragments
    lateinit var bottomNav: BottomNavigationView
        private set

    // Listener “original” guardado para restaurar después del bloqueo
    lateinit var originalNavListener: NavigationBarView.OnItemSelectedListener
        private set

    // Servicios de usuario/notificaciones
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar servicios
        usuarioService = UsuarioService(UsuarioDAO(), NotificacionService(NotificacionDAO()))
        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId().toString()

        // NavController y BottomNav
        navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        bottomNav = binding.navView

        // Configurar badging de notificaciones
        observeNotificationCount(userId)

        // Definir y guardar listener original
        originalNavListener = NavigationBarView.OnItemSelectedListener { menuItem ->
            NavigationUI.onNavDestinationSelected(menuItem, navController)
        }

        // Escuchar cambios de destino para marcar el ítem activo
        navController.addOnDestinationChangedListener { _, dest, _ ->
            when (dest.id) {
                R.id.navigation_inicio,
                R.id.navigation_añadir_producto,
                R.id.navigation_crear_lista,
                R.id.navigation_crear_producto,
                R.id.navigation_vista_lista ->
                    bottomNav.menu.findItem(R.id.navigation_inicio).isChecked = true
                R.id.navigation_notifications ->
                    bottomNav.menu.findItem(R.id.navigation_notifications).isChecked = true
                R.id.navigation_cuenta ->
                    bottomNav.menu.findItem(R.id.navigation_cuenta).isChecked = true
            }
        }

        // Asignar el listener que navega
        bottomNav.setOnItemSelectedListener(originalNavListener)
    }

    private fun observeNotificationCount(userId: String) {
        val notifItemId = R.id.navigation_notifications
        lifecycleScope.launch {
            usuarioService
                .notificacionesSinLeerCountFlow(userId)
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { count ->
                    val badge: BadgeDrawable = bottomNav.getOrCreateBadge(notifItemId)
                    badge.isVisible = count > 0
                    badge.number = count
                }
        }
    }

    /** Desactiva toda navegación y atenúa la barra */
    fun blockNavigation() {
        bottomNav.setOnItemSelectedListener { false }
        bottomNav.alpha = 0.5f
    }

    /** Restaura la navegación normal y opacidad */
    fun unblockNavigation() {
        bottomNav.setOnItemSelectedListener(originalNavListener)
        bottomNav.alpha = 1f
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
