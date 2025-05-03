package com.example.proyecto_dam_aritz_ayensa.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto

class ProductoAdapter(
    private var productos: MutableList<Producto>,
    private val onItemClick: (Producto) -> Unit,
    private val onCheckClick: (Producto, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Constantes para tipos de vista
    private companion object {
        const val VIEW_ITEM_PRODUCTO = 0
        const val VIEW_LISTA_VACIA = 1
    }


    private val selectedIds = mutableSetOf<String>()

    // ViewHolder para elementos normales
    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreProducto: TextView = itemView.findViewById(R.id.nombreProducto)
        val precioProducto: TextView = itemView.findViewById(R.id.precioProducto)
        val btnCheck: ImageButton = itemView.findViewById(R.id.btnChech)
        val container: View = itemView

        fun bind(p: Producto) {
            nombreProducto.text = p.nombre
            precioProducto.text = "${p.precioAproximado}€"

            // 1) Establece siempre el icono y el fondo según el estado actual:
            if (selectedIds.contains(p.id)) {
                btnCheck.setImageResource(R.drawable.outline_check_box_24)
                container.setBackgroundColor(Color.LTGRAY)
            } else {
                btnCheck.setImageResource(R.drawable.outline_check_box_outline_blank_24)
                container.setBackgroundColor(Color.WHITE)
            }

            // 2) Listener de click: solo cambia el estado y llama al fragment
            btnCheck.setOnClickListener {
                val nowSelected = if (selectedIds.remove(p.id)) false
                else { selectedIds.add(p.id); true }
                onCheckClick(p, nowSelected)
            }

            nombreProducto.setOnClickListener {
                onItemClick(p)
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
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarProductos(nuevosProductos: MutableList<Producto>) {
        productos.clear()
        productos.addAll(nuevosProductos)
        notifyDataSetChanged()
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
        } else if (holder is EmptyViewHolder) {
            holder.textoItemVacio.text = "Lista vacia"
        }

        when (holder) {
            is ProductoViewHolder -> holder.bind(productos[position])
            is EmptyViewHolder -> holder.textoItemVacio.text = "Lista vacia"
        }
    }
    

    override fun getItemCount(): Int {
        return if (productos.isEmpty()) 1 else productos.size // 1 para el ítem vacío
    }
}