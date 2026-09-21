package com.shyft.privacy

import com.shyft.privacy.data.model.DefaultProtectedApps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtectedAppsTest {

    @Test
    fun defaultAppsList_containsAllRequiredPromptApps() {
        val requiredAppIds = setOf(
            "phone", "gpay", "whatsapp", "youtube", "spotify", "instagram", "gallery", "calculator"
        )

        val appIds = DefaultProtectedApps.APPS.map { it.id }.toSet()

        assertEquals(8, appIds.size)
        assertTrue(appIds.containsAll(requiredAppIds))
    }

    @Test
    fun defaultAppNames_matchSpecifications() {
        val appNames = DefaultProtectedApps.APPS.map { it.name }

        assertTrue(appNames.contains("Phone"))
        assertTrue(appNames.contains("Google Pay"))
        assertTrue(appNames.contains("WhatsApp"))
        assertTrue(appNames.contains("YouTube"))
        assertTrue(appNames.contains("Spotify"))
        assertTrue(appNames.contains("Instagram"))
        assertTrue(appNames.contains("Gallery"))
        assertTrue(appNames.contains("Calculator"))
    }
}
