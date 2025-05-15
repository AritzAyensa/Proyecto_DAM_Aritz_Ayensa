package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.databinding.ActivityBottomNavigationBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class BottomNavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavigationBinding
    private lateinit var navController: NavController

    // Referencia pública al BottomNavigationView
    lateinit var bottomNav: BottomNavigationView
        private set

    // Guardamos nuestro listener “original” para restaurarlo luego
    lateinit var originalNavListener: NavigationBarView.OnItemSelectedListener
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Inicializar NavController y BottomNavigationView
        navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        bottomNav = binding.navView

        // 2. Crear y guardar nuestro propio listener que llama a NavigationUI
        originalNavListener = NavigationBarView.OnItemSelectedListener { menuItem ->
            NavigationUI.onNavDestinationSelected(menuItem, navController)
        }

        // 3. Asignar ese listener
        bottomNav.setOnItemSelectedListener(originalNavListener)
    }

    fun blockNavigation() {
        bottomNav.setOnItemSelectedListener { false }
        bottomNav.alpha = 0.5f
    }

    fun unblockNavigation() {
        bottomNav.setOnItemSelectedListener(originalNavListener)
        bottomNav.alpha = 1f
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
