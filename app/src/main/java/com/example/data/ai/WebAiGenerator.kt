package com.example.data.ai

import android.util.Log
import com.example.data.model.GeneratedWebsiteBundle
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

class WebAiGenerator {

    private val websiteSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Titre court et accrocheur du site web (2 à 5 mots)"),
            "description" to Schema.string(description = "Description concise du site web en français"),
            "category" to Schema.enumeration(
                values = listOf("landing", "ecommerce", "saas", "portfolio", "dashboard", "restaurant", "custom"),
                description = "Catégorie la plus adaptée"
            ),
            "htmlCode" to Schema.string(
                description = "Contenu HTML5 complet sémantique (sans balises <html>/<head>/<body> si fragment, ou document complet propre) incluant navigation, hero, sections riches et interactions onclick/id."
            ),
            "cssCode" to Schema.string(
                description = "Feuille de style CSS3 moderne responsive complète (variables CSS, flexbox/grid, effets visuels, @media mobile)."
            ),
            "jsCode" to Schema.string(
                description = "Code JavaScript vanilla complet et fonctionnel gérant toutes les interactions, filtres, modales, formulaires ou calculateurs."
            ),
            "summary" to Schema.string(
                description = "Résumé clair en français des fonctionnalités construites ou modifiées."
            )
        )
    )

    private val fastModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = websiteSchema
                temperature = 0.7f
            }
        )
    }

    private val proModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = websiteSchema
                temperature = 0.75f
            }
        )
    }

    suspend fun generateNewWebsite(
        userPrompt: String,
        useFastMode: Boolean = false
    ): Result<GeneratedWebsiteBundle> {
        val systemInstructions = """
            Tu es un architecte web senior full-stack spécialisé dans la création de sites web complets de qualité agence Pro.
            L'utilisateur demande : "$userPrompt"

            Contraintes impératives :
            1. Génère un site web COMPLET, moderne, visuellement spectaculaire, 100% responsive mobile/tablette/desktop.
            2. Évite les contenus vides ou "Lorem ipsum" : rédige de vrais textes professionnels en français (ou dans la langue demandée).
            3. Inclus des fonctionnalités interactives réelles en JavaScript (ex: onglets, panier, calculateur, filtres, modales, formulaire avec validation et retour visuel).
            4. N'utilise aucun lien externe cassé ou image externe aléatoire : utilise des dégradés CSS, SVG inline ou icônes emoji/typographiques élégantes pour garantir un rendu 100% autonome hors-ligne.
            5. Sépare proprement le HTML (`htmlCode`), le CSS (`cssCode`) et le JS (`jsCode`).
        """.trimIndent()

        return executeGeneration(systemInstructions, useFastMode)
    }

    suspend fun refineExistingWebsite(
        editPrompt: String,
        currentTitle: String,
        currentCategory: String,
        currentHtml: String,
        currentCss: String,
        currentJs: String,
        useFastMode: Boolean = false
    ): Result<GeneratedWebsiteBundle> {
        val prompt = """
            Tu es un développeur senior full-stack. Modifie ce site web existant selon la demande de l'utilisateur tout en conservant sa qualité, son design cohérent et ses fonctionnalités existantes.

            Titre actuel : $currentTitle
            Catégorie actuelle : $currentCategory
            Demande de modification : "$editPrompt"

            --- HTML ACTUEL ---
            ${currentHtml.take(25000)}

            --- CSS ACTUEL ---
            ${currentCss.take(15000)}

            --- JS ACTUEL ---
            ${currentJs.take(15000)}

            Retourne la version complète mise à jour dans le JSON structuré.
        """.trimIndent()

        return executeGeneration(prompt, useFastMode)
    }

    private suspend fun executeGeneration(
        prompt: String,
        useFastMode: Boolean
    ): Result<GeneratedWebsiteBundle> {
        return try {
            val activeModel = if (useFastMode) fastModel else proModel
            val response = withTimeout(60_000L) {
                activeModel.generateContent(prompt)
            }
            val rawJson = response.text
                ?: return Result.failure(IllegalStateException("Le modèle IA a renvoyé une réponse vide."))

            val json = JSONObject(rawJson)
            val bundle = GeneratedWebsiteBundle(
                title = json.optString("title", "Site Web Pro").ifBlank { "Site Web Pro" },
                description = json.optString("description", "Site web généré avec WebForge Pro"),
                category = json.optString("category", "custom"),
                htmlCode = json.optString("htmlCode", ""),
                cssCode = json.optString("cssCode", ""),
                jsCode = json.optString("jsCode", ""),
                summary = json.optString("summary", "Site web généré avec succès.")
            )
            if (bundle.htmlCode.isBlank()) {
                return Result.failure(IllegalStateException("Le code HTML généré est vide. Veuillez réessayer."))
            }
            Result.success(bundle)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI website generation failed", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "WebAiGenerator"
    }
}
