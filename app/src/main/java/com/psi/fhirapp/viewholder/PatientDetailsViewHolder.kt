package com.psi.fhirapp.viewholder


import androidx.recyclerview.widget.RecyclerView
import com.psi.fhirapp.data.PatientDetailsData
import com.psi.fhirapp.databinding.FragmentPatientDetailsBinding
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter


class PatientDetailsViewHolder(private val binding: FragmentPatientDetailsBinding) :
    RecyclerView.ViewHolder(binding.root)  {

    fun bind(data: PatientDetailsData) {
        val formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy")

        binding.name.text = data.name
        binding.gender.text = data.gender
        binding.age.text = "${getAge(data.dob)}y"
        binding.dob.text = data.dob.format(formatter)
    }


    private fun getAge(date: LocalDate?): Int {
        if( date == null ) return 0

        return Period.between(
            LocalDate.of(date.year, date.month, date.dayOfMonth),
            LocalDate.now()
        ).years
    }
}
