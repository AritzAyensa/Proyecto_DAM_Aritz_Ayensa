package com.example.proyecto_dam_aritz_ayensa.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.entity.Producto
import com.example.proyecto_dam_aritz_ayensa.model.service.ListaService

class ProductoParaAnadirAdapter(
    private var productos: List<Producto>,
    /*private val onItemClick: (Producto) -> Unit,*/
    private val onAddProducto: (String) -> Unit // Nuevo callback para añadir productos
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_ITEM_PRODUCTO = 0
        private const val VIEW_LISTA_VACIA = 1
    }

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreProducto: TextView = itemView.findViewById(R.id.nombreProducto)
        val precioProducto: TextView = itemView.findViewById(R.id.precioProducto)
        val botonAnadirALista: ImageButton = itemView.findViewById(R.id.btnAnadirALista)

        fun bind(producto: Producto) {
            nombreProducto.text = producto.nombre
            precioProducto.text = "${producto.precioAproximado}€"

            botonAnadirALista.setOnClickListener {
                onAddProducto(producto.id)
            }

            /*itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(productos[adapterPosition])
                }
            }*/
        }
    }

    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textoItemVacio: TextView = itemView.findViewById(R.id.item_vacio_tv_texto)
    }

    override fun getItemViewType(position: Int): Int {
        return if (productos.isEmpty()) VIEW_LISTA_VACIA else VIEW_ITEM_PRODUCTO
    }

    fun actualizarProductos(nuevosProductos: List<Producto>) {
        productos = nuevosProductos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_ITEM_PRODUCTO) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_producto_para_anadir, parent, false)
            ProductoViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vacio, parent, false)
            EmptyViewHolder(view).apply {
                textoItemVacio.text = "Lista vacía"
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductoViewHolder -> holder.bind(productos[position])
            is EmptyViewHolder -> holder.textoItemVacio.text = "No hay productos disponibles"
        }
    }

    override fun getItemCount(): Int = if (productos.isEmpty()) 1 else productos.size
}