package com.rrk.managesensors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rrk.managesensors.databinding.ActivityMainBinding
import rikka.shizuku.Shizuku
import android.widget.SearchView
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.content.ComponentName
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import com.rrk.managesensors.IAppOpsService
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rrk.managesensors.databinding.ItemAppBinding
import android.content.pm.ApplicationInfo

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val SHIZUKU_CODE = 1
    private lateinit var adapter: AppAdapter
    private lateinit var searchView: SearchView
    private val appList = mutableListOf<AppInfo>()
    private var appOpsService: IAppOpsService? = null
    private lateinit var serviceArgs: Shizuku.UserServiceArgs
    
    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            appOpsService = IAppOpsService.Stub.asInterface(binder)
            Log.d("MainActivity", "Service 已连接")
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            appOpsService = null
            Log.d("MainActivity", "Service 已断开")
        }
    }

    // Shizuku 权限结果监听器
    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                binding.shizukuStatus.text = "Shizuku 已授权"
                binding.shizukuStatus.setTextColor(Color.GREEN)
            } else {
                binding.shizukuStatus.text = "Shizuku 未授权"
                binding.shizukuStatus.setTextColor(Color.RED)
            }
        }
    }

    // Shizuku Binder 接收监听器
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        runOnUiThread { checkShizukuPermission() }
    }

    // Shizuku Binder 死亡监听器
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        runOnUiThread {
            binding.shizukuStatus.text = "Shizuku 服务已断开"
            binding.shizukuStatus.setTextColor(Color.RED)
        }
    }

    private val binderReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "rikka.shizuku.intent.action.BINDER_RECEIVED" -> {
                    checkShizukuStatus()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // 注册 Shizuku 监听器
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)

        registerReceiver(
            binderReceiver,
            IntentFilter("rikka.shizuku.intent.action.BINDER_RECEIVED"),
            Context.RECEIVER_NOT_EXPORTED
        )
        
        initSearchView()
        
        initRecyclerView()
        loadApps()

        checkShizukuPermission()
    }

    private fun initSearchView() {
        searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (newText.isNullOrEmpty()) {
                    appList
                } else {
                    appList.filter { 
                        it.appName.lowercase().contains(newText.lowercase()) ||
                        it.packageName.lowercase().contains(newText.lowercase())
                    }
                }
                adapter.updateList(filteredList)
                return true
            }
        })
    }

    private fun initRecyclerView() {
        adapter = AppAdapter(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadApps() {
        val pm = packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        
        appList.clear()
        for (packageInfo in packages) {
            val appInfo = AppInfo(
                packageName = packageInfo.packageName,
                appName = packageInfo.applicationInfo.loadLabel(pm).toString(),
                icon = packageInfo.applicationInfo.loadIcon(pm)
            )
            appList.add(appInfo)
        }
        
        appList.sortBy { it.appName.lowercase() }
        adapter.updateList(appList)
    }

    private fun checkShizukuStatus() {
        try {
            // 首先检查 Binder 是否已经准备好
            if (!Shizuku.pingBinder()) {
                binding.shizukuStatus.text = "等待 Shizuku 服务..."
                binding.shizukuStatus.setTextColor(Color.YELLOW)
                
                // 尝试请求 Binder
                if (Shizuku.shouldShowRequestPermissionRationale()) {
                    binding.shizukuStatus.text = "请授权 Shizuku 权限"
                    requestShizukuPermission()
                } else {
                    // 如果 Shizuku 服务未运行，尝试启动它
                    try {
                        val intent = Intent("rikka.shizuku.intent.action.REQUEST_BINDER")
                            .setPackage("moe.shizuku.privileged.api")
                        startActivity(intent)
                    } catch (e: Exception) {
                        binding.shizukuStatus.text = "Shizuku 未安装或未运行"
                        binding.shizukuStatus.setTextColor(Color.RED)
                        Log.e("MainActivity", "无法启动 Shizuku", e)
                    }
                }
                return
            }

            // Binder 已准备好，检查权限
            when {
                Shizuku.isPreV11() -> {
                    binding.shizukuStatus.text = "Shizuku 版本过低"
                    binding.shizukuStatus.setTextColor(Color.RED)
                }
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> {
                    binding.shizukuStatus.text = "Shizuku 已授权"
                    binding.shizukuStatus.setTextColor(Color.GREEN)
                }
                else -> {
                    binding.shizukuStatus.text = "Shizuku 未授权"
                    binding.shizukuStatus.setTextColor(Color.YELLOW)
                    requestShizukuPermission()
                }
            }
        } catch (e: Exception) {
            binding.shizukuStatus.text = "Shizuku 状态检查失败: ${e.message}"
            binding.shizukuStatus.setTextColor(Color.RED)
            Log.e("MainActivity", "Shizuku 检查失败", e)
        }
    }

    private fun requestShizukuPermission() {
        try {
            Shizuku.requestPermission(SHIZUKU_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "请求 Shizuku 权限失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkShizukuPermission() {
        try {
            if (Shizuku.isPreV11()) {
                binding.shizukuStatus.text = "Shizuku 版本过低"
                binding.shizukuStatus.setTextColor(Color.RED)
                return
            }

            when {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> {
                    binding.shizukuStatus.text = "Shizuku 已授权"
                    binding.shizukuStatus.setTextColor(Color.GREEN)
                }
                Shizuku.shouldShowRequestPermissionRationale() -> {
                    binding.shizukuStatus.text = "Shizuku 权限被永久拒绝"
                    binding.shizukuStatus.setTextColor(Color.RED)
                }
                else -> {
                    binding.shizukuStatus.text = "请求 Shizuku 权限..."
                    binding.shizukuStatus.setTextColor(Color.YELLOW)
                    Shizuku.requestPermission(SHIZUKU_CODE)
                }
            }
        } catch (e: Exception) {
            binding.shizukuStatus.text = "Shizuku 未运行"
            binding.shizukuStatus.setTextColor(Color.RED)
            Log.e("MainActivity", "Shizuku 检查失败", e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            SHIZUKU_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding.shizukuStatus.text = "Shizuku 已授权"
                    binding.shizukuStatus.setTextColor(Color.GREEN)
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(binderReceiver)
        // 移除所有监听器
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        // 解绑服务
        if (::serviceArgs.isInitialized) {
            try {
                Shizuku.unbindUserService(serviceArgs, serviceConnection, true)
            } catch (e: Exception) {
                Log.e("MainActivity", "解绑服务失败", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 页面恢复时延迟检查状态，给 Binder 一些准备时间
        binding.root.postDelayed({
            checkShizukuStatus()
        }, 500)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                checkShizukuStatus()
                true
            }
            R.id.action_disable_vibrate -> {
                setDouyinPermission("VIBRATE", false)
                true
            }
            R.id.action_enable_vibrate -> {
                setDouyinPermission("VIBRATE", true)
                true
            }
            R.id.action_disable_mic -> {
                setDouyinPermission("RECORD_AUDIO", false)
                true
            }
            R.id.action_enable_mic -> {
                setDouyinPermission("RECORD_AUDIO", true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setDouyinPermission(permission: String, enabled: Boolean) {
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "请先授予 Shizuku 权限", Toast.LENGTH_SHORT).show()
            return
        }

        // 绑定服务
        serviceArgs = Shizuku.UserServiceArgs(ComponentName(packageName, AppOpsService::class.java.name))
            .daemon(false)
            .processNameSuffix("appops")
            .version(1)
        
        try {
            Shizuku.bindUserService(serviceArgs, serviceConnection)
            
            // 延迟执行，确保服务已连接
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    // 查找抖音包名
                    val douyin = appList.find { it.appName.contains("抖音") }
                    if (douyin != null) {
                        appOpsService?.setAppOps(douyin.packageName, permission, enabled)
                        val status = if (enabled) "启用" else "禁用"
                        val permissionName = when(permission) {
                            "VIBRATE" -> "震动"
                            "RECORD_AUDIO" -> "麦克风"
                            else -> permission
                        }
                        Toast.makeText(this, "${status}抖音的${permissionName}权限成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "未找到抖音应用", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "执行失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "执行失败", e)
                }
            }, 1000)
            
        } catch (e: Exception) {
            Toast.makeText(this, "服务启动失败: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("MainActivity", "服务启动失败", e)
        }
    }

    private fun onAppClick(appInfo: ApplicationInfo) {
        val intent = Intent(this, AppDetailActivity::class.java).apply {
            putExtra(AppDetailActivity.EXTRA_PACKAGE_NAME, appInfo.packageName)
        }
        startActivity(intent)
    }
}



