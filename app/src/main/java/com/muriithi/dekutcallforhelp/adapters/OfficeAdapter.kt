// OfficeAdapter.kt
package com.muriithi.dekutcallforhelp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muriithi.dekutcallforhelp.R
import com.muriithi.dekutcallforhelp.beans.OfficeDetail

data class OfficeData(val officeName: String, val details: List<OfficeDetail>)

class OfficeAdapter : RecyclerView.Adapter<OfficeAdapter.OfficeViewHolder>() {

    private var officeData: List<OfficeData> = emptyList()

    fun submitList(data: List<OfficeData>) {
        officeData = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfficeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_office, parent, false)
        return OfficeViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfficeViewHolder, position: Int) {
        val office = officeData[position]
        holder.bind(office)
    }

    override fun getItemCount(): Int = officeData.size

    inner class OfficeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val officeNameTextView: TextView = itemView.findViewById(R.id.office_name)
        private val detailsRecyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view_details)

        fun bind(office: OfficeData) {
            officeNameTextView.text = office.officeName
            if (office.details.isNotEmpty()) {
                detailsRecyclerView.visibility = View.VISIBLE
                detailsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
                detailsRecyclerView.adapter = OfficeDetailAdapter(office.details)
            } else {
                detailsRecyclerView.visibility = View.GONE
            }
        }
    }
}