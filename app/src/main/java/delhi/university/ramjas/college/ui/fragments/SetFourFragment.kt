package delhi.university.ramjas.college.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import delhi.university.ramjas.college.R
import delhi.university.ramjas.college.databinding.FragmentSetFourBinding
import delhi.university.ramjas.college.parcels.SetupParcel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class SetFourFragment : Fragment() {

    private lateinit var mContext: Context

    private var _binding: FragmentSetFourBinding? = null

    private lateinit var setupParcel: SetupParcel

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!


    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mCurrentUser: FirebaseUser? = null
    private var mUserCollection: CollectionReference? = null


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val temp:SetupParcel? = arguments?.getParcelable("setupParcel")
        if (temp!=null) setupParcel = temp
        else Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show()

        //Firebase
        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = mAuth.currentUser



    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSetFourBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userid = mCurrentUser?.uid

        if(userid!=null) {
            mUserCollection =
                FirebaseFirestore.getInstance().collection("College").document("RamjasCollege")
                    .collection("Users")
            Log.i(SetFourFragment::javaClass.name, "onViewCreated: $setupParcel")

            CoroutineScope(Dispatchers.IO).launch {
                mUserCollection?.document(userid)?.set(setupParcel)?.await()
                withContext(Dispatchers.Main) {
                    findNavController().navigate(R.id.action_setFourFragment_to_mainActivity)
                }
            }
        }
    }




    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


}