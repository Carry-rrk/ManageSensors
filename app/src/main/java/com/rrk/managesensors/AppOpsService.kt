package com.rrk.managesensors

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import java.io.BufferedReader
import rikka.shizuku.Shizuku
import java.io.InputStreamReader
class AppOpsService : IAppOpsService.Stub() {
    companion object {
        private const val TAG = "AppOpsService"
    }

    override fun setAppOps(packageName: String, permission: String, enabled: Boolean) {
        Log.d(TAG, "setAppOps 开始执行: pkg=$packageName, perm=$permission, enabled=$enabled")
        
        if (enabled) {
            // 启用权限
            executeCommand(packageName, permission, "allow")
            if (!getAppOps(packageName, permission)) {
                throw RemoteException("启用权限失败")
            }
        } else {
            // 先尝试 ignore
            executeCommand(packageName, permission, "ignore")
            if (getAppOps(packageName, permission)) {
                // ignore 没有生效，尝试 deny
                Log.d(TAG, "ignore 模式未生效，尝试使用 deny")
                executeCommand(packageName, permission, "deny")
                if (getAppOps(packageName, permission)) {
                    throw RemoteException("禁用权限失败：ignore 和 deny 都无效")
                }
            }
        }
    }

    private fun executeCommand(packageName: String, permission: String, mode: String) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf(
                "appops",
                "set",
                packageName,
                permission,
                mode
            ))
            
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            if (exitCode != 0) {
                throw Exception("命令执行失败: $error")
            }
            
            Log.d(TAG, "命令执行完成: appops set $packageName $permission $mode")
        } catch (e: Exception) {
            Log.e(TAG, "执行命令失败", e)
            throw e
        }
    }

    override fun getAppOps(packageName: String, permission: String): Boolean {
        try {
            val process = Runtime.getRuntime().exec(arrayOf(
                "appops",
                "get",
                packageName,
                permission
            ))
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                throw Exception("命令执行失败: $output")
            }
            Log.d(TAG, "查询结果: $output")
            
            return when {
                output.contains("allow") -> true
                output.contains("deny") -> false
                output.contains("default") -> true
                output.contains("ignore") -> false
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "执行查询命令出错", e)
            throw RemoteException(e.message)
        }
    }

//    override fun onBind(intent: Intent?): IBinder? {
//        return this
//    }
} 