package org.lsposed.lspatch.manager

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException

fun startLogcatToFile(context: Context) {
    try {
        val logDir = context.getExternalFilesDir("log") ?: throw IOException("External storage unavailable")
        val logFile = File(logDir, "lsp.log")
        if (!logFile.exists() && !logFile.createNewFile()) {
            throw IOException("Failed to create log file")
        }

        Runtime.getRuntime().exec("logcat -c")
        val command = "logcat -f ${logFile.absolutePath} -s LSPatch LSPosed LSPatch-MetaLoader LSPatch-SigBypass LSPosed-Bridge LSPlant LSPosedContext zygisk64 LSPlt Dobby LSPosedService"
        val process = Runtime.getRuntime().exec(command)
        Log.d("LSPatchLogcat", "Logcat redirected to file: ${logFile.absolutePath}")
    } catch (e: IOException) {
        Log.e("LSPatchLogcat", "Failed to redirect logcat to file", e)
        throw e
    }
}
