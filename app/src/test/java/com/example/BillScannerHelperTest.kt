package com.example

import com.example.util.BillScannerHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BillScannerHelperTest {

    @Test
    fun testParseExtractedJson_success() {
        val sampleResponse = """
            {
              "name": "Samsung 43-inch Crystal 4K UHD TV",
              "category": "Electronics",
              "brand": "Samsung",
              "model": "UA43AUE60AKLXL",
              "serialNumber": "SN987654321",
              "purchasePrice": 28990.0,
              "purchaseDate": "2024-05-12",
              "warrantyMonths": 24,
              "sellerName": "Croma Electronics",
              "sellerPhone": "+91 9876543210",
              "sellerLocation": "Indiranagar, Bangalore",
              "notes": "Invoice #CR-2024-889. Panel warranty 2 years."
            }
        """.trimIndent()

        val data = BillScannerHelper.parseExtractedJson(sampleResponse)

        assertEquals("Samsung 43-inch Crystal 4K UHD TV", data.name)
        assertEquals("Electronics", data.category)
        assertEquals("Samsung", data.brand)
        assertEquals("UA43AUE60AKLXL", data.model)
        assertEquals("SN987654321", data.serialNumber)
        assertEquals(28990.0, data.purchasePrice ?: 0.0, 0.001)
        assertEquals(24, data.warrantyMonths)
        assertEquals("Croma Electronics", data.sellerName)
        assertEquals("+91 9876543210", data.sellerPhone)
        assertEquals("Indiranagar, Bangalore", data.sellerLocation)
        assertEquals("Invoice #CR-2024-889. Panel warranty 2 years.", data.notes)
        assertNotNull(data.purchaseDateMillis)
        assertTrue((data.purchaseDateMillis ?: 0L) > 0L)
    }

    @Test
    fun testParseExtractedJson_withMarkdownFences() {
        val sampleResponse = """
            ```json
            {
              "name": "Sony WH-1000XM5 Wireless Headphones",
              "category": "personal audio",
              "brand": "Sony",
              "purchasePrice": 24990,
              "warrantyMonths": 12
            }
            ```
        """.trimIndent()

        val data = BillScannerHelper.parseExtractedJson(sampleResponse)

        assertEquals("Sony WH-1000XM5 Wireless Headphones", data.name)
        assertEquals("Electronics", data.category) // normalized from personal audio / audio
        assertEquals("Sony", data.brand)
        assertEquals(24990.0, data.purchasePrice ?: 0.0, 0.001)
        assertEquals(12, data.warrantyMonths)
    }
}
