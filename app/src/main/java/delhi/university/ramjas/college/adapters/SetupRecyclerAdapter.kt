package delhi.university.ramjas.college.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import delhi.university.ramjas.college.R
import delhi.university.ramjas.college.databinding.TextItemLayoutBinding
import delhi.university.ramjas.college.models.SetupItems
import delhi.university.ramjas.college.utils.loadUrl

class SetupRecyclerAdapter(
    var items: List<SetupItems>,
    val mContext: Context,
    private val listener: (SetupItems,View) -> Unit
) : RecyclerView.Adapter<SetupRecyclerAdapter.SetupRAViewHolder>() {


    inner class SetupRAViewHolder(val binding: TextItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetupRAViewHolder {
        val binding =
            TextItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SetupRAViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetupRAViewHolder, position: Int) {
        val item = items[position]
        holder.binding.itemName.text = item.itemName
        holder.binding.itemPic.loadUrl(item.itemPic)
        holder.binding.root.setOnClickListener {
            if (!item.isSelected){
                listener(item,it)
                it.setBackgroundColor(ResourcesCompat.getColor(mContext.resources, R.color.orangish, null))
                item.isSelected = true
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}