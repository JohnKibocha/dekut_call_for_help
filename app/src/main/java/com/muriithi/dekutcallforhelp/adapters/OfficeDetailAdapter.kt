// OfficeDetailAdapter.kt
package com.muriithi.dekutcallforhelp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muriithi.dekutcallforhelp.R
import com.muriithi.dekutcallforhelp.beans.OfficeDetail

class OfficeDetailAdapter(private val details: List<OfficeDetail>) :
    RecyclerView.Adapter<OfficeDetailAdapter.OfficeDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfficeDetailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_office_detail, parent, false)
        return OfficeDetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfficeDetailViewHolder, position: Int) {
        val detail = details[position]
        holder.bind(detail)
    }

    override fun getItemCount(): Int = details.size

    inner class OfficeDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val visitDateTextView: TextView = itemView.findViewById(R.id.visit_date)
        private val officerTextView: TextView = itemView.findViewById(R.id.officer)
        private val ratingTextView: TextView = itemView.findViewById(R.id.rating)
        private val statusTextView: TextView = itemView.findViewById(R.id.status)

        fun bind(detail: OfficeDetail) {
            visitDateTextView.text = detail.visitDate
            officerTextView.text = detail.officer
            ratingTextView.text = detail.rating.toString()
            statusTextView.text = detail.status.name
        }
    }
}