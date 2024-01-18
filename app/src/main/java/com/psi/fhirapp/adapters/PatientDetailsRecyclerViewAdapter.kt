package com.psi.fhirapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.psi.fhirapp.data.PatientItem
import com.psi.fhirapp.databinding.FragmentPatientDetailsBinding
import com.psi.fhirapp.databinding.FragmentPatientItemBinding
import com.psi.fhirapp.viewholder.PatientDetailsItemViewHolder
import com.psi.fhirapp.viewholder.PatientItemViewHolder

class PatientDetailsRecyclerViewAdapter:
    ListAdapter<PatientItem, PatientDetailsItemViewHolder>(PatientDataDiffCallback())  {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PatientDetailsItemViewHolder {
        return PatientDetailsItemViewHolder(
            FragmentPatientDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: PatientDetailsItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bind(item)
    }

}