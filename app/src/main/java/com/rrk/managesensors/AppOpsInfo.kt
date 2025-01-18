package com.rrk.managesensors

data class AppOpsInfo(
    val name: String,           // AppOps 内部名称
    val description: String,    // 显示给用户看的描述
    var isEnabled: Boolean = false
)

object AppOpsPermissions {
    val permissions = listOf(
        // 位置相关
        AppOpsInfo("COARSE_LOCATION", "粗略位置"),
        AppOpsInfo("FINE_LOCATION", "精确位置"),
        AppOpsInfo("GPS", "GPS定位"),
        AppOpsInfo("MOCK_LOCATION", "模拟位置"),

        // 传感器相关
        AppOpsInfo("ACTIVITY_RECOGNITION", "活动识别"),
        AppOpsInfo("SENSORS", "传感器"),
        AppOpsInfo("BODY_SENSORS", "身体传感器"),
        AppOpsInfo("ACCELEROMETER", "加速度传感器"),
        AppOpsInfo("GYROSCOPE", "陀螺仪"),
        AppOpsInfo("MAGNETIC_SENSOR", "磁力计"),

        // 存储相关
        AppOpsInfo("READ_EXTERNAL_STORAGE", "读取存储"),
        AppOpsInfo("WRITE_EXTERNAL_STORAGE", "写入存储"),
        AppOpsInfo("READ_MEDIA_AUDIO", "读取音频"),
        AppOpsInfo("READ_MEDIA_VIDEO", "读取视频"),
        AppOpsInfo("READ_MEDIA_IMAGES", "读取图片"),

        // 个人信息相关
        AppOpsInfo("READ_CONTACTS", "读取通讯录"),
        AppOpsInfo("WRITE_CONTACTS", "修改通讯录"),
        AppOpsInfo("READ_CALL_LOG", "读取通话记录"),
        AppOpsInfo("WRITE_CALL_LOG", "写入通话记录"),
        AppOpsInfo("READ_CALENDAR", "读取日历"),
        AppOpsInfo("WRITE_CALENDAR", "修改日历"),
        AppOpsInfo("READ_SMS", "读取短信"),
        AppOpsInfo("WRITE_SMS", "发送短信"),
        AppOpsInfo("RECEIVE_SMS", "接收短信"),
        AppOpsInfo("READ_PHONE_STATE", "读取手机状态"),
        AppOpsInfo("CALL_PHONE", "拨打电话"),

        // 媒体相关
        AppOpsInfo("CAMERA", "相机"),
        AppOpsInfo("RECORD_AUDIO", "录音"),
        AppOpsInfo("RECORD_VIDEO", "录像"),
        AppOpsInfo("TAKE_AUDIO_FOCUS", "音频焦点"),
        AppOpsInfo("TAKE_MEDIA_BUTTONS", "媒体按键"),

        // 系统相关
        AppOpsInfo("RUN_IN_BACKGROUND", "后台运行"),
        AppOpsInfo("WAKE_LOCK", "保持唤醒"),
        AppOpsInfo("SYSTEM_ALERT_WINDOW", "显示悬浮窗"),
        AppOpsInfo("POST_NOTIFICATION", "发送通知"),
        AppOpsInfo("VIBRATE", "震动"),
        AppOpsInfo("READ_CLIPBOARD", "读取剪贴板"),
        AppOpsInfo("WRITE_CLIPBOARD", "写入剪贴板"),
        AppOpsInfo("BLUETOOTH_SCAN", "蓝牙扫描"),
        AppOpsInfo("BLUETOOTH_CONNECT", "蓝牙连接"),
        AppOpsInfo("BLUETOOTH_ADVERTISE", "蓝牙广播"),
        AppOpsInfo("WIFI_SCAN", "WiFi扫描"),

        // 网络相关
        AppOpsInfo("INTERNET", "网络访问"),
        AppOpsInfo("ACCESS_WIFI_STATE", "访问WiFi状态"),
        AppOpsInfo("CHANGE_WIFI_STATE", "修改WiFi状态"),
        AppOpsInfo("ACCESS_NETWORK_STATE", "访问网络状态"),
        AppOpsInfo("CHANGE_NETWORK_STATE", "修改网络状态")
    )

    // 按类别分组的权限
    val permissionGroups = mapOf(
        "位置" to permissions.slice(0..3),
        "传感器" to permissions.slice(4..9),
        "存储" to permissions.slice(10..14),
        "个人信息" to permissions.slice(15..25),
        "媒体" to permissions.slice(26..30),
        "系统" to permissions.slice(31..41),
        "网络" to permissions.slice(42..46)
    )
} 