package com.example.data.repository

import com.example.data.model.ProjectVersion
import com.example.data.model.StudioMessage
import com.example.data.model.WebProject
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class ProjectRepository(
    private val db: FirebaseFirestore
) {
    private fun projectsCollection(userId: String) =
        db.collection("users").document(userId).collection("projects")

    private fun messagesCollection(userId: String, projectId: String) =
        projectsCollection(userId).document(projectId).collection("messages")

    private fun versionsCollection(userId: String, projectId: String) =
        projectsCollection(userId).document(projectId).collection("versions")

    fun observeProjects(userId: String): Flow<List<WebProject>> =
        projectsCollection(userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    WebProject::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            }

    fun observeProject(userId: String, projectId: String): Flow<WebProject?> =
        projectsCollection(userId)
            .document(projectId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        WebProject::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }

    fun observeMessages(userId: String, projectId: String): Flow<List<StudioMessage>> =
        messagesCollection(userId, projectId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    StudioMessage::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedBy { it.createdAt ?: Timestamp(0, 0) }
            }

    fun observeVersions(userId: String, projectId: String): Flow<List<ProjectVersion>> =
        versionsCollection(userId, projectId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    ProjectVersion::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.versionNumber }
            }

    suspend fun getProject(userId: String, projectId: String): Result<WebProject?> {
        val docRef = projectsCollection(userId).document(projectId)
        return try {
            val snapshot = docRef.get().await()
            val project = if (snapshot.exists()) {
                snapshot.toObject(
                    WebProject::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
            Result.success(project)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createProject(
        userId: String,
        title: String,
        description: String,
        category: String,
        htmlCode: String,
        cssCode: String,
        jsCode: String,
        initialUserPrompt: String? = null,
        initialAssistantSummary: String? = null
    ): Result<String> {
        val projectId = "proj_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val projectRef = projectsCollection(userId).document(projectId)
        val versionRef = versionsCollection(userId, projectId).document("ver_1")

        val projectPayload = mapOf(
            "userId" to userId,
            "title" to title.take(120).ifBlank { "Nouveau Site Pro" },
            "description" to description.take(500),
            "category" to normalizeCategory(category),
            "htmlCode" to htmlCode.take(400000),
            "cssCode" to cssCode.take(200000),
            "jsCode" to jsCode.take(200000),
            "versionCount" to 1L,
            "isFavorite" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val versionPayload = mapOf(
            "userId" to userId,
            "projectId" to projectId,
            "versionNumber" to 1L,
            "promptLabel" to (initialUserPrompt?.take(500)?.ifBlank { "Création initiale" } ?: "Création initiale"),
            "htmlCode" to htmlCode.take(400000),
            "cssCode" to cssCode.take(200000),
            "jsCode" to jsCode.take(200000),
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            val batch = db.batch()
            batch.set(projectRef, projectPayload)
            batch.set(versionRef, versionPayload)

            if (!initialUserPrompt.isNullOrBlank()) {
                val userMsgRef = messagesCollection(userId, projectId)
                    .document("msg_${UUID.randomUUID().toString().replace("-", "").take(12)}")
                batch.set(
                    userMsgRef,
                    mapOf(
                        "userId" to userId,
                        "projectId" to projectId,
                        "role" to "user",
                        "content" to initialUserPrompt.take(20000),
                        "summary" to "",
                        "versionTag" to 1L,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            if (!initialAssistantSummary.isNullOrBlank()) {
                val aiMsgRef = messagesCollection(userId, projectId)
                    .document("msg_${UUID.randomUUID().toString().replace("-", "").take(12)}")
                batch.set(
                    aiMsgRef,
                    mapOf(
                        "userId" to userId,
                        "projectId" to projectId,
                        "role" to "assistant",
                        "content" to initialAssistantSummary.take(20000),
                        "summary" to "Version v1 générée",
                        "versionTag" to 1L,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit().await()
            Result.success(projectId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, projectRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateProjectCode(
        userId: String,
        project: WebProject,
        newHtml: String,
        newCss: String,
        newJs: String,
        promptLabel: String,
        userPrompt: String? = null,
        assistantSummary: String? = null,
        newTitle: String? = null,
        newDescription: String? = null
    ): Result<Long> {
        require(project.id.isNotBlank()) { "Project ID cannot be blank" }
        val nextVersion = (project.versionCount + 1L).coerceAtMost(10000L)
        val projectRef = projectsCollection(userId).document(project.id)
        val versionRef = versionsCollection(userId, project.id)
            .document("ver_${nextVersion}_${UUID.randomUUID().toString().replace("-", "").take(6)}")

        val updateMap = mapOf(
            "title" to (newTitle ?: project.title).take(120).ifBlank { "Projet Web" },
            "description" to (newDescription ?: project.description).take(500),
            "htmlCode" to newHtml.take(400000),
            "cssCode" to newCss.take(200000),
            "jsCode" to newJs.take(200000),
            "versionCount" to nextVersion,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val versionPayload = mapOf(
            "userId" to userId,
            "projectId" to project.id,
            "versionNumber" to nextVersion,
            "promptLabel" to promptLabel.take(500).ifBlank { "Mise à jour v$nextVersion" },
            "htmlCode" to newHtml.take(400000),
            "cssCode" to newCss.take(200000),
            "jsCode" to newJs.take(200000),
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            val batch = db.batch()
            batch.update(projectRef, updateMap)
            batch.set(versionRef, versionPayload)

            if (!userPrompt.isNullOrBlank()) {
                val userMsgRef = messagesCollection(userId, project.id)
                    .document("msg_${UUID.randomUUID().toString().replace("-", "").take(12)}")
                batch.set(
                    userMsgRef,
                    mapOf(
                        "userId" to userId,
                        "projectId" to project.id,
                        "role" to "user",
                        "content" to userPrompt.take(20000),
                        "summary" to "",
                        "versionTag" to nextVersion,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            if (!assistantSummary.isNullOrBlank()) {
                val aiMsgRef = messagesCollection(userId, project.id)
                    .document("msg_${UUID.randomUUID().toString().replace("-", "").take(12)}")
                batch.set(
                    aiMsgRef,
                    mapOf(
                        "userId" to userId,
                        "projectId" to project.id,
                        "role" to "assistant",
                        "content" to assistantSummary.take(20000),
                        "summary" to "Version v$nextVersion déployée",
                        "versionTag" to nextVersion,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit().await()
            Result.success(nextVersion)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, projectRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(userId: String, project: WebProject): Result<Unit> {
        val docRef = projectsCollection(userId).document(project.id)
        return try {
            docRef.update(
                mapOf(
                    "isFavorite" to !project.isFavorite,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun renameProject(userId: String, project: WebProject, newTitle: String): Result<Unit> {
        val cleanTitle = newTitle.trim().take(120)
        if (cleanTitle.isBlank()) return Result.failure(IllegalArgumentException("Titre invalide"))
        val docRef = projectsCollection(userId).document(project.id)
        return try {
            docRef.update(
                mapOf(
                    "title" to cleanTitle,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun restoreVersion(
        userId: String,
        project: WebProject,
        version: ProjectVersion
    ): Result<Long> {
        return updateProjectCode(
            userId = userId,
            project = project,
            newHtml = version.htmlCode,
            newCss = version.cssCode,
            newJs = version.jsCode,
            promptLabel = "Restauration de v${version.versionNumber}",
            assistantSummary = "Version v${version.versionNumber} (${version.promptLabel}) restaurée avec succès."
        )
    }

    suspend fun deleteProject(userId: String, projectId: String): Result<Unit> {
        val projectRef = projectsCollection(userId).document(projectId)
        return try {
            val messages = messagesCollection(userId, projectId).get().await()
            val versions = versionsCollection(userId, projectId).get().await()
            val batch = db.batch()
            messages.documents.forEach { batch.delete(it.reference) }
            versions.documents.forEach { batch.delete(it.reference) }
            batch.delete(projectRef)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, projectRef.path)
            Result.failure(e)
        }
    }

    private fun normalizeCategory(category: String): String {
        val allowed = setOf("landing", "ecommerce", "saas", "portfolio", "dashboard", "restaurant", "custom")
        val lower = category.lowercase()
        return if (lower in allowed) lower else "custom"
    }
}
