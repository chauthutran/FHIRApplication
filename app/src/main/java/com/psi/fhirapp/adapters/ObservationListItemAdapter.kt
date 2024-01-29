package com.psi.fhirapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.psi.fhirapp.data.ObservationListItem
import com.psi.fhirapp.data.PatientDetailsData
import com.psi.fhirapp.databinding.ObservationListItemBinding
import com.psi.fhirapp.viewholder.ObservationListItemViewHolder

class ObservationListItemAdapter(): ListAdapter<ObservationListItem, ObservationListItemViewHolder>(ObservationItemDiffCallback()){


    class ObservationItemDiffCallback :
        DiffUtil.ItemCallback<ObservationListItem>() {
        override fun areItemsTheSame(
            oldItem: ObservationListItem,
            newItem: ObservationListItem,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ObservationListItem,
            newItem: ObservationListItem,
        ): Boolean = oldItem.id == newItem.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationListItemViewHolder {
        return ObservationListItemViewHolder(
            ObservationListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: ObservationListItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bind(item)
    }

}

