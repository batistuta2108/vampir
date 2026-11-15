package com.example.ui.studio

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.data.model.WebProject
import com.example.data.model.buildStandaloneHtmlDocument
import org.json.JSONObject
import java.io.File
import java.io.OutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object MobileExportHelper {

    fun getInstalledApkSizeLabel(context: Context): String {
        val apkFile = runCatching { File(context.applicationInfo.sourceDir) }.getOrNull()
        val bytes = apkFile?.takeIf { it.exists() }?.length() ?: 0L
        return if (bytes > 0L) {
            String.format(Locale.FRANCE, "%.1f Mo", bytes / (1024.0 * 1024.0))
        } else {
            "APK Android"
        }
    }

    fun defaultApkFileName(): String = "WebForge-Pro-v${BuildConfig.VERSION_NAME}.apk"

    fun slugForProject(project: WebProject): String =
        project.title
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "webforge-site" }

    fun writeInstalledApkToUri(context: Context, destinationUri: Uri): Result<Long> = runCatching {
        val sourceFile = File(context.applicationInfo.sourceDir)
        require(sourceFile.exists() && sourceFile.canRead()) {
            "Fichier APK source inaccessible sur cet appareil."
        }
        val bytesCopied = context.contentResolver.openOutputStream(destinationUri)?.use { out ->
            sourceFile.inputStream().use { input ->
                input.copyTo(out)
            }
        } ?: error("Impossible d'ouvrir le fichier de destination.")
        bytesCopied
    }

    fun shareInstalledApk(context: Context): Result<Unit> = runCatching {
        val sourceFile = File(context.applicationInfo.sourceDir)
        require(sourceFile.exists() && sourceFile.canRead()) {
            "Fichier APK source inaccessible sur cet appareil."
        }
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val targetApk = File(exportDir, defaultApkFileName())
        sourceFile.inputStream().use { input ->
            targetApk.outputStream().use { out -> input.copyTo(out) }
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            targetApk
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "WebForge Pro - Application Mobile Android (.apk)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Partager l'APK WebForge Pro"))
    }

    fun writeStandaloneHtmlToUri(
        context: Context,
        project: WebProject,
        destinationUri: Uri
    ): Result<Unit> = runCatching {
        val fullHtml = buildStandaloneHtmlDocument(
            htmlCode = project.htmlCode,
            cssCode = project.cssCode,
            jsCode = project.jsCode,
            title = project.title
        )
        context.contentResolver.openOutputStream(destinationUri)?.use { out ->
            out.write(fullHtml.toByteArray(Charsets.UTF_8))
        } ?: error("Impossible d'écrire le fichier HTML.")
    }

    fun writeMobilePackZipToUri(
        context: Context,
        project: WebProject,
        destinationUri: Uri
    ): Result<Unit> = runCatching {
        context.contentResolver.openOutputStream(destinationUri)?.use { out ->
            writeMobilePackZipStream(project, out)
        } ?: error("Impossible d'écrire l'archive ZIP.")
    }

    fun shareMobilePackZip(context: Context, project: WebProject): Result<Unit> = runCatching {
        val slug = slugForProject(project)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val zipFile = File(exportDir, "$slug-mobile-pack.zip")
        zipFile.outputStream().use { out ->
            writeMobilePackZipStream(project, out)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            zipFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${project.title} - Pack Mobile PWA & Android")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Exporter le Pack Mobile (.zip)"))
    }

    private fun writeMobilePackZipStream(project: WebProject, outputStream: OutputStream) {
        val standaloneHtml = buildStandaloneHtmlDocument(
            htmlCode = project.htmlCode,
            cssCode = project.cssCode,
            jsCode = project.jsCode,
            title = project.title
        )
        val manifestJson = JSONObject().apply {
            put("name", project.title)
            put("short_name", project.title.take(12))
            put("description", project.description.ifBlank { "Application Web Mobile créée avec WebForge Pro" })
            put("start_url", "./index.html")
            put("display", "standalone")
            put("background_color", "#0B0E14")
            put("theme_color", "#7C5CFF")
        }.toString(2)

        val pwaIndexHtml = buildStandaloneHtmlDocument(
            htmlCode = project.htmlCode,
            cssCode = project.cssCode,
            jsCode = project.jsCode,
            title = project.title
        ).replace(
            "</head>",
            "  <link rel=\"manifest\" href=\"manifest.json\" />\n  <meta name=\"theme-color\" content=\"#7C5CFF\" />\n</head>",
            ignoreCase = true
        )

        val wrapperMainActivity = """
            package com.webforge.generated

            import android.annotation.SuppressLint
            import android.os.Bundle
            import android.webkit.WebView
            import android.webkit.WebViewClient
            import androidx.activity.ComponentActivity

            class MainActivity : ComponentActivity() {
                @SuppressLint("SetJavaScriptEnabled")
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    val webView = WebView(this).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = WebViewClient()
                        loadUrl("file:///android_asset/index.html")
                    }
                    setContentView(webView)
                }
            }
        """.trimIndent()

        val wrapperManifest = """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <uses-permission android:name="android.permission.INTERNET" />
                <application
                    android:allowBackup="true"
                    android:label="${project.title.replace("\"", "")}">
                    <activity
                        android:name=".MainActivity"
                        android:exported="true">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
        """.trimIndent()

        val readme = """
            WebForge Pro - Pack Mobile Export (${project.title} v${project.versionCount})
            =========================================================================
            1. Version PWA Mobile (web/) :
               Hébergez le dossier `web/` sur n'importe quel serveur HTTPS (Firebase Hosting, Netlify, Vercel)
               et ouvrez-le sur Android Chrome pour l'installer sur l'écran d'accueil en mode application.
            2. Wrapper Android APK (android-app/) :
               Copiez `app/src/main/assets/index.html`, `MainActivity.kt` et `AndroidManifest.xml` dans un projet Android Studio
               puis exécutez Build > Build APK(s) pour générer l'APK autonome de votre site.
        """.trimIndent()

        ZipOutputStream(outputStream).use { zip ->
            fun addEntry(path: String, content: String) {
                zip.putNextEntry(ZipEntry(path))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            addEntry("web/index.html", pwaIndexHtml)
            addEntry("web/styles.css", project.cssCode)
            addEntry("web/app.js", project.jsCode)
            addEntry("web/manifest.json", manifestJson)
            addEntry("android-app/app/src/main/assets/index.html", standaloneHtml)
            addEntry("android-app/app/src/main/java/com/webforge/generated/MainActivity.kt", wrapperMainActivity)
            addEntry("android-app/app/src/main/AndroidManifest.xml", wrapperManifest)
            addEntry("README.txt", readme)
        }
    }
}
