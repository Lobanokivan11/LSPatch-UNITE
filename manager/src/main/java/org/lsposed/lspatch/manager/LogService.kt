package org.lsposed.lspatch.manager

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException

fun startLogcatToFile(context: Context) {
    val logFile = File(Environment.getExternalStorageDirectory() + "/Android/media/org.lsposed.lspatch/log/lsp.log")
    try {
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        Runtime.getRuntime().exec("logcat -c")
        val command = "logcat -f ${logFile.absolutePath} -s LSPatch LSPosed LSPatch-MetaLoader LSPatch-SigBypass LSPosed-Bridge LSPlant LSPosedContext zygisk64 LSPlt Dobby LSPosedService"
        Runtime.getRuntime().exec(command)
        Log.d("LSPatchLogcat", "Logcat redirected to file: ${logFile.absolutePath}")
    } catch (e: IOException) {
        e.printStackTrace()
        Log.e("LSPatchLogcat", "Failed to redirect logcat to file", e)
    }
}
