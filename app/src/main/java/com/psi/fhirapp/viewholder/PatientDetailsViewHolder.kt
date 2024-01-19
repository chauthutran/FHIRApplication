package com.psi.fhirapp.viewholder


import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.psi.fhirapp.databinding.PatientDetailsCardViewBinding
import com.psi.fhirapp.fragments.PatientDetailData
import com.psi.fhirapp.fragments.PatientDetailProperty


class PatientDetailsViewHolder(private val binding: PatientDetailsCardViewBinding) :
    RecyclerView.ViewHolder(binding.root)  {

    fun bind(data: PatientDetailData) {
        (data as PatientDetailProperty).let {
            binding.name.text = it.patientProperty.header
            binding.fieldName.text = it.patientProperty.value
        }
    }
}
