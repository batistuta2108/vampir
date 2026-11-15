package com.example.ui.studio

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.WebProject
import com.example.ui.theme.CoralSpark
import com.example.ui.theme.CyanMint
import com.example.ui.theme.ElectricIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileExportBottomSheet(
    project: WebProject? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val apkSizeLabel = remember(context) { MobileExportHelper.getInstalledApkSizeLabel(context) }

    val saveApkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { uri ->
        if (uri != null) {
            MobileExportHelper.writeInstalledApkToUri(context, uri)
                .onSuccess { bytes ->
                    val mb = String.format(java.util.Locale.FRANCE, "%.1f Mo", bytes / (1024.0 * 1024.0))
                    Toast.makeText(context, "APK enregistré ($mb) dans vos téléchargements !", Toast.LENGTH_LONG).show()
                }
                .onFailure { err ->
                    Toast.makeText(context, err.localizedMessage ?: "Échec de l'enregistrement APK.", Toast.LENGTH_LONG).show()
                }
        }
    }

    val saveHtmlLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        if (uri != null && project != null) {
            MobileExportHelper.writeStandaloneHtmlToUri(context, project, uri)
                .onSuccess {
                    Toast.makeText(context, "Fichier HTML sauvegardé !", Toast.LENGTH_SHORT).show()
                }
                .onFailure { err ->
                    Toast.makeText(context, err.localizedMessage ?: "Échec de sauvegarde HTML.", Toast.LENGTH_LONG).show()
                }
        }
    }

    val saveZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null && project != null) {
            MobileExportHelper.writeMobilePackZipToUri(context, project, uri)
                .onSuccess {
                    Toast.makeText(context, "Pack Mobile PWA + Android (.zip) enregistré !", Toast.LENGTH_LONG).show()
                }
                .onFailure { err ->
                    Toast.makeText(context, err.localizedMessage ?: "Échec de sauvegarde ZIP.", Toast.LENGTH_LONG).show()
                }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Télécharger & Exporter (Mobile APK)",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Enregistrez l'application mobile WebForge Pro (.apk) directement sur votre téléphone ou exportez votre site en pack mobile.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            // 1. Download WebForge Pro APK to Device Downloads
            ExportActionCard(
                icon = Icons.Filled.Android,
                accent = CyanMint,
                title = "Télécharger l'APK WebForge Pro ($apkSizeLabel)",
                subtitle = "Enregistre le fichier .apk Android sur votre appareil (Téléchargements)",
                trailingIcon = Icons.Filled.Download,
                testTag = "download_apk_button",
                onClick = {
                    saveApkLauncher.launch(MobileExportHelper.defaultApkFileName())
                }
            )

            // 2. Share APK via Android Sharesheet
            ExportActionCard(
                icon = Icons.Filled.Share,
                accent = ElectricIndigo,
                title = "Partager l'APK mobile (.apk)",
                subtitle = "Envoyer le package Android via Drive, Telegram, Email ou Bluetooth",
                trailingIcon = Icons.Filled.Share,
                testTag = "share_apk_button",
                onClick = {
                    MobileExportHelper.shareInstalledApk(context)
                        .onFailure { err ->
                            Toast.makeText(context, err.localizedMessage ?: "Partage APK impossible.", Toast.LENGTH_LONG).show()
                        }
                }
            )

            if (project != null) {
                val slug = remember(project.id, project.title) {
                    MobileExportHelper.slugForProject(project)
                }

                // 3. Export Project Mobile Pack ZIP (PWA + Android WebView Wrapper)
                ExportActionCard(
                    icon = Icons.Filled.FolderZip,
                    accent = CoralSpark,
                    title = "Télécharger le Pack Mobile du site (.zip)",
                    subtitle = "Contient la version PWA installable + projet Android WebView prêt à compiler",
                    trailingIcon = Icons.Filled.Download,
                    testTag = "download_mobile_pack_zip_button",
                    onClick = {
                        saveZipLauncher.launch("$slug-mobile-pack.zip")
                    }
                )

                // 4. Download standalone HTML
                ExportActionCard(
                    icon = Icons.Filled.Code,
                    accent = ElectricIndigo,
                    title = "Télécharger le site complet (.html)",
                    subtitle = "Fichier HTML/CSS/JS autonome prêt à héberger",
                    trailingIcon = Icons.Filled.Download,
                    testTag = "download_html_button",
                    onClick = {
                        saveHtmlLauncher.launch("$slug-v${project.versionCount}.html")
                    }
                )
            }
        }
    }
}

@Composable
private fun ExportActionCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String,
    trailingIcon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
