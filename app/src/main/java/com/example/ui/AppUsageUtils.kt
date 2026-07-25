package com.example.ui

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import java.util.Calendar

object AppUsageUtils {
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getDailyAppUsage(context: Context): List<Pair<String, Long>> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        
        val packageManager = context.packageManager
        val appUsageMap = mutableMapOf<String, Long>()
        
        for (usageStats in usageStatsList) {
            if (usageStats.totalTimeInForeground > 0) {
                try {
                    val appInfo = packageManager.getApplicationInfo(usageStats.packageName, 0)
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    appUsageMap[appName] = appUsageMap.getOrDefault(appName, 0L) + usageStats.totalTimeInForeground
                } catch (e: PackageManager.NameNotFoundException) {
                    // Ignore
                }
            }
        }
        
        return appUsageMap.toList()
            .sortedByDescending { it.second }
            .take(4) // Top 4 apps
            .map { it.copy(second = it.second / 1000 / 60) } // Convert ms to minutes
    }
}
