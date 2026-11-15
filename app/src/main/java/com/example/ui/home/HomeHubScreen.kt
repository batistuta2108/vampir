package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Web
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ProjectCategory
import com.example.data.model.WebProject
import com.example.data.templates.WebTemplate
import com.example.data.templates.WebTemplates
import com.example.ui.studio.GenerationUiState
import com.example.ui.studio.MobileExportBottomSheet
import com.example.ui.studio.ProjectsUiState
import com.example.ui.theme.CoralSpark
import com.example.ui.theme.CyanMint
import com.example.ui.theme.ElectricIndigo

private val QuickPromptIdeas = listOf(
    "SaaS IA de facturation avec calculateur de prix",
    "Boutique e-commerce de montres minimalistes avec panier",
    "Portfolio d'agence créative avec galerie interactive",
    "Dashboard crypto temps réel avec graphiques et tableau",
    "Site restaurant gastronomique avec menu et réservation"
)

@Composable
fun HomeHubScreen(
    userDisplayName: String,
    projectsState: ProjectsUiState,
    generationState: GenerationUiState,
    useFastMode: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleFastMode: (Boolean) -> Unit,
    onGenerateFromPrompt: (String) -> Unit,
    onSelectTemplate: (WebTemplate) -> Unit,
    onOpenProject: (WebProject) -> Unit,
    onToggleFavorite: (WebProject) -> Unit,
    onDeleteProject: (WebProject) -> Unit,
    onClearGenerationState: () -> Unit,
    onSignOut: () -> Unit
) {
    var promptText by rememberSaveable { mutableStateOf("") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var projectToDelete by remember { mutableStateOf<WebProject?>(null) }
    var showMobileApkSheet by remember { mutableStateOf(false) }

    val isGenerating = generationState is GenerationUiState.Generating

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(ElectricIndigo, CoralSpark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "WebForge Pro",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = CyanMint.copy(alpha = 0.16f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AllInclusive,
                                            contentDescription = null,
                                            tint = CyanMint,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "ILLIMITÉ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyanMint,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Bonjour, $userDisplayName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showMobileApkSheet = true },
                            modifier = Modifier.testTag("home_download_apk_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = "Télécharger la version mobile APK",
                                tint = CyanMint
                            )
                        }
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier.testTag("toggle_theme_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Basculer le thème clair ou sombre",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Se déconnecter",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .align(Alignment.TopCenter),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // 1. AI Prompt Composer Hero
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(
                                        ElectricIndigo.copy(alpha = 0.7f),
                                        CoralSpark.copy(alpha = 0.55f),
                                        CyanMint.copy(alpha = 0.5f)
                                    )
                                ),
                                RoundedCornerShape(24.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Que voulez-vous construire ?",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            OutlinedTextField(
                                value = promptText,
                                onValueChange = { promptText = it },
                                placeholder = {
                                    Text("Ex: Crée une boutique e-commerce de sneakers premium avec filtres, panier live et mode sombre...")
                                },
                                minLines = 3,
                                maxLines = 6,
                                enabled = !isGenerating,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricIndigo,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.45f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("home_prompt_input")
                            )

                            // Quick prompt suggestion chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickPromptIdeas.forEach { idea ->
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .clickable(enabled = !isGenerating) {
                                                promptText = idea
                                            }
                                    ) {
                                        Text(
                                            text = idea,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            // Model Speed & Generate Action Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilterChip(
                                        selected = !useFastMode,
                                        onClick = { onToggleFastMode(false) },
                                        label = { Text("Pro 3.8") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.AutoAwesome,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElectricIndigo.copy(alpha = 0.22f),
                                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    FilterChip(
                                        selected = useFastMode,
                                        onClick = { onToggleFastMode(true) },
                                        label = { Text("Rapide") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Bolt,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }

                                Button(
                                    onClick = { onGenerateFromPrompt(promptText) },
                                    enabled = promptText.isNotBlank() && !isGenerating,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricIndigo,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("generate_site_button")
                                ) {
                                    if (isGenerating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Création...")
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Générer le site")
                                    }
                                }
                            }

                            AnimatedVisibility(visible = generationState !is GenerationUiState.Idle) {
                                when (generationState) {
                                    is GenerationUiState.Generating -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            LinearProgressIndicator(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(4.dp)),
                                                color = CyanMint
                                            )
                                            Text(
                                                text = generationState.statusLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CyanMint
                                            )
                                        }
                                    }
                                    is GenerationUiState.Error -> {
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
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
                                    }
                                    else -> Unit
                                }
                            }
                        }
                    }
                }

                // 2. Pro Templates Carousel (All unlocked)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Modèles Pro Débloqués",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Lancez un site complet interactif en un seul clic",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(WebTemplates.all, key = { it.id }) { tpl ->
                                TemplateCard(
                                    template = tpl,
                                    enabled = !isGenerating,
                                    onClick = { onSelectTemplate(tpl) }
                                )
                            }
                        }
                    }
                }

                // 3. User Projects Header & Filter
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Mes Projets Cloud",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Rechercher un projet...") },
                            leadingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_projects_input")
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("Tous") }
                            )
                            ProjectCategory.entries.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat.id,
                                    onClick = {
                                        selectedCategory = if (selectedCategory == cat.id) null else cat.id
                                    },
                                    label = { Text(cat.labelFr) }
                                )
                            }
                        }
                    }
                }

                // 4. Projects List
                when (projectsState) {
                    is ProjectsUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = ElectricIndigo)
                            }
                        }
                    }
                    is ProjectsUiState.Error -> {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = projectsState.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    is ProjectsUiState.Success -> {
                        val filtered = projectsState.projects.filter { project ->
                            val matchesSearch = searchQuery.isBlank() ||
                                project.title.contains(searchQuery, ignoreCase = true) ||
                                project.description.contains(searchQuery, ignoreCase = true)
                            val matchesCat = selectedCategory == null || project.category == selectedCategory
                            matchesSearch && matchesCat
                        }
                        if (filtered.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Web,
                                            contentDescription = null,
                                            tint = ElectricIndigo,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Text(
                                            text = "Aucun projet trouvé",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Décrivez votre site ci-dessus ou choisissez un modèle Pro pour démarrer.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filtered, key = { it.id }) { project ->
                                ProjectCard(
                                    project = project,
                                    onOpen = { onOpenProject(project) },
                                    onToggleFavorite = { onToggleFavorite(project) },
                                    onDelete = { projectToDelete = project }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (projectToDelete != null) {
        val target = projectToDelete!!
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Supprimer ce projet ?") },
            text = { Text("Le projet « ${target.title} » et tout son historique de versions seront définitivement supprimés.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProject(target)
                        projectToDelete = null
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showMobileApkSheet) {
        MobileExportBottomSheet(
            project = null,
            onDismiss = { showMobileApkSheet = false }
        )
    }
}

@Composable
private fun TemplateCard(
    template: WebTemplate,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .width(236.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("template_card_${template.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricIndigo.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = template.category.labelFr,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanMint.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = template.badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanMint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = template.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = template.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenInNew,
                    contentDescription = null,
                    tint = ElectricIndigo,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Utiliser ce modèle",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricIndigo
                )
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: WebProject,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .clickable(onClick = onOpen)
            .testTag("project_card_${project.id}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                ElectricIndigo.copy(alpha = 0.25f),
                                CyanMint.copy(alpha = 0.18f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "v${project.versionCount}",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanMint
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = ProjectCategory.fromId(project.category).labelFr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = project.description.ifBlank { "Site web complet HTML/CSS/JS" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (project.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (project.isFavorite) CoralSpark else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Supprimer le projet",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
