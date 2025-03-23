package com.example.proyecto_dam_aritz_ayensa.activities.ui.inicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.activities.CrearListaFragment
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentInicioBinding

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null

    lateinit var buttonAñadirLista : Button

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)

        buttonAñadirLista = binding.btnAnadirLista
        if (buttonAñadirLista != null) {
            buttonAñadirLista.setOnClickListener {
                abrirAñadirLista()
            }
        }
        return binding.root
    }

    private fun abrirAñadirLista() {
        findNavController().navigate(R.id.action_inicioFragment_to_crearListaFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}