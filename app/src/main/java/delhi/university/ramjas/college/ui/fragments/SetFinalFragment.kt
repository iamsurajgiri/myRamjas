package delhi.university.ramjas.college.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import delhi.university.ramjas.college.R
import delhi.university.ramjas.college.databinding.FragmentSetFinalBinding
import delhi.university.ramjas.college.parcels.SetupParcel

class SetFinalFragment : Fragment() {
    private lateinit var mContext: Context
    private lateinit var setupParcel: SetupParcel


    private var _binding: FragmentSetFinalBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val temp:SetupParcel? = arguments?.getParcelable("setupParcel")
        if (temp!=null) setupParcel = temp
        else Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show()

        Log.i("SetFinalFragment", "onCreate: $setupParcel")
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentSetFinalBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.nextBtn.setOnClickListener {
            val whatsapp = "+91" + binding.whatsapp.text.toString().trim()
            val instagram = binding.instagram.text.toString().trim()
            val facebook = binding.facebook.text.toString().trim()
            val twitter = binding.twitter.text.toString().trim()


            val socialMap = mapOf(
                "whatsapp" to whatsapp,
                "instagram" to instagram,
                "facebook" to facebook,
                "twitter" to twitter
            )

            setupParcel.socials = socialMap

            Log.i("SetFinalFragment", "onViewCreated: $whatsapp $instagram $facebook $twitter")
            val bundle = bundleOf(
                "setupParcel" to setupParcel
            )
            findNavController().navigate(R.id.action_setFinalFragment_to_setFourFragment, bundle)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
}