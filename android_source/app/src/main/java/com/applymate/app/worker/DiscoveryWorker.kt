package com.applymate.app.worker

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.applymate.app.data.AppDatabase
import com.applymate.app.data.DiscoveredOpportunity
import com.applymate.app.data.PreferenceProfile
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

class DiscoveryWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val db = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "applymate-db"
    ).build()

    override suspend fun doWork(): Result {
        val preferenceDao = db.preferenceDao()
        val discoveryDao = db.discoveryDao()
        
        val profile = preferenceDao.getPreferenceProfile().firstOrNull() ?: return Result.success()

        try {
            // 1. Fetch from API (Mocking SerpAPI call)
            val rawOpportunities = fetchOpportunitiesFromApi(profile)

            // 2. Score with Gemini
            val scoredOpportunities = scoreOpportunitiesWithGemini(rawOpportunities, profile)

            // 3. Save to DB
            discoveryDao.clearUnsaved()
            discoveryDao.insertOpportunities(scoredOpportunities)

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun fetchOpportunitiesFromApi(profile: PreferenceProfile): List<RawOpportunity> {
        // In a real app, this would call SerpAPI or Google Custom Search
        // Mocking results for 2026 build
        return listOf(
            RawOpportunity("Software Engineering Intern", "Google", "Tokyo, Japan", "https://google.com/jobs", "Exciting internship in Japan"),
            RawOpportunity("DAAD Scholarship", "DAAD", "Bonn, Germany", "https://daad.de", "Full scholarship for international students"),
            RawOpportunity("Research Assistant", "MIT", "Remote", "https://mit.edu", "Remote research opportunity in AI")
        )
    }

    private suspend fun scoreOpportunitiesWithGemini(
        raw: List<RawOpportunity>,
        profile: PreferenceProfile
    ): List<DiscoveredOpportunity> {
        // In a real app, use process.env.GEMINI_API_KEY
        // Mocking Gemini scoring logic
        return raw.map {
            val score = calculateMockMatchScore(it, profile)
            DiscoveredOpportunity(
                title = it.title,
                organization = it.organization,
                location = it.location,
                link = it.link,
                matchScore = score,
                description = it.description,
                type = if (it.title.contains("Scholarship", true)) "Scholarship" else "Internship"
            )
        }
    }

    private fun calculateMockMatchScore(opp: RawOpportunity, profile: PreferenceProfile): Int {
        var score = 50
        if (opp.location.contains(profile.preferredLocations, true)) score += 20
        if (opp.title.contains(profile.fieldOfStudy, true)) score += 20
        return score.coerceIn(0, 100)
    }

    data class RawOpportunity(
        val title: String,
        val organization: String,
        val location: String,
        val link: String,
        val description: String
    )
}
