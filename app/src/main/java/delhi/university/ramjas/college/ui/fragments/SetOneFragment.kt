package delhi.university.ramjas.college.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import delhi.university.ramjas.college.R
import delhi.university.ramjas.college.databinding.FragmentSetOneBinding
import delhi.university.ramjas.college.parcels.SetupParcel


class SetOneFragment : Fragment() {
    private var _binding: FragmentSetOneBinding? = null
    private lateinit var mContext: Context

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!



    private lateinit var setupParcel: SetupParcel

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val temp: SetupParcel? = arguments?.getParcelable("setupParcel")
        if (temp != null) setupParcel = temp
        else Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show()

        Log.i("SetOneFragment", "onCreate: $setupParcel")

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSetOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val homestateAdapter = ArrayAdapter(
            mContext,
            R.layout.list_item,
            resources.getStringArray(R.array.state_list)
        )
        binding.homestate.apply {
            setAdapter(homestateAdapter)
            onItemClickListener = OnItemClickListener { _, _, pos, _ ->
                setupParcel.homestate = homestateAdapter.getItem(pos).toString()
            }
        }

        val deptAdapter = ArrayAdapter(
            mContext,
            R.layout.list_item,
            resources.getStringArray(R.array.dept_list)
        )
        binding.department.apply {
            setAdapter(deptAdapter)
            onItemClickListener = OnItemClickListener { _, _, pos, _ ->
                setupParcel.department = deptAdapter.getItem(pos).toString()

            }
        }

        val semesterList = listOf("I", "II", "III", "IV", "V", "VI")
        val semesterAdapter = ArrayAdapter(mContext, R.layout.list_item, semesterList)
        binding.semester.apply {
            setAdapter(semesterAdapter)
            onItemClickListener = OnItemClickListener { _, _, pos, _ ->
                setupParcel.semester = semesterAdapter.getItem(pos).toString()
            }
        }
        binding.nextBtn.setOnClickListener {
            Log.i("SetOneFragment", "onViewCreated: $setupParcel")
            val bundle = bundleOf(
                "setupParcel" to setupParcel
            )
            findNavController().navigate(R.id.action_setOneFragment_to_setTwoFragment, bundle)
        }


    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

}