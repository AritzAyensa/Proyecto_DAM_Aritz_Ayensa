package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.activities.ui.cuenta.CuentaFragment
import com.example.proyecto_dam_aritz_ayensa.activities.ui.inicio.InicioFragment
import com.example.proyecto_dam_aritz_ayensa.activities.ui.notifications.NotificationsFragment
import com.example.proyecto_dam_aritz_ayensa.adapters.NotificacionAdapter
import com.example.proyecto_dam_aritz_ayensa.databinding.ActivityBottomNavigationBinding
import com.example.proyecto_dam_aritz_ayensa.model.dao.NotificacionDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.ProductoDAO
import com.example.proyecto_dam_aritz_ayensa.model.dao.UsuarioDAO
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.example.proyecto_dam_aritz_ayensa.model.service.NotificacionService
import com.example.proyecto_dam_aritz_ayensa.model.service.ProductoService
import com.example.proyecto_dam_aritz_ayensa.model.service.UsuarioService
import com.example.proyecto_dam_aritz_ayensa.utils.SessionManager
import com.example.proyecto_dam_aritz_ayensa.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BottomNavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavigationBinding
    private lateinit var navController: NavController
    private lateinit var notificacionService: NotificacionService
    private lateinit var usuarioService: UsuarioService
    private lateinit var sessionManager: SessionManager
    private lateinit var userId : String

    private var notificaciones: List<Notificacion> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        notificacionService = NotificacionService(NotificacionDAO())
        usuarioService = UsuarioService(UsuarioDAO())

        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId().toString()
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        // 1. Obtener NavController desde el NavHostFragment
        navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)

        // 2. Configurar los destinos de nivel superior (IDs deben coincidir con los del menú)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_inicio,
                R.id.navigation_cuenta,
                R.id.navigation_vista_lista,
                R.id.navigation_añadir_producto,
                R.id.navigation_crear_lista,
                R.id.navigation_crear_producto,
                R.id.navigation_notifications
            )
        )

        // 3. Vincular BottomNavigationView con NavController
        binding.navView.setupWithNavController(navController)

        // Obtén la referencia a tu BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.nav_view)

        // Identificador del item de notificaciones en tu menú
        val itemIdNotifs = R.id.navigation_notifications

        lifecycleScope.launch() {
            notificaciones = notificacionService.getNotificacionesPorUsuario(userId)
            if(notificaciones.isNotEmpty()){
                val badge = bottomNav.getOrCreateBadge(itemIdNotifs).apply {
                    isVisible = true
                    number = notificaciones.size
                }
            }
        }

        navController.addOnDestinationChangedListener { _, dest, _ ->
            when (dest.id) {
                R.id.navigation_inicio,
                R.id.navigation_añadir_producto,
                R.id.navigation_crear_lista,
                R.id.navigation_crear_producto,
                R.id.navigation_vista_lista -> {
                    bottomNav.menu.findItem(R.id.navigation_inicio).isChecked = true
                }
                R.id.navigation_notifications -> {
                    bottomNav.menu.findItem(R.id.navigation_notifications).isChecked = true
                }
                R.id.navigation_cuenta -> {
                    bottomNav.menu.findItem(R.id.navigation_cuenta).isChecked = true
                }
            }
        }



    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}