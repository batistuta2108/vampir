package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.ai.WebAiGenerator
import com.example.data.repository.ProjectRepository
import com.example.firebase.configureAppCheck
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.home.HomeHubScreen
import com.example.ui.studio.StudioViewModel
import com.example.ui.theme.WebForgeTheme
import com.example.ui.workspace.ProjectWorkspaceScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private const val ROUTE_HUB = "hub"
private const val ROUTE_WORKSPACE = "workspace"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure App Check with launch intent before any UI, ViewModels, or Firebase operations
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(true) }
            WebForgeTheme(darkTheme = isDarkTheme) {
                WebForgeApp(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
private fun WebForgeApp(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val auth = Firebase.auth
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        val uid = user.uid
        val displayName = user.displayName
            ?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "Créateur Pro"

        key(uid) {
            AuthenticatedStudioRoot(
                currentUserId = uid,
                userDisplayName = displayName,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    }
}

@Composable
private fun AuthenticatedStudioRoot(
    currentUserId: String,
    userDisplayName: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val studioViewModel: StudioViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as Application
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                StudioViewModel(
                    repository = ProjectRepository(db),
                    aiGenerator = WebAiGenerator(),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val projectsState by studioViewModel.projectsState.collectAsStateWithLifecycle()
    val activeProject by studioViewModel.activeProject.collectAsStateWithLifecycle()
    val activeMessages by studioViewModel.activeMessages.collectAsStateWithLifecycle()
    val activeVersions by studioViewModel.activeVersions.collectAsStateWithLifecycle()
    val generationState by studioViewModel.generationState.collectAsStateWithLifecycle()
    val useFastMode by studioViewModel.useFastMode.collectAsStateWithLifecycle()

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_HUB
    ) {
        composable(ROUTE_HUB) {
            HomeHubScreen(
                userDisplayName = userDisplayName,
                projectsState = projectsState,
                generationState = generationState,
                useFastMode = useFastMode,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onToggleFastMode = { studioViewModel.setFastMode(it) },
                onGenerateFromPrompt = { prompt ->
                    studioViewModel.createProjectFromPrompt(prompt) {
                        navController.navigate(ROUTE_WORKSPACE)
                    }
                },
                onSelectTemplate = { template ->
                    studioViewModel.createProjectFromTemplate(template) {
                        navController.navigate(ROUTE_WORKSPACE)
                    }
                },
                onOpenProject = { project ->
                    studioViewModel.selectProject(project.id)
                    studioViewModel.clearGenerationState()
                    navController.navigate(ROUTE_WORKSPACE)
                },
                onToggleFavorite = { studioViewModel.toggleFavorite(it) },
                onDeleteProject = { studioViewModel.deleteProject(it.id) },
                onClearGenerationState = { studioViewModel.clearGenerationState() },
                onSignOut = {
                    signOutUser(
                        credentialManager = credentialManager,
                        scope = scope
                    )
                }
            )
        }

        composable(ROUTE_WORKSPACE) {
            ProjectWorkspaceScreen(
                project = activeProject,
                messages = activeMessages,
                versions = activeVersions,
                generationState = generationState,
                onBack = {
                    studioViewModel.selectProject(null)
                    studioViewModel.clearGenerationState()
                    navController.popBackStack()
                },
                onRefineWithAi = { proj, prompt ->
                    studioViewModel.refineActiveProject(proj, prompt)
                },
                onSaveManualCode = { proj, html, css, js ->
                    studioViewModel.saveManualCode(proj, html, css, js)
                },
                onRestoreVersion = { proj, ver ->
                    studioViewModel.restoreVersion(proj, ver)
                },
                onRenameProject = { proj, title ->
                    studioViewModel.renameProject(proj, title)
                },
                onClearGenerationState = { studioViewModel.clearGenerationState() }
            )
        }
    }
}
