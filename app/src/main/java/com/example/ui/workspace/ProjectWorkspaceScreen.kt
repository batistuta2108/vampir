package com.example.ui.workspace

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TabletMac
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.CodeFileTab
import com.example.data.model.PreviewViewport
import com.example.data.model.ProjectVersion
import com.example.data.model.StudioMessage
import com.example.data.model.WebProject
import com.example.data.model.buildStandaloneHtmlDocument
import com.example.ui.studio.GenerationUiState
import com.example.ui.studio.HtmlExportHelper
import com.example.ui.studio.MobileExportBottomSheet
import com.example.ui.theme.CoralSpark
import com.example.ui.theme.CyanMint
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface

private enum class WorkspaceTab(val labelFr: String) {
    PREVIEW("Aperçu"),
    CODE("Code"),
    CHAT("IA Chat"),
    HISTORY("Versions")
}

private val QuickRefinements = listOf(
    "Ajoute une section tarifs complète",
    "Passe en mode sombre premium",
    "Ajoute une FAQ accordéon interactive",
    "Ajoute des animations fluides au survol",
    "Améliore le formulaire de contact"
)

@Composable
fun ProjectWorkspaceScreen(
    project: WebProject?,
    messages: List<StudioMessage>,
    versions: List<ProjectVersion>,
    generationState: GenerationUiState,
    onBack: () -> Unit,
    onRefineWithAi: (WebProject, String) -> Unit,
    onSaveManualCode: (WebProject, String, String, String) -> Unit,
    onRestoreVersion: (WebProject, ProjectVersion) -> Unit,
    onRenameProject: (WebProject, String) -> Unit,
    onClearGenerationState: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var activeTab by rememberSaveable { mutableStateOf(WorkspaceTab.PREVIEW) }
    var viewport by rememberSaveable { mutableStateOf(PreviewViewport.MOBILE) }
    var reloadSignal by remember { mutableIntStateOf(0) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showConsoleDialog by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    val consoleLogs = remember { mutableStateListOf<String>() }

    if (project == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = ElectricIndigo)
                Text(
                    text = "Chargement du studio...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onBack) {
                    Text("Retour aux projets")
                }
            }
        }
        return
    }

    val isGenerating = generationState is GenerationUiState.Generating

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("workspace_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showRenameDialog = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = project.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Renommer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CyanMint.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "v${project.versionCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanMint,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = "Sync cloud actif",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                HtmlExportHelper.copyFullHtmlToClipboard(context, project)
                                Toast.makeText(context, "Code HTML complet copié !", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("copy_full_html_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copier le HTML complet",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showExportSheet = true },
                            modifier = Modifier.testTag("export_html_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = "Télécharger APK et exporter",
                                tint = CyanMint
                            )
                        }
                    }
                }

                TabRow(
                    selectedTabIndex = activeTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = ElectricIndigo
                ) {
                    WorkspaceTab.entries.forEach { tab ->
                        val icon = when (tab) {
                            WorkspaceTab.PREVIEW -> Icons.Filled.Visibility
                            WorkspaceTab.CODE -> Icons.Filled.Code
                            WorkspaceTab.CHAT -> Icons.Filled.AutoAwesome
                            WorkspaceTab.HISTORY -> Icons.Filled.History
                        }
                        Tab(
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                            modifier = Modifier.testTag("workspace_tab_${tab.name.lowercase()}"),
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = tab.labelFr,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                }
                            }
                        )
                    }
                }

                AnimatedVisibility(visible = generationState !is GenerationUiState.Idle) {
                    when (generationState) {
                        is GenerationUiState.Generating -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ElectricIndigo.copy(alpha = 0.12f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = CyanMint
                                )
                                Text(
                                    text = generationState.statusLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyanMint
                                )
                            }
                        }
                        is GenerationUiState.Success -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyanMint.copy(alpha = 0.14f))
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = generationState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyanMint,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = onClearGenerationState) {
                                    Text("Fermer", color = CyanMint)
                                }
                            }
                        }
                        is GenerationUiState.Error -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = generationState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = onClearGenerationState) {
                                    Text("OK")
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
        ) {
            when (activeTab) {
                WorkspaceTab.PREVIEW -> LivePreviewPane(
                    project = project,
                    viewport = viewport,
                    reloadSignal = reloadSignal,
                    consoleCount = consoleLogs.size,
                    isGenerating = isGenerating,
                    onViewportChange = { viewport = it },
                    onReload = { reloadSignal++ },
                    onOpenConsole = { showConsoleDialog = true },
                    onConsoleMessage = { msg ->
                        if (consoleLogs.size > 40) consoleLogs.removeAt(0)
                        consoleLogs.add(msg)
                    },
                    onQuickRefine = { prompt -> onRefineWithAi(project, prompt) }
                )

                WorkspaceTab.CODE -> MultiFileCodeEditorPane(
                    project = project,
                    isSaving = isGenerating,
                    onSaveCode = { html, css, js ->
                        onSaveManualCode(project, html, css, js)
                    }
                )

                WorkspaceTab.CHAT -> StudioChatPane(
                    messages = messages,
                    isGenerating = isGenerating,
                    onSendPrompt = { prompt -> onRefineWithAi(project, prompt) }
                )

                WorkspaceTab.HISTORY -> VersionHistoryPane(
                    currentVersion = project.versionCount,
                    versions = versions,
                    isBusy = isGenerating,
                    onRestore = { ver ->
                        onRestoreVersion(project, ver)
                        activeTab = WorkspaceTab.PREVIEW
                    }
                )
            }
        }
    }

    if (showRenameDialog) {
        var newTitle by remember(project.title) { mutableStateOf(project.title) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Renommer le projet") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    label = { Text("Nom du site") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRenameProject(project, newTitle)
                        showRenameDialog = false
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showExportSheet) {
        MobileExportBottomSheet(
            project = project,
            onDismiss = { showExportSheet = false }
        )
    }

    if (showConsoleDialog) {
        AlertDialog(
            onDismissRequest = { showConsoleDialog = false },
            title = { Text("Console WebView JS") },
            text = {
                if (consoleLogs.isEmpty()) {
                    Text("Aucun message ou erreur détecté dans la console.")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        consoleLogs.forEach { logLine ->
                            Text(
                                text = logLine,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showConsoleDialog = false }) {
                    Text("Fermer")
                }
            },
            dismissButton = {
                TextButton(onClick = { consoleLogs.clear() }) {
                    Text("Vider")
                }
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LivePreviewPane(
    project: WebProject,
    viewport: PreviewViewport,
    reloadSignal: Int,
    consoleCount: Int,
    isGenerating: Boolean,
    onViewportChange: (PreviewViewport) -> Unit,
    onReload: () -> Unit,
    onOpenConsole: () -> Unit,
    onConsoleMessage: (String) -> Unit,
    onQuickRefine: (String) -> Unit
) {
    var quickPrompt by rememberSaveable { mutableStateOf("") }

    val compiledHtml = remember(project.htmlCode, project.cssCode, project.jsCode, project.title, viewport) {
        val rawDoc = buildStandaloneHtmlDocument(
            htmlCode = project.htmlCode,
            cssCode = project.cssCode,
            jsCode = project.jsCode,
            title = project.title
        )
        // Adjust viewport meta tag when simulating Tablet or Desktop widths
        val targetViewport = when (viewport) {
            PreviewViewport.MOBILE -> "width=device-width, initial-scale=1.0"
            PreviewViewport.TABLET -> "width=768"
            PreviewViewport.DESKTOP -> "width=1200"
        }
        rawDoc.replace(
            Regex("<meta[^>]*name=[\"']viewport[\"'][^>]*>", RegexOption.IGNORE_CASE),
            "<meta name=\"viewport\" content=\"$targetViewport\" />"
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Viewport & Debug Toolbar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PreviewViewport.entries.forEach { vp ->
                        val icon = when (vp) {
                            PreviewViewport.MOBILE -> Icons.Filled.PhoneAndroid
                            PreviewViewport.TABLET -> Icons.Filled.TabletMac
                            PreviewViewport.DESKTOP -> Icons.Filled.DesktopWindows
                        }
                        FilterChip(
                            selected = viewport == vp,
                            onClick = { onViewportChange(vp) },
                            label = { Text(vp.labelFr, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricIndigo.copy(alpha = 0.24f)
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenConsole) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Terminal,
                                contentDescription = "Console JS",
                                tint = if (consoleCount > 0) CoralSpark else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(onClick = onReload) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Recharger l'aperçu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Responsive WebView Frame
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF07090E))
                .padding(if (viewport == PreviewViewport.DESKTOP) 0.dp else 10.dp),
            contentAlignment = Alignment.Center
        ) {
            val frameWidthModifier = when (viewport) {
                PreviewViewport.MOBILE -> Modifier.widthIn(max = 390.dp).fillMaxWidth()
                PreviewViewport.TABLET -> Modifier.widthIn(max = 768.dp).fillMaxWidth()
                PreviewViewport.DESKTOP -> Modifier.fillMaxWidth()
            }

            Surface(
                shape = if (viewport == PreviewViewport.DESKTOP) RoundedCornerShape(0.dp) else RoundedCornerShape(18.dp),
                modifier = frameWidthModifier
                    .fillMaxHeight()
                    .border(
                        width = if (viewport == PreviewViewport.DESKTOP) 0.dp else 1.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = if (viewport == PreviewViewport.DESKTOP) RoundedCornerShape(0.dp) else RoundedCornerShape(18.dp)
                    )
            ) {
                var lastRenderedKey by remember { mutableStateOf("") }
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("preview_webview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    // Keep anchor and local navigation inside preview
                                    val url = request?.url?.toString().orEmpty()
                                    return url.startsWith("http://") || url.startsWith("https://")
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                    if (consoleMessage != null) {
                                        onConsoleMessage("[${consoleMessage.messageLevel()}] ${consoleMessage.message()}")
                                    }
                                    return true
                                }
                            }
                        }
                    },
                    update = { webView ->
                        webView.settings.useWideViewPort = viewport != PreviewViewport.MOBILE
                        webView.settings.loadWithOverviewMode = viewport != PreviewViewport.MOBILE
                        val renderKey = "${compiledHtml.hashCode()}_${viewport.name}_$reloadSignal"
                        if (lastRenderedKey != renderKey) {
                            lastRenderedKey = renderKey
                            webView.loadDataWithBaseURL(
                                "https://webforge.local/",
                                compiledHtml,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    }
                )
            }
        }

        // Quick AI Refinement Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quickPrompt,
                    onValueChange = { quickPrompt = it },
                    placeholder = { Text("Modifier le site avec l'IA (ex: Ajoute une FAQ)...") },
                    singleLine = true,
                    enabled = !isGenerating,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("preview_quick_prompt_input")
                )
                Button(
                    onClick = {
                        val p = quickPrompt
                        quickPrompt = ""
                        onQuickRefine(p)
                    },
                    enabled = quickPrompt.isNotBlank() && !isGenerating,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("preview_quick_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Appliquer avec l'IA",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MultiFileCodeEditorPane(
    project: WebProject,
    isSaving: Boolean,
    onSaveCode: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    var selectedFile by rememberSaveable { mutableStateOf(CodeFileTab.HTML) }
    var htmlText by remember(project.id, project.versionCount) { mutableStateOf(project.htmlCode) }
    var cssText by remember(project.id, project.versionCount) { mutableStateOf(project.cssCode) }
    var jsText by remember(project.id, project.versionCount) { mutableStateOf(project.jsCode) }

    val hasChanges = htmlText != project.htmlCode || cssText != project.cssCode || jsText != project.jsCode

    val activeText = when (selectedFile) {
        CodeFileTab.HTML -> htmlText
        CodeFileTab.CSS -> cssText
        CodeFileTab.JS -> jsText
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // File tabs + Save Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CodeFileTab.entries.forEach { fileTab ->
                        FilterChip(
                            selected = selectedFile == fileTab,
                            onClick = { selectedFile = fileTab },
                            label = {
                                Text(
                                    text = fileTab.fileName,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText(selectedFile.fileName, activeText))
                            Toast.makeText(context, "${selectedFile.fileName} copié", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copier ce fichier",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick = { onSaveCode(htmlText, cssText, jsText) },
                        enabled = hasChanges && !isSaving,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanMint,
                            contentColor = ObsidianBg
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("save_code_button")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sauver", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Code Surface
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(ObsidianBg)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            BasicTextField(
                value = activeText,
                onValueChange = { updated ->
                    when (selectedFile) {
                        CodeFileTab.HTML -> htmlText = updated
                        CodeFileTab.CSS -> cssText = updated
                        CodeFileTab.JS -> jsText = updated
                    }
                },
                textStyle = TextStyle(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFE2E8F8)
                ),
                cursorBrush = SolidColor(CyanMint),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(min = 340.dp)
                    .testTag("code_editor_field")
            )
        }
    }
}

@Composable
private fun StudioChatPane(
    messages: List<StudioMessage>,
    isGenerating: Boolean,
    onSendPrompt: (String) -> Unit
) {
    var chatInput by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Assistant Studio WebForge Pro",
                                style = MaterialTheme.typography.titleMedium,
                                color = ElectricIndigo
                            )
                            Text(
                                text = "Demandez n'importe quelle amélioration : nouvelles sections, changement de palette, animations JavaScript, tableaux interactifs ou formulaires.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isUser) {
                                ElectricIndigo.copy(alpha = 0.22f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            modifier = Modifier
                                .widthIn(max = 520.dp)
                                .border(
                                    1.dp,
                                    if (isUser) ElectricIndigo.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(18.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isUser) CoralSpark else CyanMint)
                                    )
                                    Text(
                                        text = if (isUser) "Vous" else "WebForge IA • v${msg.versionTag}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isUser) CoralSpark else CyanMint,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = msg.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Enhancement Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickRefinements.forEach { suggestion ->
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(enabled = !isGenerating) {
                            chatInput = suggestion
                        }
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Chat Input Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Décrivez une amélioration...") },
                    maxLines = 4,
                    enabled = !isGenerating,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_prompt_input")
                )
                Button(
                    onClick = {
                        val p = chatInput
                        chatInput = ""
                        onSendPrompt(p)
                    },
                    enabled = chatInput.isNotBlank() && !isGenerating,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Envoyer",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionHistoryPane(
    currentVersion: Long,
    versions: List<ProjectVersion>,
    isBusy: Boolean,
    onRestore: (ProjectVersion) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (versions.isEmpty()) {
            item {
                Text(
                    text = "Aucune version archivée pour le moment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(versions, key = { it.id }) { ver ->
                val isCurrent = ver.versionNumber == currentVersion
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isCurrent) CyanMint.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCurrent) CyanMint.copy(alpha = 0.18f) else ElectricIndigo.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "v${ver.versionNumber}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isCurrent) CyanMint else ElectricIndigo,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                if (isCurrent) {
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanMint
                                    )
                                }
                            }
                            Text(
                                text = ver.promptLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "HTML ${ver.htmlCode.length} car. • CSS ${ver.cssCode.length} car. • JS ${ver.jsCode.length} car.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!isCurrent) {
                            TextButton(
                                onClick = { onRestore(ver) },
                                enabled = !isBusy
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Restore,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Restaurer")
                            }
                        }
                    }
                }
            }
        }
    }
}
