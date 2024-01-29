package com.psi.fhirapp.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.psi.fhirapp.data.ObservationListItem
import com.psi.fhirapp.databinding.FragmentPatientDetailsBinding
import com.psi.fhirapp.databinding.ObservationListItemBinding

class ObservationListItemViewHolder( private val binding: ObservationListItemBinding) :
    RecyclerView.ViewHolder(binding.root){

        private val code = binding.code
        private val value = binding.value

        fun bind(observationItem: ObservationListItem )
        {
            code.text = observationItem.code
            value.text = observationItem.value
        }
}