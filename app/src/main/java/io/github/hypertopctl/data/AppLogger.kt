package io.github.hypertopctl.data

import android.content.Context
import io.github.hypertopctl.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 应用进程诊断日志。hook 进程日志仍由 LSPosed 管理。 */
object AppLogger {
    data class Entry(
        val timestamp: String,
        val tag: String,
        val message: String,
    )

    private const val MAX_ENTRIES = 500
    private const val LOG_FILE_NAME = "app_log.txt"
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val lock = Any()
    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries.asStateFlow()

    private var appContext: Context? = null
    private var initialized = false

    fun initialize(context: Context) {
        synchronized(lock) {
            if (initialized) return
            appContext = context.applicationContext
            _entries.value = readEntries()
            initialized = true
        }
    }

    fun log(tag: String, message: String) {
        synchronized(lock) {
            val entry = Entry(timeFormat.format(Date()), tag, message)
            val updated = (_entries.value + entry).takeLast(MAX_ENTRIES)
            _entries.value = updated
            writeEntries(updated)
        }
    }

    fun clear() {
        synchronized(lock) {
            _entries.value = emptyList()
            appContext?.let { context ->
                runCatching { context.logFile().delete() }
            }
        }
    }

    fun exportText(): String = synchronized(lock) {
        buildString {
            appendLine("Hyper-TopCtl 应用诊断日志")
            appendLine("构建类型：${buildTypeLabel()}")
            appendLine("构建标识：${BuildConfig.BUILD_TYPE}")
            if (BuildConfig.DEBUG) {
                appendLine("提示：这是 Debug 版本，可用于提交 Issue。")
            } else {
                appendLine("提示：这是 Release 版本。提交 Issue 请使用 Debug 版本。")
            }
            appendLine("说明：以下内容仅包含应用侧日志；hook 侧日志请在 LSPosed 管理器中查看，标签为 HyperTopCtl。")
            appendLine()
            if (_entries.value.isEmpty()) {
                appendLine("暂无日志")
            } else {
                _entries.value.forEach { entry ->
                    appendLine("${entry.timestamp} [${entry.tag}] ${entry.message}")
                }
            }
        }
    }

    fun buildTypeLabel(): String = if (BuildConfig.DEBUG) "Debug" else "Release"

    private fun readEntries(): List<Entry> {
        val file = appContext?.logFile() ?: return emptyList()
        return runCatching {
            file.useLines { lines ->
                lines.mapNotNull(::parseLine).toList().takeLast(MAX_ENTRIES)
            }
        }.getOrDefault(emptyList())
    }

    private fun writeEntries(entries: List<Entry>) {
        val context = appContext ?: return
        runCatching {
            val file = context.logFile()
            file.parentFile?.mkdirs()
            file.writeText(
                entries.joinToString(separator = "\n") { entry ->
                    "${entry.timestamp} [${entry.tag}] ${entry.message}"
                },
                Charsets.UTF_8,
            )
        }
    }

    private fun parseLine(line: String): Entry? {
        if (line.length < 27) return null
        val tagStart = line.indexOf(" [", startIndex = 23)
        val tagEnd = line.indexOf("] ", startIndex = tagStart + 2)
        if (tagStart < 0 || tagEnd < 0) return null
        return Entry(
            timestamp = line.substring(0, 23),
            tag = line.substring(tagStart + 2, tagEnd),
            message = line.substring(tagEnd + 2),
        )
    }

    private fun Context.logFile() = File(filesDir, "logs/$LOG_FILE_NAME")
}
