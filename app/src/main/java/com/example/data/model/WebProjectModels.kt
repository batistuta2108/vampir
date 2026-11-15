package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class WebProject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "custom",
    val htmlCode: String = "",
    val cssCode: String = "",
    val jsCode: String = "",
    val versionCount: Long = 1L,
    val isFavorite: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StudioMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val projectId: String = "",
    val role: String = "user",
    val content: String = "",
    val summary: String = "",
    val versionTag: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ProjectVersion(
    @DocumentId val id: String = "",
    val userId: String = "",
    val projectId: String = "",
    val versionNumber: Long = 1L,
    val promptLabel: String = "",
    val htmlCode: String = "",
    val cssCode: String = "",
    val jsCode: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

enum class ProjectCategory(val id: String, val labelFr: String) {
    SAAS("saas", "SaaS & IA"),
    ECOMMERCE("ecommerce", "E-Commerce"),
    LANDING("landing", "Landing Pro"),
    PORTFOLIO("portfolio", "Portfolio"),
    DASHBOARD("dashboard", "Dashboard"),
    RESTAURANT("restaurant", "Restaurant"),
    CUSTOM("custom", "Sur mesure");

    companion object {
        fun fromId(id: String): ProjectCategory =
            entries.firstOrNull { it.id == id } ?: CUSTOM
    }
}

enum class PreviewViewport(val labelFr: String, val widthDp: Int?) {
    MOBILE("Mobile", 390),
    TABLET("Tablette", 768),
    DESKTOP("Bureau", null)
}

enum class CodeFileTab(val fileName: String) {
    HTML("index.html"),
    CSS("styles.css"),
    JS("app.js")
}

data class GeneratedWebsiteBundle(
    val title: String,
    val description: String,
    val category: String,
    val htmlCode: String,
    val cssCode: String,
    val jsCode: String,
    val summary: String
)

fun buildStandaloneHtmlDocument(htmlCode: String, cssCode: String, jsCode: String, title: String = "WebForge Pro Site"): String {
    val trimmedHtml = htmlCode.trim()
    val hasHtmlTag = trimmedHtml.contains("<html", ignoreCase = true)
    return if (hasHtmlTag) {
        var doc = trimmedHtml
        if (cssCode.isNotBlank()) {
            val styleBlock = "<style>\n$cssCode\n</style>"
            doc = if (doc.contains("</head>", ignoreCase = true)) {
                doc.replace("</head>", "$styleBlock\n</head>", ignoreCase = true)
            } else {
                "$styleBlock\n$doc"
            }
        }
        if (jsCode.isNotBlank()) {
            val scriptBlock = "<script>\n$jsCode\n</script>"
            doc = if (doc.contains("</body>", ignoreCase = true)) {
                doc.replace("</body>", "$scriptBlock\n</body>", ignoreCase = true)
            } else {
                "$doc\n$scriptBlock"
            }
        }
        doc
    } else {
        """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>${title.replace("<", "&lt;").replace(">", "&gt;")}</title>
          <style>
          $cssCode
          </style>
        </head>
        <body>
          $trimmedHtml
          <script>
          $jsCode
          </script>
        </body>
        </html>
        """.trimIndent()
    }
}
