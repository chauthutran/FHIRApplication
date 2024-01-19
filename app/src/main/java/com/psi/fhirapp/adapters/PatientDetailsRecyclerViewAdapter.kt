package com.psi.fhirapp.adapters

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.psi.fhirapp.R
import com.psi.fhirapp.data.PatientItem
import com.psi.fhirapp.databinding.FragmentPatientDetailsBinding
import com.psi.fhirapp.databinding.PatientDetailsCardViewBinding
import com.psi.fhirapp.fragments.PatientDetailData
import com.psi.fhirapp.viewholder.PatientDetailsViewHolder

class PatientDetailsRecyclerViewAdapter( private val _context: Context
//, private val _patientList: PatientItem
):
    ListAdapter<PatientDetailData, PatientDetailsViewHolder>(PatientDetailDiffUtil()) {

    //Passing Values to Local Variables
//    private var context = _context
//    private var patientList = _patientList



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PatientDetailsViewHolder {
        println("===== PatientDetailsRecyclerViewAdapter.onCreateViewHolder")
        return PatientDetailsViewHolder(
            PatientDetailsCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

//    override fun getItemCount(): Int {
//
//        println("===== PatientDetailsRecyclerViewAdapter.getItemCount ")
//        return 1
//    }

    override fun onBindViewHolder(holder: PatientDetailsViewHolder, position: Int) {
        val item = getItem(position)
        println("===== PatientDetailsRecyclerViewAdapter.onBindViewHolder ")
//        holder.setIsRecyclable(false)
        holder.bind(item)
    }

}

class PatientDetailDiffUtil : DiffUtil.ItemCallback<PatientDetailData>() {
    override fun areItemsTheSame(o: PatientDetailData, n: PatientDetailData) = o == n

    override fun areContentsTheSame(o: PatientDetailData, n: PatientDetailData) =
        areItemsTheSame(o, n)
}
