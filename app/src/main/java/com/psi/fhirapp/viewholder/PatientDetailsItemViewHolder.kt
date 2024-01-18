package com.psi.fhirapp.viewholder

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.psi.fhirapp.data.PatientItem
import com.psi.fhirapp.databinding.FragmentPatientDetailsBinding


class PatientDetailsItemViewHolder(binding: FragmentPatientDetailsBinding) :
    RecyclerView.ViewHolder(binding.root)  {

    private val idTextView: TextView = binding.txtId
    private val txtName: TextView = binding.txtName
    private val txtGender: TextView = binding.txtGender
    private val txtDob = binding.txtDob
    private val txtPhone = binding.txtPhone

    fun bind(patientItem: PatientItem) {
        idTextView.text = patientItem.resourceId
        txtName.text = patientItem.name
        txtGender.text = patientItem.gender
        txtDob.text = patientItem.dob.toString()
        txtPhone.text = patientItem.phone

//        itemView.setOnClickListener {
////            val position = adapterPosition
//            if (listener != null && position != RecyclerView.NO_POSITION) {
//                listener.onItemClick(notes.get(position))
//            }
//        }
    }
}