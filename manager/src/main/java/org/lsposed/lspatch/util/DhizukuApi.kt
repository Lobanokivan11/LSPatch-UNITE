package org.lsposed.lspatch.util

import android.content.IntentSender
import android.content.pm.*
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.os.Process
import android.os.SystemProperties
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.rikka.tools.refine.Refine
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener

object DhizukuApi {

    private fun IBinder.wrap(): IBinder = this // Dhizuku does not need wrapping like Shizuku
    private fun IInterface.asBinder(): IBinder = this.asBinder()

    private val iPackageManager: IPackageManager by lazy {
        IPackageManager.Stub.asInterface(Dhizuku.getBinder()?.let { binder ->
            SystemServiceHelper.getSystemService("package", binder)
        })
    }

    private val iPackageInstaller: IPackageInstaller by lazy {
        IPackageInstaller.Stub.asInterface(iPackageManager.packageInstaller.asBinder())
    }

    private val packageInstaller: PackageInstaller by lazy {
        val userId = Process.myUserHandle().hashCode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Refine.unsafeCast(PackageInstallerHidden(iPackageInstaller, "com.android.shell", null, userId))
        } else {
            Refine.unsafeCast(PackageInstallerHidden(iPackageInstaller, "com.android.shell", userId))
        }
    }

    var isBinderAvailable by mutableStateOf(false)
    var isPermissionGranted by mutableStateOf(false)

    fun init() {
        Dhizuku.addBinderReceivedListener {
            isBinderAvailable = true
            checkPermission()
        }
        Dhizuku.addBinderDeadListener {
            isBinderAvailable = false
            isPermissionGranted = false
        }
        checkPermission()
    }

    private fun checkPermission() {
        isPermissionGranted = Dhizuku.isPermissionGranted()
    }

    fun requestPermission(listener: DhizukuRequestPermissionListener) {
        Dhizuku.requestPermission(listener)
    }

    fun createPackageInstallerSession(params: PackageInstaller.SessionParams): PackageInstaller.Session {
        val sessionId = packageInstaller.createSession(params)
        val iSession = IPackageInstallerSession.Stub.asInterface(iPackageInstaller.openSession(sessionId).asBinder())
        return Refine.unsafeCast(PackageInstallerHidden.SessionHidden(iSession))
    }

    fun isPackageInstalledWithoutPatch(packageName: String): Boolean {
        val userId = Process.myUserHandle().hashCode()
        val app = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            iPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA.toLong(), userId)
        } else {
            iPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA, userId)
        }
        return (app != null) && (app.metaData?.containsKey("lspatch") != true)
    }

    fun uninstallPackage(packageName: String, intentSender: IntentSender) {
        packageInstaller.uninstall(packageName, intentSender)
    }

    fun performDexOptMode(packageName: String): Boolean {
        return iPackageManager.performDexOptMode(
            packageName,
            SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false),
            "verify", true, true, null
        )
    }
}
