package com.psi.fhirapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.psi.fhirapp.models.PatientListViewModel

class PatientDetailsRecyclerViewAdapter :
    ListAdapter<PatientListViewModel.ObservationItem, ObservationItemViewHolder>(
        ObservationItemDiffCallback(),
    ) {

    class ObservationItemDiffCallback :
        DiffUtil.ItemCallback<PatientListViewModel.ObservationItem>() {
        override fun areItemsTheSame(
            oldItem: PatientListViewModel.ObservationItem,
            newItem: PatientListViewModel.ObservationItem,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: PatientListViewModel.ObservationItem,
            newItem: PatientListViewModel.ObservationItem,
        ): Boolean = oldItem.id == newItem.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationItemViewHolder {
        return ObservationItemViewHolder(
            ObservationListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: ObservationItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item)
    }
}