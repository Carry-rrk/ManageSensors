package com.rrk.managesensors


import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rrk.managesensors.databinding.ItemAppBinding

data class AppInfo(
    val packageName: String, 
    val appName: String, 
    val icon: Drawable,
    var isSelected: Boolean = false
)

class AppAdapter(private val context: Context) : 
    RecyclerView.Adapter<AppAdapter.ViewHolder>() {
    
    private var appList = mutableListOf<AppInfo>()

    fun updateList(newList: List<AppInfo>) {
        appList.clear()
        appList.addAll(newList)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = appList[position]
        holder.binding.apply {
            ivAppIcon.setImageDrawable(app.icon)
            tvAppName.text = app.appName
            tvPackageName.text = app.packageName
            
            root.setOnClickListener {
                val intent = Intent(context, AppDetailActivity::class.java).apply {
                    putExtra("package_name", app.packageName)
                    putExtra("app_name", app.appName)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = appList.size

    class ViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)
} 