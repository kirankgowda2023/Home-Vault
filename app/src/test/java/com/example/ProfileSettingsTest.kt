package com.example

import com.example.data.model.ItemEntity
import com.example.data.model.UserProfile
import com.example.util.FileStorageHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileSettingsTest {

    @Test
    fun testUserProfileDataClass() {
        val profile = UserProfile(
            name = "Kiran Gowda",
            mobileNumber = "+1 555-0199",
            residenceAddress = "104 Oakwood Condo, Apt 4B"
        )
        assertEquals("Kiran Gowda", profile.name)
        assertEquals("+1 555-0199", profile.mobileNumber)
        assertEquals("104 Oakwood Condo, Apt 4B", profile.residenceAddress)
    }

    @Test
    fun testGenerateInventoryReportWithProfile() {
        val items = listOf(
            ItemEntity(
                name = "LG OLED 65 Smart TV",
                category = "Electronics",
                purchasePrice = 1800.0,
                purchaseDate = System.currentTimeMillis() - 1000L * 3600 * 24 * 60,
                warrantyMonths = 24
            )
        )
        val report = FileStorageHelper.generateInventoryReportText(
            items = items,
            ownerName = "Kiran Gowda",
            mobileNumber = "+1 555-0199",
            residenceAddress = "104 Oakwood Condo, Apt 4B"
        )

        assertTrue(report.contains("Owner Name: Kiran Gowda"))
        assertTrue(report.contains("Mobile No:  +1 555-0199"))
        assertTrue(report.contains("Residence:  104 Oakwood Condo, Apt 4B"))
        assertTrue(report.contains("LG OLED 65 Smart TV"))
    }
}
