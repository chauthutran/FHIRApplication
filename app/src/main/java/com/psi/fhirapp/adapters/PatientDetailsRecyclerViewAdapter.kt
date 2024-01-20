package com.psi.fhirapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.psi.fhirapp.databinding.PatientDetailsCardViewBinding
import com.psi.fhirapp.data.PatientDetailProperty
import com.psi.fhirapp.viewholder.PatientDetailsViewHolder

class PatientDetailsRecyclerViewAdapter( private val _context: Context ):
    ListAdapter<PatientDetailProperty, PatientDetailsViewHolder>(PatientDetailDiffUtil()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PatientDetailsViewHolder {
        return PatientDetailsViewHolder(
            PatientDetailsCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: PatientDetailsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

}

class PatientDetailDiffUtil : DiffUtil.ItemCallback<PatientDetailProperty>() {
    override fun areItemsTheSame(o: PatientDetailProperty, n: PatientDetailProperty) = o == n

    override fun areContentsTheSame(o: PatientDetailProperty, n: PatientDetailProperty) =
        areItemsTheSame(o, n)
}
