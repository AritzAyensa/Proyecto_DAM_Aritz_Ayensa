package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.activities.ui.cuenta.CuentaFragment
import com.example.proyecto_dam_aritz_ayensa.activities.ui.inicio.InicioFragment
import com.example.proyecto_dam_aritz_ayensa.activities.ui.notifications.NotificationsFragment
import com.example.proyecto_dam_aritz_ayensa.databinding.ActivityBottomNavigationBinding

class BottomNavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavigationBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        // 1. Obtener NavController desde el NavHostFragment
        navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)

        // 2. Configurar los destinos de nivel superior (IDs deben coincidir con los del menú)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_inicio,       // ID del fragmento de inicio
                R.id.navigation_cuenta,      // ID del fragmento de cuenta
                R.id.navigation_notifications// ID del fragmento de notificaciones
            )
        )

        // 3. Vincular BottomNavigationView con NavController
        binding.navView.setupWithNavController(navController)

    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}