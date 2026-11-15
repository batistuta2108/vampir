package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ProjectRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createProject_andObserve_succeedsForOwner() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ProjectRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createProject(
                userId = aliceUid,
                title = "Landing SaaS IA",
                description = "Site complet généré",
                category = "saas",
                htmlCode = "<main><h1>Bienvenue</h1></main>",
                cssCode = "body { margin: 0; }",
                jsCode = "console.log('init');",
                initialUserPrompt = "Crée un SaaS IA complet",
                initialAssistantSummary = "Site SaaS généré avec succès."
            )
        }
        assertTrue("Expected createProject to succeed: ${createResult.exceptionOrNull()}", createResult.isSuccess)
        val projectId = createResult.getOrThrow()
        assertTrue(projectId.isNotEmpty())

        val projects = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeProjects(aliceUid).first { list -> list.any { it.id == projectId } }
        }
        val created = projects.first { it.id == projectId }
        assertEquals("Landing SaaS IA", created.title)
        assertEquals(1L, created.versionCount)

        val messages = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeMessages(aliceUid, projectId).first { it.size >= 2 }
        }
        assertEquals(2, messages.size)

        val versions = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeVersions(aliceUid, projectId).first { it.isNotEmpty() }
        }
        assertEquals(1L, versions.first().versionNumber)
    }

    @Test
    fun updateProjectCode_incrementsVersionAndStoresSnapshot() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ProjectRepository(firestore)

        val projectId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createProject(
                userId = aliceUid,
                title = "E-Commerce",
                description = "Boutique",
                category = "ecommerce",
                htmlCode = "<h1>V1</h1>",
                cssCode = "",
                jsCode = ""
            ).getOrThrow()
        }

        val createdProject = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getProject(aliceUid, projectId).getOrThrow()
        }
        assertNotNull(createdProject)

        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateProjectCode(
                userId = aliceUid,
                project = createdProject!!,
                newHtml = "<h1>V2 Pro</h1>",
                newCss = "h1 { color: #7C5CFF; }",
                newJs = "console.log('v2');",
                promptLabel = "Ajout thème violet",
                userPrompt = "Passe les titres en violet",
                assistantSummary = "Thème mis à jour."
            )
        }
        assertTrue("Expected updateProjectCode to succeed: ${updateResult.exceptionOrNull()}", updateResult.isSuccess)
        assertEquals(2L, updateResult.getOrThrow())
    }

    @Test
    fun getProject_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = ProjectRepository(firestore)
        val projectId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createProject(
                userId = aliceUid,
                title = "Secret Studio",
                description = "Privé",
                category = "landing",
                htmlCode = "<h1>Secret</h1>",
                cssCode = "",
                jsCode = ""
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = ProjectRepository(firestore)
        val bobResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getProject(aliceUid, projectId)
        }
        assertTrue(bobResult.isFailure)
        val fsEx = bobResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun observeProjects_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = ProjectRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeProjects(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED when unauthenticated")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_webforge@test.com"
        const val BOB_EMAIL = "bob_webforge@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
