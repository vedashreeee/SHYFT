package com.shyft.privacy

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.shyft.privacy.data.repository.ProtectedAppsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProtectedAppsDataStoreTest {

    @Test
    fun testDataStorePersistence_savesAndLoadsSelectionsCorrectly() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = ProtectedAppsRepository(appContext)

        val testSet = setOf("phone", "gpay", "whatsapp", "calculator")
        repository.setProtectedApps(testSet)

        val loadedSet = repository.protectedAppIdsFlow.first()
        assertEquals(testSet, loadedSet)

        repository.toggleAppProtection("youtube", true)
        val updatedSet = repository.protectedAppIdsFlow.first()
        assertTrue(updatedSet.contains("youtube"))
        assertTrue(updatedSet.contains("calculator"))
    }
}
