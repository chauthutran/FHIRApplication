package com.psi.fhirapp.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.FhirEngine
import com.psi.fhirapp.FhirApplication
import com.psi.fhirapp.MainActivity
import com.psi.fhirapp.R
import com.psi.fhirapp.adapters.PatientDetailsRecyclerViewAdapter
import com.psi.fhirapp.databinding.FragmentPatientDetailsBinding
import com.psi.fhirapp.dialog.NoticeDialogFragment
import com.psi.fhirapp.dialog.NoticeDialogListener
import com.psi.fhirapp.models.PatientDetailsViewModel
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.ResourceType

//class PatientDetailsFragment : Fragment(), NoticeDialogListener {
class PatientDetailsFragment : Fragment() {

    private lateinit var _binding: FragmentPatientDetailsBinding
    private lateinit var viewModel: PatientDetailsViewModel
    private lateinit var fhirEngine: FhirEngine

    private var isPatientDeleted = false

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
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        viewModel = PatientDetailsViewModel(requireActivity().application, fhirEngine, args.patientId )
        var adapter = PatientDetailsRecyclerViewAdapter(requireContext())
        binding.recyclerDetails.adapter = adapter

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Patient Details"
            setDisplayHomeAsUpEnabled(true)
        }

        viewModel.livePatientData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            if (!it.isNullOrEmpty()) {
                editMenuItem?.isEnabled = true
            }
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
        println("==== showNoticeDialog ")
//        // Create an instance of the dialog fragment and show it.
//        val dialog = NoticeDialogFragment()
////        dialog.show(childFragmentManager, "NoticeDialogFragment")
//        dialog.show(requireActivity().supportFragmentManager, "NoticeDialogFragment")

        var me = this;
        var builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Message need to set here ??")
            .setTitle("This is title")
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, id ->
                    println("-- OK ${args.patientId}")
                    viewLifecycleOwner.lifecycleScope.launch {
                        println("-- OK viewLifecycleOwner.lifecycleScope.launch ")
                        fhirEngine.delete(ResourceType.Patient, args.patientId)
                        Toast.makeText(requireContext(), "Patient is deleted.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                })
        builder.show();
    }

}
