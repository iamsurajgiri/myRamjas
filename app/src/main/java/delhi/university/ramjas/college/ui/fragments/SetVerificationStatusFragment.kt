package delhi.university.ramjas.college.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import delhi.university.ramjas.college.R
import delhi.university.ramjas.college.databinding.FragmentSetVerificationStatusBinding
import delhi.university.ramjas.college.parcels.SetupParcel

class SetVerificationStatusFragment : Fragment() {

    private var _binding: FragmentSetVerificationStatusBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var setupParcel: SetupParcel
    private lateinit var mContext: Context

    private var mCurrentUser: FirebaseUser? = null

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSetVerificationStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val temp: SetupParcel? = arguments?.getParcelable("setupParcel")
        if (temp != null) setupParcel = temp
        else Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show()

        Log.i("SetVerificationStatusFragment", "onCreate: $setupParcel")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (setupParcel.verification == "success") {
            binding.verificationStatus.text = getString(R.string.successfully_verified)
            binding.next.text = getString(R.string.next)
            binding.root.setBackgroundColor(resources.getColor(R.color.dark_green, null))
        } else {
            binding.verificationStatus.text = getString(R.string.failed_verification)
            binding.root.setBackgroundColor(resources.getColor(R.color.orangeDark, null))
            binding.next.text = getString(R.string.try_again)
        }
        binding.next.setOnClickListener {
            if (setupParcel.verification == "success") {
                val bundle = bundleOf(
                    "setupParcel" to setupParcel
                )
                findNavController().navigate(
                    R.id.action_setVerificationStatusFragment_to_selfFragment,
                    bundle
                )
            } else {
                findNavController().navigate(R.id.action_setVerificationStatusFragment_to_setZeroFragment)

            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
}