package com.example.proyecto_dam_aritz_ayensa.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto

class ProductoAdapter(
    private var productos: List<Producto>,
    private val onItemClick: (Producto) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Constantes para tipos de vista
    private companion object {
        const val VIEW_ITEM_PRODUCTO = 0
        const val VIEW_LISTA_VACIA = 1
    }

    // ViewHolder para elementos normales
    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreProducto: TextView = itemView.findViewById(R.id.nombreProducto)
        val precioProducto: TextView = itemView.findViewById(R.id.precioProducto)
        /*val colorProducto: TextView = itemView.findViewById(R.id.coloProducto)*/

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(productos[adapterPosition])
                }
            }
        }
    }

    // ViewHolder para lista vacía
    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textoItemVacio: TextView = itemView.findViewById(R.id.item_vacio_tv_texto)
    }

    override fun getItemViewType(position: Int): Int {
        return if (productos.isEmpty()) VIEW_LISTA_VACIA else VIEW_ITEM_PRODUCTO
    }
    fun actualizarProductos(nuevosProductos: List<Producto>) {
        productos = nuevosProductos
        notifyDataSetChanged() // Notificar cambios
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_ITEM_PRODUCTO) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_producto, parent, false)
            ProductoViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vacio, parent, false) // Layout para vacío
            EmptyViewHolder(view)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ProductoViewHolder) {
            val producto = productos[position]
            holder.nombreProducto.text = producto.nombre
            holder.precioProducto.text = "${producto.precioAproximado}€"
            /*holder.colorProducto.setBackgroundColor(Color.parseColor(lista.color))*/
        } else if (holder is EmptyViewHolder) {
            holder.textoItemVacio.text = "Lista vacia"
        }
    }
    

    override fun getItemCount(): Int {
        return if (productos.isEmpty()) 1 else productos.size // 1 para el ítem vacío
    }
}