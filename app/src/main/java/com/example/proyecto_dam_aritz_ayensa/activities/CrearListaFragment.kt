package com.example.proyecto_dam_aritz_ayensa.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.databinding.FragmentCrearListaBinding


/**
 * A simple [Fragment] subclass.
 * Use the [CrearListaFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CrearListaFragment : Fragment() {

    private var _binding: FragmentCrearListaBinding? = null
    private val binding get() = _binding!!

    lateinit var buttonAñadirLista : Button
    lateinit var buttonCancelar : Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrearListaBinding.inflate(inflater, container, false)


        buttonAñadirLista = binding.crearListaBtnCrearLista
        buttonCancelar = binding.crearListaBtnCancelar

        if (buttonAñadirLista != null) {
            buttonAñadirLista.setOnClickListener {
                crearLista()
            }
        }
        if (buttonCancelar != null) {
            buttonCancelar.setOnClickListener {
                cancelar()
            }
        }
        return binding.root
    }
    private fun crearLista() {
        TODO("Not yet implemented")
    }

    private fun cancelar() {
       parentFragmentManager.popBackStack()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}