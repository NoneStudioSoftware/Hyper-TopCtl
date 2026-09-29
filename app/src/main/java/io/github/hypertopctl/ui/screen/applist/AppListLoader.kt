package io.github.hypertopctl.ui.screen.applist

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import io.github.hypertopctl.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Sort orders for the per-app list, mirroring ReSukiSU's SuperUser sorting options. */
enum class AppSortType(val displayNameRes: Int) {
    NAME(R.string.applist_sort_name),
    INSTALL_TIME(R.string.applist_sort_install_time),
    UPDATE_TIME(R.string.applist_sort_update_time),
}

/**
 * A single installed app shown in the per-app list. [icon] is loaded eagerly with the entry and
 * rendered via [io.github.hypertopctl.ui.util.DrawablePainter].
 */
data class AppEntry(
    val label: String,
    val packageName: String,
    val isSystem: Boolean,
    val icon: Drawable?,
    val firstInstallTime: Long = 0L,
    val lastUpdateTime: Long = 0L,
)

/**
 * Loads launcher-visible apps, optionally merged with every installed (system) app when
 * [includeSystem] is on. Sorted by localized label; package name breaks ties. List sorting by
 * [AppSortType] happens in the UI layer.
 */
suspend fun loadAppEntries(context: Context, includeSystem: Boolean): List<AppEntry> =
    withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val entries = LinkedHashMap<String, AppEntry>()

        pm.queryIntentActivities(launcherIntent, 0).forEach { info ->
            val appInfo = info.activityInfo?.applicationInfo ?: return@forEach
            val pkg = runCatching {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(appInfo.packageName, 0)
            }.getOrNull()
            entries[appInfo.packageName] = appInfo.toAppEntry(
                pm = pm,
                firstInstallTime = pkg?.firstInstallTime ?: 0L,
                lastUpdateTime = pkg?.lastUpdateTime ?: 0L,
            )
        }

        if (includeSystem) {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(PackageManager.GET_META_DATA).forEach { pkg ->
                val appInfo = pkg.applicationInfo ?: return@forEach
                if (!entries.containsKey(appInfo.packageName)) {
                    entries[appInfo.packageName] = appInfo.toAppEntry(
                        pm = pm,
                        firstInstallTime = pkg.firstInstallTime,
                        lastUpdateTime = pkg.lastUpdateTime,
                    )
                }
            }
        }

        entries.values.sortedWith(
            compareBy<AppEntry> { it.label.lowercase() }.thenBy { it.packageName },
        )
    }

private fun ApplicationInfo.toAppEntry(
    pm: PackageManager,
    firstInstallTime: Long,
    lastUpdateTime: Long,
): AppEntry = AppEntry(
    label = runCatching { loadLabel(pm).toString() }.getOrDefault(packageName),
    packageName = packageName,
    isSystem = (flags and ApplicationInfo.FLAG_SYSTEM) != 0,
    icon = runCatching { loadIcon(pm) }.getOrNull(),
    firstInstallTime = firstInstallTime,
    lastUpdateTime = lastUpdateTime,
)
