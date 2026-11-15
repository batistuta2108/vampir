package com.example.ui.studio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.WebProject
import com.example.data.model.buildStandaloneHtmlDocument
import java.io.File

object HtmlExportHelper {

    fun copyFullHtmlToClipboard(context: Context, project: WebProject) {
        val fullHtml = buildStandaloneHtmlDocument(
            htmlCode = project.htmlCode,
            cssCode = project.cssCode,
            jsCode = project.jsCode,
            title = project.title
        )
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(project.title, fullHtml))
    }

    fun shareHtmlFile(context: Context, project: WebProject) {
        val fullHtml = buildStandaloneHtmlDocument(
            htmlCode = project.htmlCode,
            cssCode = project.cssCode,
            jsCode = project.jsCode,
            title = project.title
        )
        val safeSlug = project.title
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "webforge-site" }

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "$safeSlug-v${project.versionCount}.html")
        file.writeText(fullHtml, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${project.title} (WebForge Pro)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(shareIntent, "Exporter le site web complet (.html)")
        )
    }
}
