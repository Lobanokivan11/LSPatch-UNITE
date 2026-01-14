package org.lsposed.lspatch.ui.page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.ramcosta.composedestinations.annotation.Destination
import org.lsposed.lspatch.ui.component.CenterTopBar
import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun LogsScreen(context: Context = LocalContext.current) {
    val file = File(Environment.getExternalStorageDirectory() + "/Android/media/org.lsposed.lspatch/log/lsp.log")
    var logEntries by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        logEntries = readLogFile(context, file)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Logs") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(logEntries) { log ->
                Text(
                    text = log,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}


fun readLogFile(context: Context, fileName: String): List<String> {
    val file = File(Environment.getExternalStorageDirectory() + "/Android/media/org.lsposed.lspatch/log/lsp.log")
    return if (file.exists()) {
        file.readLines()
    } else {
        emptyList()
    }
}
