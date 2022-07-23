package delhi.university.ramjas.college.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import delhi.university.ramjas.college.R
import delhi.university.ramjas.college.databinding.FragmentSetTwoBinding
import delhi.university.ramjas.college.parcels.SetupParcel
import delhi.university.ramjas.college.utils.snack


class SetTwoFragment : Fragment() {

    private lateinit var mContext: Context

    private var _binding: FragmentSetTwoBinding? = null

    private lateinit var setupParcel: SetupParcel
    private lateinit var interestList: MutableList<String>
    private lateinit var careerList: MutableList<String>
    private lateinit var societiesList: MutableList<String>

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var interestAdapter: ArrayAdapter<String>
    private lateinit var careerAdapter: ArrayAdapter<String>
    private lateinit var societiesAdapter: ArrayAdapter<String>

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val temp: SetupParcel? = arguments?.getParcelable("setupParcel")
        if (temp != null) setupParcel = temp
        else Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show()

        Log.i("SetTwoFragment", "onCreate: $setupParcel")

        interestList = mutableListOf(
            "Fishing",
            "Drawing",
            "Coding",
            "Singing",
            "Dancing",
            "Rafting",
            "Comedy",
            "Driving"
        )
        interestList.sort()
        careerList = mutableListOf("Fishing", "Drawing", "Coding", "Singing", "Dancing")
        careerList.sort()
        societiesList = mutableListOf(
            "Not a member of any society",
            "Shunya",
            "Qnights",
            "DebSoc",
            "TRMUN",
            "Placement Cell"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSetTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        interestAdapter = ArrayAdapter(
            mContext,
            R.layout.list_item,
            interestList
        )
        binding.interestEt.apply {
            setAdapter(interestAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
                val item: String = interestAdapter.getItem(pos).toString()
                val chipsList = binding.interestChipGroup.children.toList()
                val selectedChips = chipsList.joinToString(", ") { (it as Chip).text }
                if (selectedChips.contains(item) || chipsList.size > 4) {
                    text.clear()
                    binding.root.snack("Maximum selection limit reached", mContext)
                    return@OnItemClickListener
                }
                createChips(item, binding.interestChipGroup, interestAdapter)
                text.clear()
                showDropDown()
            }
        }
        careerAdapter = ArrayAdapter(
            mContext,
            R.layout.list_item,
            careerList
        )
        binding.careerEt.apply {
            setAdapter(careerAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
                val item: String = careerAdapter.getItem(pos).toString()
                val chipsList = binding.careerChipGroup.children.toList()
                val selectedChips = chipsList.joinToString(", ") { (it as Chip).text }
                if (selectedChips.contains(item) || chipsList.size > 2) {
                    text.clear()
                    binding.root.snack("Maximum selection limit reached", mContext)
                    return@OnItemClickListener
                }
                createChips(item, binding.careerChipGroup, careerAdapter)
                text.clear()
                showDropDown()
            }
        }
        societiesAdapter = ArrayAdapter(
            mContext,
            R.layout.list_item,
            societiesList
        )
        binding.societyEt.apply {
            setAdapter(societiesAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
                val item: String = societiesAdapter.getItem(pos).toString()
                val chipsList = binding.societyChipGroup.children.toList()
                val selectedChips = chipsList.joinToString(", ") { (it as Chip).text }
                val hasNoneSociety = chipsList.map { (it as Chip).text.toString() }
                    .toTypedArray().contains("Not a member of any society")
                if (selectedChips.contains(item) || hasNoneSociety) {
                    text.clear()
                    return@OnItemClickListener
                }
                societiesAdapter.remove("Not a member of any society")
                createChips(item, binding.societyChipGroup, societiesAdapter)
                text.clear()
                showDropDown()
            }
        }
        binding.nextBtn.setOnClickListener {
            val interestChips =
                binding.interestChipGroup.children.toList().map { (it as Chip).text.toString() }.toList()
            val careerChips =
                binding.careerChipGroup.children.toList().map { (it as Chip).text.toString() }.toList()
            val societyChips =
                binding.societyChipGroup.children.toList().map { (it as Chip).text.toString() }.toList()
            if (interestChips.isNotEmpty() && careerChips.isNotEmpty() && societyChips.isNotEmpty()) {
                setupParcel.hobbies = interestChips
                setupParcel.careers = careerChips
                setupParcel.societies = societyChips
                val bundle = bundleOf(
                    "setupParcel" to setupParcel
                )
                findNavController().navigate(R.id.action_setTwoFragment_to_setFinalFragment, bundle)
            } else {
                binding.root.snack("Something went wrong or missing!", mContext)
            }
        }
    }

    private fun createChips(item: String, chipGroup: ChipGroup, adapter: ArrayAdapter<String>) {
        adapter.remove(item)
        val chip = Chip(mContext)
        chip.text = item
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(it)
            adapter.add(item)
            adapter.sort { s, s2 -> s.compareTo(s2) }
            adapter.setNotifyOnChange(true)
        }
        chipGroup.apply {
            addView(chip)
            visibility = View.VISIBLE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


}