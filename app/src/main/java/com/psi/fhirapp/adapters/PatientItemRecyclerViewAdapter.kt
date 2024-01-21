package com.psi.fhirapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.psi.fhirapp.databinding.FragmentPatientItemBinding
import com.psi.fhirapp.data.PatientListItem
import com.psi.fhirapp.viewholder.PatientItemViewHolder


/**
 * "ListAdapter"" class is a convenience wrapper around AsyncListDiffer that implements Adapter
 * common default behavior for item access and counting.
 * **/
class PatientItemRecyclerViewAdapter (val onClickItem: (view: View, patientListItem: PatientListItem) -> Unit):
    ListAdapter<PatientListItem, PatientItemViewHolder>(PatientDataDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientItemViewHolder {
        return PatientItemViewHolder(
            FragmentPatientItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PatientItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bind(item, createOnClickListener(item))
    }

    private fun createOnClickListener(patientListItem: PatientListItem): View.OnClickListener {
        return View.OnClickListener { view ->
            onClickItem(view, patientListItem)
        }
    }
}

class PatientDataDiffCallback  : DiffUtil.ItemCallback<PatientListItem>() {

    override fun areItemsTheSame(oldItem: PatientListItem, newItem: PatientListItem) : Boolean {
        // Patient properties may have changed if reloaded from the DB, but ID is fixed
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: PatientListItem,
        newItem: PatientListItem,
    ) : Boolean {
        // NOTE: if you use equals, your object must properly override Object#equals()
        // Incorrectly returning false here will result in too many animations.
        return oldItem.id == newItem.id
    }

}