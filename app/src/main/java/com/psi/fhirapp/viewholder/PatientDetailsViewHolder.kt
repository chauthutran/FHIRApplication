package com.psi.fhirapp.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.psi.fhirapp.R
import com.psi.fhirapp.data.PatientItem
import com.psi.fhirapp.databinding.FragmentPatientDetailsBinding
import com.psi.fhirapp.databinding.PatientDetailsCardViewBinding
import com.psi.fhirapp.fragments.PatientDetailData
import com.psi.fhirapp.fragments.PatientDetailProperty


class PatientDetailsViewHolder(private val binding: PatientDetailsCardViewBinding) :
    RecyclerView.ViewHolder(binding.root)  {

//    private val idTextView: TextView = binding.txtId
//    private val txtName: TextView = binding.txtName
//    private val txtGender: TextView = binding.txtGender
//    private val txtDob: TextView= binding.txtDob
//    private val txtPhone: TextView = binding.txtPhone

//    fun bind(patientDetailData: PatientDetailData) {
//
//        patientDetailData.firstInGroup.
//        println("======== PatientDetailsViewHolder.bind : ${patientItem.name}");
//        idTextView.text = patientItem.resourceId
//        txtName.text = patientItem.name
//        txtGender.text = patientItem.gender
//        txtDob.text = patientItem.dob.toString()
//        txtPhone.text = patientItem.phone
//    }

    fun bind(data: PatientDetailData) {
        (data as PatientDetailProperty).let {
            binding.name.text = it.patientProperty.header
            binding.fieldName.text = it.patientProperty.value
        }
    }
}

//class PatientDetailsViewHolder(itemView: View) :
//    RecyclerView.ViewHolder(itemView) {
//
//    private val idTextView: TextView = itemView.findViewById(R.id.txtId)
//    private val txtName: TextView =  itemView.findViewById(R.id.txtName)
//    private val txtGender: TextView =  itemView.findViewById(R.id.txtGender)
//    private val txtDob: TextView =  itemView.findViewById(R.id.txtDob)
//    private val txtPhone : TextView=  itemView.findViewById(R.id.txtPhone)
//
//
//    // Constructor
//    init {
//        this.courseModelArrayList = courseModelArrayList
//    }
//
//}
