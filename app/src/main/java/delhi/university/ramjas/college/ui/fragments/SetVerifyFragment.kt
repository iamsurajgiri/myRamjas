package delhi.university.ramjas.college.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import delhi.university.ramjas.college.R
import delhi.university.ramjas.college.databinding.FragmentSetVerifyBinding
import delhi.university.ramjas.college.parcels.SetupParcel
import delhi.university.ramjas.college.utils.snack


class SetVerifyFragment : Fragment() {

    private var _binding: FragmentSetVerifyBinding? = null
    private lateinit var mContext: Context

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSetVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        var isRollNo = true
        val verificationList = listOf("By Roll No.", "By Reference No.")
        val verificationAdapter =
            ArrayAdapter(mContext, R.layout.list_item, verificationList)
        binding.verify.apply {
            setAdapter(verificationAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
                when (pos){
                    0 -> {
                        binding.rollNoTIL.hint = "Enter Roll No."
                        binding.rollNoTIL.helperText = "As per your ID Card e.g 2020/29/0129"
                        binding.rollNoTIL.visibility = View.VISIBLE
                        isRollNo = true
                    }
                    1 ->{
                        binding.rollNoTIL.hint = "Enter Reference No."
                        binding.rollNoTIL.helperText = "As per your Acknowledgment Slip"
                        binding.rollNoTIL.visibility = View.VISIBLE
                        isRollNo = false
                    }
                }
            }
        }

        binding.rollNoTIL.setEndIconOnClickListener {
            val rollNo = binding.rollNoEt.text.toString().trim()
            if (isRollNo){
                if(rollNo.isNotEmpty() && useRegex(rollNo)){
                    binding.tv2.text = getString(R.string.rollno_method)
                    binding.verificationMethodTIL.visibility = View.VISIBLE
                    binding.constraintLayout.visibility = View.VISIBLE
                    binding.rollNoTIL.isEnabled = false
                    binding.tv0.isEnabled = false
                }else{
                    binding.root.snack(getString(R.string.enter_roll),mContext)
                }
            }
            else{
                if(rollNo.isNotEmpty()){
                    binding.tv2.text = getString(R.string.refno_method)
                    binding.verificationMethodTIL.visibility = View.VISIBLE
                    binding.constraintLayout.visibility = View.VISIBLE
                    binding.rollNoTIL.isEnabled = false
                    binding.tv0.isEnabled = false
                }else{
                    binding.root.snack(getString(R.string.enter_ref),mContext)
                }
            }



        }

        binding.scan.setOnClickListener {

            if(isRollNo){
                val rollNo = binding.rollNoEt.text.toString().trim()
                if (rollNo.isEmpty() && !useRegex(rollNo)) {
                    binding.root.snack(getString(R.string.enter_roll), mContext)
                    return@setOnClickListener
                }
                val setupParcel = SetupParcel()
                setupParcel.collegeID = rollNo
                setupParcel.idType = "roll"
                val bundle = bundleOf(
                    "setupParcel" to setupParcel
                )
                findNavController().navigate(
                    R.id.action_setVerifyFragment_to_barcodeFragment,
                    bundle)
            }else{
                val rollNo = binding.rollNoEt.text.toString().trim()
                if (rollNo.isEmpty()) {
                    binding.root.snack(getString(R.string.enter_ref), mContext)
                    return@setOnClickListener
                }

                val setupParcel = SetupParcel()
                setupParcel.collegeID = rollNo
                setupParcel.idType = "ref"
                val bundle = bundleOf(
                    "setupParcel" to setupParcel
                )
                findNavController().navigate(
                    R.id.action_setVerifyFragment_to_cameraFragment,
                    bundle)
            }
        }

        binding.next.setOnClickListener {
            val rollNo = binding.rollNoEt.text.toString().trim()
            if (isRollNo){
                if (rollNo.isEmpty() && !useRegex(rollNo)) {
                    binding.root.snack(getString(R.string.enter_roll), mContext)
                    return@setOnClickListener
                }
                val setupParcel = SetupParcel()
                setupParcel.collegeID = rollNo
                setupParcel.idType = "roll"
                setupParcel.verification = "pending"

                val bundle = bundleOf(
                    "setupParcel" to setupParcel
                )
                findNavController().navigate(
                    R.id.action_setVerifyFragment_to_selfFragment,
                    bundle)
            }else{
                if (rollNo.isEmpty()) {
                    binding.root.snack(getString(R.string.enter_ref), mContext)
                    return@setOnClickListener
                }
                val setupParcel = SetupParcel()
                setupParcel.collegeID = rollNo
                setupParcel.idType = "ref"
                setupParcel.verification = "pending"

                val bundle = bundleOf(
                    "setupParcel" to setupParcel
                )
                findNavController().navigate(
                    R.id.action_setVerifyFragment_to_selfFragment, bundle)
            }

        }

        val processList = listOf("Instant", "Prolonged")
        val processAdapter =
            ArrayAdapter(mContext, R.layout.list_item, processList)
        binding.verificationMethod.apply {
            setAdapter(processAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
                when(pos){
                    0 ->{
                        binding.verificationStatus.visibility = View.GONE
                        binding.scan.visibility = View.VISIBLE
                        binding.next.visibility = View.GONE
                    }
                    1 ->{
                        binding.verificationStatus.visibility = View.VISIBLE
                        binding.scan.visibility = View.GONE
                        binding.next.visibility = View.VISIBLE
                    }
                }
            }
        }


    }
    private fun useRegex(input: String): Boolean {
        val regex = Regex(pattern = ".*/.*/.*", options = setOf(RegexOption.IGNORE_CASE))
        return regex.matches(input)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

}