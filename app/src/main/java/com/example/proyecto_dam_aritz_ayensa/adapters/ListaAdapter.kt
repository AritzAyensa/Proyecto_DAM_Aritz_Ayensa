package com.example.proyecto_dam_aritz_ayensa.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista

import com.google.android.material.imageview.ShapeableImageView

class ListaAdapter(
    private val listas: List<Lista>,
    private val onItemClick: (Lista) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Constantes para tipos de vista
    private companion object {
        const val VIEW_ITEM_LISTA = 0
        const val VIEW_LISTA_VACIA = 1
    }

    // ViewHolder para elementos normales
    inner class ListaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTarea: TextView = itemView.findViewById(R.id.nombreLista)
        val numProductos: TextView = itemView.findViewById(R.id.numeroItems)
        val colorLista: ShapeableImageView = itemView.findViewById(R.id.colorLista)


        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(listas[adapterPosition])
                }
            }
        }
    }

    // ViewHolder para lista vacía
    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textoItemVacio: TextView = itemView.findViewById(R.id.item_vacio_tv_texto)
    }

    override fun getItemViewType(position: Int): Int {
        return if (listas.isEmpty()) VIEW_LISTA_VACIA else VIEW_ITEM_LISTA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_ITEM_LISTA) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lista_inicio, parent, false)
            ListaViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vacio, parent, false) // Layout para vacío
            EmptyViewHolder(view)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ListaViewHolder) {
            val lista = listas[position]
            holder.nombreTarea.text = lista.titulo
            holder.numProductos.text = "${lista.idProductos.size} elementos"
            holder.colorLista.setBackgroundColor(Color.parseColor(lista.color))
        } else if (holder is EmptyViewHolder) {
            holder.textoItemVacio.text = "Sin listas"
        }
    }







    override fun getItemCount(): Int {
        return if (listas.isEmpty()) 1 else listas.size // 1 para el ítem vacío
    }
}