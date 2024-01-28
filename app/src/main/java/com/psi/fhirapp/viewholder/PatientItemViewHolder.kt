package com.psi.fhirapp.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.psi.fhirapp.R
import com.psi.fhirapp.data.PatientListItem
import com.psi.fhirapp.databinding.FragmentPatientItemBinding
import java.time.LocalDate
import java.time.Period


/**
 * RecyclerView.ViewHolder is a ViewHolder describes an item view and metadata about its place
 * within the RecyclerView.
 * ViewHolders belong to the adapter. Adapters should feel free to use their own custom
 * ViewHolder implementations to store data that makes binding view contents easier.
 *
 * This class is used for populating Patient data to the view
 * **/
class PatientItemViewHolder(binding: FragmentPatientItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val patientIcon = binding.patientIcon
    private val name: TextView = binding.name
    private val id: TextView = binding.id
    private val address = binding.address

    fun bind(patientListItem: PatientListItem, onClickListener: View.OnClickListener) {

        // For icon
        if( patientListItem.gender == "male" ) {
            patientIcon.apply {
                setImageResource(R.drawable.male_patient)
            }
        }
        else {
            patientIcon.apply {
                setImageResource(R.drawable.female_patient)
            }
        }

        name.text = "${patientListItem.name}, ${getAge(patientListItem.dob)}"
        id.text = patientListItem.resourceId
        address.text = "${patientListItem.city}, ${patientListItem.country}"

        // Set up the click event
        itemView.setOnClickListener(onClickListener)
    }

    private fun getAge(date: LocalDate?): Int {
        if( date == null ) return 0

        return Period.between(
            LocalDate.of(date.year, date.month, date.dayOfMonth),
            LocalDate.now()
        ).years
    }
}
