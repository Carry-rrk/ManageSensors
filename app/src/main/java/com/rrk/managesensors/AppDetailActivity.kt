package com.rrk.managesensors

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rrk.managesensors.databinding.ActivityAppDetailBinding
import rikka.shizuku.Shizuku
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class AppDetailActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "AppDetailActivity"
        const val EXTRA_PACKAGE_NAME = "package_name"
    }

    private lateinit var binding: ActivityAppDetailBinding
    private lateinit var permissionAdapter: PermissionAdapter
    private var currentPackageName: String? = null
    private var appOpsService: IAppOpsService? = null
    private var permissions = mutableListOf<AppOpsInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        Log.d(TAG, "收到包名: $currentPackageName")
        
        if (currentPackageName == null) {
            Toast.makeText(this, "包名为空，无法加载权限", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 先初始化空的适配器
        setupRecyclerView()
        
        // 显示加载动画
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.rvPermissions.visibility = View.GONE

        // 连接服务
        initAppOpsService()
    }

    private fun initAppOpsService() {
        try {
            Log.d(TAG, "开始初始化 AppOpsService")
            val binder = ShizukuBinderWrapper(SystemServiceHelper.getSystemService("appops"))
            appOpsService = IAppOpsService.Stub.asInterface(binder)
            Log.d(TAG, "AppOpsService 初始化成功")
            
            // 服务连接成功后，开始加载权限列表
            permissions.clear()
            permissions.addAll(AppOpsPermissions.permissions)
            Log.d(TAG, "权限列表初始化完成，数量: ${permissions.size}")
            permissionAdapter.updatePermissions(permissions)
            
            // 加载权限状态
            Log.d(TAG, "开始加载权限状态")
            loadAppOpsStatus()
        } catch (e: Exception) {
            Log.e(TAG, "初始化 AppOpsService 失败", e)
            Toast.makeText(this, "服务连接失败: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.loadingOverlay.visibility = View.GONE
            binding.rvPermissions.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        permissionAdapter = PermissionAdapter(this) { permission ->
            togglePermission(permission)
        }
        binding.rvPermissions.apply {
            layoutManager = LinearLayoutManager(this@AppDetailActivity)
            adapter = permissionAdapter
        }
    }

    private fun loadAppOpsStatus() {
        currentPackageName?.let { pkgName ->
            Log.d(TAG, "开始加载包 $pkgName 的权限状态")
            lifecycleScope.launch {
                try {
                    // 在后台获取所有权限状态
                    val updatedPermissions = withContext(Dispatchers.IO) {
                        permissions.map { permission ->
                            try {
                                Log.d(TAG, "正在获取权限 ${permission.name} 的状态")
                                delay(50)
                                val isEnabled = appOpsService?.getAppOps(pkgName, permission.name)
                                Log.d(TAG, "权限 ${permission.name} 状态: $isEnabled")
                                permission.copy(isEnabled = isEnabled ?: false)
                            } catch (e: Exception) {
                                Log.e(TAG, "获取权限状态失败: ${permission.name}", e)
                                permission
                            }
                        }
                    }

                    Log.d(TAG, "所有权限状态获取完成")
                    // 获取完成后，更新UI
                    permissions.clear()
                    permissions.addAll(updatedPermissions)
                    
                    // 隐藏加载动画，显示列表
                    Log.d(TAG, "更新UI")
                    binding.loadingOverlay.visibility = View.GONE
                    binding.rvPermissions.visibility = View.VISIBLE
                    
                    // 更新适配器数据
                    permissionAdapter.updatePermissions(permissions)
                    Log.d(TAG, "UI更新完成")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "加载权限状态失败", e)
                    binding.loadingOverlay.visibility = View.GONE
                    binding.rvPermissions.visibility = View.VISIBLE
                    Toast.makeText(this@AppDetailActivity, "获取权限状态失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Log.e(TAG, "packageName 为空")
            binding.loadingOverlay.visibility = View.GONE
            binding.rvPermissions.visibility = View.VISIBLE
            Toast.makeText(this, "包名为空", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePermission(permission: AppOpsInfo) {
        currentPackageName?.let { pkgName ->
            if (appOpsService == null) {
                Log.e(TAG, "appOpsService 为空")
                Toast.makeText(this, "服务未连接", Toast.LENGTH_SHORT).show()
                return@let
            }

            lifecycleScope.launch {
                try {
                    binding.loadingOverlay.visibility = View.VISIBLE
                    
                    val success = withContext(Dispatchers.IO) {
                        try {
                            appOpsService?.setAppOps(pkgName, permission.name, !permission.isEnabled)
                            delay(100)
                            val currentState = appOpsService?.getAppOps(pkgName, permission.name)
                            currentState == !permission.isEnabled
                        } catch (e: Exception) {
                            Log.e(TAG, "权限操作失败", e)
                            false
                        }
                    }

                    binding.loadingOverlay.visibility = View.GONE

                    if (success) {
                        permission.isEnabled = !permission.isEnabled
                        val position = permissions.indexOfFirst { it.name == permission.name }
                        if (position != -1) {
                            permissionAdapter.notifyItemChanged(position)
                        }
                        
                        Toast.makeText(
                            this@AppDetailActivity,
                            "${if (permission.isEnabled) "启用" else "禁用"}${permission.description}成功",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@AppDetailActivity,
                            "${if (!permission.isEnabled) "启用" else "禁用"}${permission.description}失败",
                            Toast.LENGTH_SHORT
                        ).show()
                        val currentState = appOpsService?.getAppOps(pkgName, permission.name) ?: false
                        permission.isEnabled = currentState
                        val position = permissions.indexOfFirst { it.name == permission.name }
                        if (position != -1) {
                            permissionAdapter.notifyItemChanged(position)
                        }
                    }
                } catch (e: Exception) {
                    binding.loadingOverlay.visibility = View.GONE
                    Toast.makeText(
                        this@AppDetailActivity,
                        "操作失败: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "切换权限失败", e)
                    val currentState = appOpsService?.getAppOps(pkgName, permission.name) ?: false
                    permission.isEnabled = currentState
                    val position = permissions.indexOfFirst { it.name == permission.name }
                    if (position != -1) {
                        permissionAdapter.notifyItemChanged(position)
                    }
                }
            }
        }
    }
} 