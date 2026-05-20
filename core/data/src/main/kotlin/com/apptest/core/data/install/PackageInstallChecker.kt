package com.apptest.core.data.install

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Checks whether an app (identified by [packageName]) is currently installed on the device.
 *
 * Uses [PackageManager.getPackageInfo] per APT-A-003 default (PackageManager V1).
 * Play Integrity API (V2) is deferred to post-launch per the same decision.
 *
 * Called by:
 * - [com.apptest.core.data.worker.SupabaseHeartbeatWorker] — nightly install-presence check
 * - `:feature:testing` UI — surface AtRisk warning when the app under test is not found
 */
@Singleton
class PackageInstallChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Returns `true` if [packageName] is installed and not just archived.
     * Handles API-level differences: uses flags-0 check on API ≥ 33 because
     * `MATCH_UNINSTALLED_PACKAGES` would include archived apps that aren't truly installed.
     */
    fun isInstalled(packageName: String): Boolean = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(packageName, 0)
        }
        true
    }.getOrDefault(false)
}
