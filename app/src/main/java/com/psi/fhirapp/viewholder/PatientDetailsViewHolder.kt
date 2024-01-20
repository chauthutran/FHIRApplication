package com.psi.fhirapp.viewholder


import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.psi.fhirapp.databinding.PatientDetailsCardViewBinding
import com.psi.fhirapp.data.PatientDetailProperty


class PatientDetailsViewHolder(private val binding: PatientDetailsCardViewBinding) :
    RecyclerView.ViewHolder(binding.root)  {

    fun bind(data: PatientDetailProperty) {
        (data as PatientDetailProperty).let {
            binding.name.text = it.patientProperty.header
            binding.fieldName.text = it.patientProperty.value
        }
    }
}
