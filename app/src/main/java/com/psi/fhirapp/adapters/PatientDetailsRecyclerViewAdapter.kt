package com.psi.fhirapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.psi.fhirapp.data.PatientItem
import com.psi.fhirapp.databinding.FragmentPatientDetailsBinding
import com.psi.fhirapp.viewholder.PatientDetailsViewHolder

class PatientDetailsRecyclerViewAdapter:
    ListAdapter<PatientItem, PatientDetailsViewHolder>(PatientDataDiffCallback())  {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PatientDetailsViewHolder {
        return PatientDetailsViewHolder(
            FragmentPatientDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: PatientDetailsViewHolder, position: Int) {
        val item = currentList[position]
        holder.bind(item)
    }

}