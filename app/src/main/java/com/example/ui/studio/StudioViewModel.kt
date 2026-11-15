package com.example.ui.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.WebAiGenerator
import com.example.data.model.ProjectVersion
import com.example.data.model.StudioMessage
import com.example.data.model.WebProject
import com.example.data.repository.ProjectRepository
import com.example.data.templates.WebTemplate
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProjectsUiState {
    data object Loading : ProjectsUiState
    data class Success(val projects: List<WebProject>) : ProjectsUiState
    data class Error(val message: String) : ProjectsUiState
}

sealed interface GenerationUiState {
    data object Idle : GenerationUiState
    data class Generating(val statusLabel: String) : GenerationUiState
    data class Success(val message: String) : GenerationUiState
    data class Error(val message: String) : GenerationUiState
}

class StudioViewModel(
    private val repository: ProjectRepository,
    private val aiGenerator: WebAiGenerator,
    private val currentUserId: String
) : ViewModel() {

    val projectsState: StateFlow<ProjectsUiState> = repository.observeProjects(currentUserId)
        .map<List<WebProject>, ProjectsUiState> { ProjectsUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            emit(ProjectsUiState.Error(root.localizedMessage ?: "Impossible de charger vos projets."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ProjectsUiState.Loading
        )

    private val _selectedProjectId = MutableStateFlow<String?>(null)
    val selectedProjectId: StateFlow<String?> = _selectedProjectId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeProject: StateFlow<WebProject?> = _selectedProjectId
        .flatMapLatest { pid ->
            if (pid.isNullOrBlank()) flowOf(null)
            else repository.observeProject(currentUserId, pid)
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<StudioMessage>> = _selectedProjectId
        .flatMapLatest { pid ->
            if (pid.isNullOrBlank()) flowOf(emptyList())
            else repository.observeMessages(currentUserId, pid)
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeVersions: StateFlow<List<ProjectVersion>> = _selectedProjectId
        .flatMapLatest { pid ->
            if (pid.isNullOrBlank()) flowOf(emptyList())
            else repository.observeVersions(currentUserId, pid)
        }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val generationState: StateFlow<GenerationUiState> = _generationState.asStateFlow()

    private val _useFastMode = MutableStateFlow(false)
    val useFastMode: StateFlow<Boolean> = _useFastMode.asStateFlow()

    fun selectProject(projectId: String?) {
        _selectedProjectId.value = projectId
    }

    fun setFastMode(fast: Boolean) {
        _useFastMode.value = fast
    }

    fun clearGenerationState() {
        _generationState.value = GenerationUiState.Idle
    }

    fun createProjectFromPrompt(
        prompt: String,
        onCreated: (String) -> Unit
    ) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank()) return

        viewModelScope.launch {
            _generationState.value = GenerationUiState.Generating("Architecture et génération IA du site complet...")
            val aiResult = aiGenerator.generateNewWebsite(
                userPrompt = cleanPrompt,
                useFastMode = _useFastMode.value
            )
            aiResult.fold(
                onSuccess = { bundle ->
                    _generationState.value = GenerationUiState.Generating("Sauvegarde du projet dans le cloud...")
                    val saveResult = repository.createProject(
                        userId = currentUserId,
                        title = bundle.title,
                        description = bundle.description,
                        category = bundle.category,
                        htmlCode = bundle.htmlCode,
                        cssCode = bundle.cssCode,
                        jsCode = bundle.jsCode,
                        initialUserPrompt = cleanPrompt,
                        initialAssistantSummary = bundle.summary
                    )
                    saveResult.fold(
                        onSuccess = { newId ->
                            _selectedProjectId.value = newId
                            _generationState.value = GenerationUiState.Success("Site « ${bundle.title} » généré !")
                            onCreated(newId)
                        },
                        onFailure = { err ->
                            _generationState.value = GenerationUiState.Error(
                                err.localizedMessage ?: "Échec de la sauvegarde Firestore."
                            )
                        }
                    )
                },
                onFailure = { err ->
                    _generationState.value = GenerationUiState.Error(
                        err.localizedMessage ?: "La génération IA a échoué."
                    )
                }
            )
        }
    }

    fun createProjectFromTemplate(
        template: WebTemplate,
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            _generationState.value = GenerationUiState.Generating("Déploiement du modèle ${template.title}...")
            val saveResult = repository.createProject(
                userId = currentUserId,
                title = template.title,
                description = template.subtitle,
                category = template.category.id,
                htmlCode = template.htmlCode,
                cssCode = template.cssCode,
                jsCode = template.jsCode,
                initialUserPrompt = "Créer depuis le modèle Pro : ${template.title}",
                initialAssistantSummary = "Modèle complet « ${template.title} » initialisé avec HTML, CSS et JavaScript interactifs."
            )
            saveResult.fold(
                onSuccess = { newId ->
                    _selectedProjectId.value = newId
                    _generationState.value = GenerationUiState.Success("Modèle « ${template.title} » prêt !")
                    onCreated(newId)
                },
                onFailure = { err ->
                    _generationState.value = GenerationUiState.Error(
                        err.localizedMessage ?: "Impossible de créer le projet."
                    )
                }
            )
        }
    }

    fun refineActiveProject(
        project: WebProject,
        editPrompt: String
    ) {
        val cleanPrompt = editPrompt.trim()
        if (cleanPrompt.isBlank()) return

        viewModelScope.launch {
            _generationState.value = GenerationUiState.Generating("Application de vos modifications avec l'IA...")
            val aiResult = aiGenerator.refineExistingWebsite(
                editPrompt = cleanPrompt,
                currentTitle = project.title,
                currentCategory = project.category,
                currentHtml = project.htmlCode,
                currentCss = project.cssCode,
                currentJs = project.jsCode,
                useFastMode = _useFastMode.value
            )
            aiResult.fold(
                onSuccess = { bundle ->
                    val updateResult = repository.updateProjectCode(
                        userId = currentUserId,
                        project = project,
                        newHtml = bundle.htmlCode,
                        newCss = bundle.cssCode,
                        newJs = bundle.jsCode,
                        promptLabel = cleanPrompt.take(120),
                        userPrompt = cleanPrompt,
                        assistantSummary = bundle.summary,
                        newTitle = bundle.title,
                        newDescription = bundle.description
                    )
                    updateResult.fold(
                        onSuccess = { v ->
                            _generationState.value = GenerationUiState.Success("Version v$v générée et appliquée !")
                        },
                        onFailure = { err ->
                            _generationState.value = GenerationUiState.Error(
                                err.localizedMessage ?: "Impossible d'enregistrer la mise à jour."
                            )
                        }
                    )
                },
                onFailure = { err ->
                    _generationState.value = GenerationUiState.Error(
                        err.localizedMessage ?: "Erreur lors de la modification IA."
                    )
                }
            )
        }
    }

    fun saveManualCode(
        project: WebProject,
        htmlCode: String,
        cssCode: String,
        jsCode: String
    ) {
        viewModelScope.launch {
            _generationState.value = GenerationUiState.Generating("Enregistrement du code...")
            val res = repository.updateProjectCode(
                userId = currentUserId,
                project = project,
                newHtml = htmlCode,
                newCss = cssCode,
                newJs = jsCode,
                promptLabel = "Édition manuelle du code",
                assistantSummary = "Modifications manuelles enregistrées dans index.html / styles.css / app.js."
            )
            res.fold(
                onSuccess = { v ->
                    _generationState.value = GenerationUiState.Success("Code sauvegardé (v$v)")
                },
                onFailure = { err ->
                    _generationState.value = GenerationUiState.Error(
                        err.localizedMessage ?: "Erreur lors de la sauvegarde du code."
                    )
                }
            )
        }
    }

    fun restoreVersion(project: WebProject, version: ProjectVersion) {
        viewModelScope.launch {
            _generationState.value = GenerationUiState.Generating("Restauration de v${version.versionNumber}...")
            val res = repository.restoreVersion(currentUserId, project, version)
            res.fold(
                onSuccess = { v ->
                    _generationState.value = GenerationUiState.Success("Version v${version.versionNumber} restaurée en v$v !")
                },
                onFailure = { err ->
                    _generationState.value = GenerationUiState.Error(
                        err.localizedMessage ?: "Échec de la restauration."
                    )
                }
            )
        }
    }

    fun toggleFavorite(project: WebProject) {
        viewModelScope.launch {
            repository.toggleFavorite(currentUserId, project)
        }
    }

    fun renameProject(project: WebProject, newTitle: String) {
        viewModelScope.launch {
            repository.renameProject(currentUserId, project, newTitle)
        }
    }

    fun deleteProject(projectId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.deleteProject(currentUserId, projectId)
            if (res.isSuccess) {
                if (_selectedProjectId.value == projectId) {
                    _selectedProjectId.value = null
                }
                onDeleted()
            }
        }
    }
}
