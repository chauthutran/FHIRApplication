package com.psi.fhirapp.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.FhirEngine
import com.psi.fhirapp.FhirApplication
import com.psi.fhirapp.MainActivity
import com.psi.fhirapp.R
import com.psi.fhirapp.databinding.FragmentPatientDetailsBinding
import com.psi.fhirapp.models.PatientDetailsViewModel
import com.psi.fhirapp.viewholder.PatientDetailsViewHolder
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.ResourceType


class PatientDetailsFragment : Fragment() {

    private lateinit var _binding: FragmentPatientDetailsBinding
    private lateinit var viewModel: PatientDetailsViewModel
    private lateinit var fhirEngine: FhirEngine

    private val args: PatientDetailsFragmentArgs by navArgs()


    private var editMenuItem: MenuItem? = null

    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPatientDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.patient_details)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.toolbar_back_button)
        }
        (activity as MainActivity).setDrawerEnabled(false)

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        viewModel = PatientDetailsViewModel(requireActivity().application, fhirEngine, args.patientId )


        var viewHolder = PatientDetailsViewHolder(binding)
//
//
//        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
//            title = getString(R.string.patient_details)
//            setDisplayHomeAsUpEnabled(true)
//        }

        viewModel.livePatientData.observe(viewLifecycleOwner) {
//            adapter.submitList(it)
//            if (!it.isNullOrEmpty()) {

                val viewHolder = PatientDetailsViewHolder(binding)
                viewHolder.bind(viewModel.livePatientData.value!!)
                // Enable the Edit icon
                editMenuItem?.isEnabled = true
//            }
        }
        viewModel.getPatientDetailData()
        (activity as MainActivity).setDrawerEnabled(false)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.patient_details_menu, menu)
        editMenuItem = menu.findItem(R.id.menu_patient_edit)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavHostFragment.findNavController(this).navigateUp()
                true
            }
            R.id.menu_patient_edit -> {

                val bundle = Bundle()
                bundle.putString("patient_id", args.patientId)
                findNavController().navigate(R.id.nav_patient_details_to_edit_patient, bundle)
                true
            }
            R.id.menu_patient_delete -> {
                showNoticeDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showNoticeDialog() {
        var me = this

        var builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Message need to set here ??")
            .setTitle("This is title")
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, id ->
                    viewLifecycleOwner.lifecycleScope.launch {

                        /**
                         * This function is not worked properly if the patient has relationship with another resource
                         */
                        fhirEngine.delete(ResourceType.Patient, args.patientId)
                        Toast.makeText(requireContext(), "Patient is deleted.", Toast.LENGTH_SHORT).show()

                        // Refresh
                        val myIntent = Intent( context, MainActivity::class.java )
                        requireActivity().startActivity(myIntent)
                    }
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                })
        builder.show();
    }

}
