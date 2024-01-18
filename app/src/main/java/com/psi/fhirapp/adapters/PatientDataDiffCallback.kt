package com.psi.fhirapp.adapters

import androidx.recyclerview.widget.DiffUtil
import com.psi.fhirapp.data.PatientItem

class PatientDataDiffCallback  : DiffUtil.ItemCallback<PatientItem>() {
//        override fun areItemsTheSame(oldItem: PatientItem, newItem: PatientItem) = oldItem.id == newItem.id
//
//        override fun areContentsTheSame(
//            oldItem: PatientItem,
//            newItem: PatientItem,
//        ) c = oldItem.id == newItem.id && oldItem.risk == newItem.risk

    override fun areItemsTheSame(oldItem: PatientItem, newItem: PatientItem) : Boolean {
        // Patient properties may have changed if reloaded from the DB, but ID is fixed
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: PatientItem,
        newItem: PatientItem,
    ) : Boolean {
        // NOTE: if you use equals, your object must properly override Object#equals()
        // Incorrectly returning false here will result in too many animations.
        return oldItem.id == newItem.id && oldItem.risk == newItem.risk
    }

}