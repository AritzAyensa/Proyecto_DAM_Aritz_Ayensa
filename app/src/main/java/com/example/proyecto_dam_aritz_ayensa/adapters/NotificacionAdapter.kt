package com.example.proyecto_dam_aritz_ayensa.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_dam_aritz_ayensa.R
import com.example.proyecto_dam_aritz_ayensa.model.entity.Notificacion

class NotificacionAdapter(
    private val notificaciones: List<Notificacion>,
    private val onItemClick: (Notificacion) -> Unit,
    private val onCheckClick: (Notificacion, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val VIEW_ITEM_NOTIFICACION = 0
        const val VIEW_SIN_NOTIFICACIONES = 1
    }

    private val selectedIds = mutableSetOf<String>()
    inner class NotificacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descripcion: TextView = itemView.findViewById(R.id.descripcionNotificacion)
        private val btnCheck: ImageButton = itemView.findViewById(R.id.btnChech)

        fun bind(n: Notificacion) {

            descripcion.text = n.descripcion
            // Pinta el icono según esté en selectedIds
            btnCheck.setImageResource(
                if (selectedIds.contains(n.id)) R.drawable.outline_check_box_24
                else R.drawable.outline_check_box_outline_blank_24
            )

            descripcion.setOnClickListener { onItemClick(n) }

            // Click en el check: alterna estado, cambia icono y notifica al fragment
            btnCheck.setOnClickListener {
                val nowSelected = if (selectedIds.remove(n.id)) {
                    btnCheck.setImageResource(R.drawable.outline_check_box_outline_blank_24)
                    false
                } else {
                    selectedIds.add(n.id)
                    btnCheck.setImageResource(R.drawable.outline_check_box_24)
                    true
                }
                onCheckClick(n, nowSelected)
            }
        }
    }

    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textoItemVacio: TextView = itemView.findViewById(R.id.item_vacio_tv_texto)
    }

    override fun getItemViewType(position: Int): Int =
        if (notificaciones.isEmpty()) VIEW_SIN_NOTIFICACIONES else VIEW_ITEM_NOTIFICACION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("Adapter", "Creando viewholder")
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_ITEM_NOTIFICACION) {
            val view = inflater.inflate(R.layout.item_notificacion, parent, false)
            NotificacionViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vacio, parent, false)
            EmptyViewHolder(view).apply {
                textoItemVacio.text = "No tienes notificaciones"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NotificacionViewHolder -> holder.bind(notificaciones[position])
            is EmptyViewHolder -> holder.textoItemVacio.text = "No tienes notificaciones"
        }
    }


    override fun getItemCount(): Int =
        if (notificaciones.isEmpty()) 1 else notificaciones.size
}
