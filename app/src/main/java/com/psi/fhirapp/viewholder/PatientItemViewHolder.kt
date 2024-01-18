package com.psi.fhirapp.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.psi.fhirapp.adapters.PatientItemRecyclerViewAdapter
import com.psi.fhirapp.data.PatientItem
import com.psi.fhirapp.databinding.FragmentPatientItemBinding


/**
 * RecyclerView.ViewHolder is a ViewHolder describes an item view and metadata about its place
 * within the RecyclerView.
 * ViewHolders belong to the adapter. Adapters should feel free to use their own custom
 * ViewHolder implementations to store data that makes binding view contents easier.
 *
 * This class is used for populating Patient data to the view
 * **/
class PatientItemViewHolder(binding: FragmentPatientItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val nameTextView: TextView = binding.name
    private val idTextView: TextView = binding.id
    private val genderTextView: TextView = binding.gender
    private val cityTextView = binding.city

    fun bind(patientItem: PatientItem, onClickListener: View.OnClickListener) {
        nameTextView.text = patientItem.name
        idTextView.text = patientItem.resourceId
        genderTextView.text = patientItem.gender
        cityTextView.text = patientItem.country

//        itemView.setOnClickListener( object : View.OnClickListener {
//            override fun onClick(view: View) {
//                adapter.listener?.onItemClick(patientItem)
//            }
//        })

        itemView.setOnClickListener(onClickListener)
    }
}
