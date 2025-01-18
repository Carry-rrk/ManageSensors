package com.rrk.managesensors

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rrk.managesensors.databinding.ItemPermissionBinding

class PermissionAdapter(
    private val context: Context,
    private val onTogglePermission: (AppOpsInfo) -> Unit
) : RecyclerView.Adapter<PermissionAdapter.ViewHolder>() {

    private val permissions = mutableListOf<AppOpsInfo>()

    fun updatePermissions(newPermissions: List<AppOpsInfo>) {
        permissions.clear()
        permissions.addAll(newPermissions)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemPermissionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPermissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val permission = permissions[position]
        holder.binding.apply {
            tvPermissionName.text = permission.name
            tvPermissionDescription.text = permission.description
            btnToggle.apply {
                text = if (permission.isEnabled) "已启用" else "已禁用"
                setBackgroundColor(if (permission.isEnabled) Color.GREEN else Color.RED)
                setOnClickListener {
                    onTogglePermission(permission)
                }
            }
        }
    }

    override fun getItemCount() = permissions.size
} 