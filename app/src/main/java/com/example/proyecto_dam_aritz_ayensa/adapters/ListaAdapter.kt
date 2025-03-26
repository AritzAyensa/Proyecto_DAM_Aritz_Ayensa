package com.example.proyecto_dam_aritz_ayensa.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.entity.Lista

class ListaAdapter(
    private val listas: List<Lista>,
    private val onItemClick: (Lista) -> Unit
) : RecyclerView.Adapter<ListaAdapter.ListaViewHolder>() {

    // ViewHolder: Representa cada elemento de la lista
    inner class ListaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTarea: TextView = itemView.findViewById(R.id.nombreTarea)
        val numProductos: TextView = itemView.findViewById(R.id.numeroItems)
        val colorLista: TextView = itemView.findViewById(R.id.colorLista)

        init {
            itemView.setOnClickListener {
                onItemClick(listas[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lista_inicio, parent, false)
        return ListaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListaViewHolder, position: Int) {
        val lista = listas[position]
        holder.nombreTarea.text = lista.titulo
        holder.numProductos.text = "1 elemento"
        holder.colorLista.setBackgroundColor(Color.parseColor(lista.color))
    }

    override fun getItemCount(): Int = listas.size
}