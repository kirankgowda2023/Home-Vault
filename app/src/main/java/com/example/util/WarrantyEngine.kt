package com.example.util

import com.example.data.model.ItemEntity
import com.example.data.model.WarrantyInfo
import com.example.data.model.WarrantyState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object WarrantyEngine {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun calculateExpiryDate(purchaseDateMillis: Long, warrantyMonths: Int): Long? {
        if (warrantyMonths <= 0) return null
        val calendar = Calendar.getInstance().apply {
            timeInMillis = purchaseDateMillis
            add(Calendar.MONTH, warrantyMonths)
        }
        return calendar.timeInMillis
    }

    fun getWarrantyInfo(purchaseDateMillis: Long, warrantyMonths: Int): WarrantyInfo {
        if (warrantyMonths <= 0) {
            return WarrantyInfo(
                state = WarrantyState.NO_WARRANTY,
                expiryDateMillis = null,
                daysRemaining = 0,
                humanReadableRemaining = "No warranty info",
                isExpiringSoon = false
            )
        }

        val expiryMillis = calculateExpiryDate(purchaseDateMillis, warrantyMonths) ?: return WarrantyInfo(
            state = WarrantyState.NO_WARRANTY,
            expiryDateMillis = null,
            daysRemaining = 0,
            humanReadableRemaining = "No warranty info",
            isExpiringSoon = false
        )

        val nowMillis = System.currentTimeMillis()
        val diffMillis = expiryMillis - nowMillis
        val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis)

        val expiryDateStr = formatDate(expiryMillis)

        return when {
            diffMillis < 0 -> {
                WarrantyInfo(
                    state = WarrantyState.EXPIRED,
                    expiryDateMillis = expiryMillis,
                    daysRemaining = daysRemaining,
                    humanReadableRemaining = "Expired on $expiryDateStr",
                    isExpiringSoon = false
                )
            }
            daysRemaining <= 30 -> {
                val text = if (daysRemaining == 0L) "Expires today" else "Expires in $daysRemaining days"
                WarrantyInfo(
                    state = WarrantyState.EXPIRING_SOON,
                    expiryDateMillis = expiryMillis,
                    daysRemaining = daysRemaining,
                    humanReadableRemaining = text,
                    isExpiringSoon = true
                )
            }
            else -> {
                // Active warranty: calculate months and years
                val monthsRemaining = (daysRemaining / 30.4375).toInt()
                val readable = when {
                    monthsRemaining >= 24 -> {
                        val years = monthsRemaining / 12
                        val remainderMonths = monthsRemaining % 12
                        if (remainderMonths > 0) "$years years $remainderMonths mos remaining"
                        else "$years years remaining"
                    }
                    monthsRemaining >= 12 -> {
                        val remainderMonths = monthsRemaining - 12
                        if (remainderMonths > 0) "1 year $remainderMonths mos remaining"
                        else "1 year remaining"
                    }
                    else -> {
                        "$monthsRemaining months remaining"
                    }
                }
                WarrantyInfo(
                    state = WarrantyState.ACTIVE,
                    expiryDateMillis = expiryMillis,
                    daysRemaining = daysRemaining,
                    humanReadableRemaining = readable,
                    isExpiringSoon = false
                )
            }
        }
    }

    fun formatDate(timeMillis: Long): String {
        return dateFormat.format(Date(timeMillis))
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("INR", "₹").trim()
    }

    fun formatLakhOrThousand(amount: Double): String {
        return when {
            amount >= 100_000_00 -> String.format(Locale.getDefault(), "₹%.2f Cr", amount / 100_000_00)
            amount >= 100_000 -> String.format(Locale.getDefault(), "₹%.1f Lakh", amount / 100_000)
            amount >= 1_000 -> String.format(Locale.getDefault(), "₹%.1f K", amount / 1_000)
            else -> formatCurrency(amount)
        }
    }

    /**
     * Default realistic seed items representing the user's scenario
     * e.g., Samsung Refrigerator, LG Washing Machine, Apple MacBook Pro, Sony Bravia TV, Royal Enfield
     */
    fun getSampleSeedItems(): List<ItemEntity> {
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L

        return listOf(
            ItemEntity(
                name = "Samsung Refrigerator",
                category = "Home",
                brand = "Samsung",
                model = "RT42 Double Door 394L",
                serialNumber = "SN-SM9842100A",
                purchaseDate = now - (240 * dayMillis), // ~8 months ago
                purchasePrice = 48000.0,
                warrantyMonths = 24, // 2 Years -> Active (~16 mos left)
                sellerName = "Croma Electronics",
                sellerPhone = "+91 98765 43210",
                sellerLocation = "Indiranagar, Bangalore",
                notes = "Annual service checkup completed. Digital inverter compressor has 10 yr warranty.",
                billPhotoUri = "asset_bill_sample_samsung"
            ),
            ItemEntity(
                name = "LG Front Load Washing Machine",
                category = "Home",
                brand = "LG",
                model = "FHT1408ZWL 8Kg AI DirectDrive",
                serialNumber = "LG-WM8823194B",
                purchaseDate = now - (340 * dayMillis), // ~11 months ago
                purchasePrice = 38500.0,
                warrantyMonths = 12, // 1 Year -> Expiring in ~25 days!
                sellerName = "Reliance Digital",
                sellerPhone = "+91 98450 11223",
                sellerLocation = "Phoenix Marketcity",
                notes = "Motor covered under extended 10-year warranty. Free drum descaling coupon inside.",
                billPhotoUri = "asset_bill_sample_lg"
            ),
            ItemEntity(
                name = "Apple MacBook Pro 14\"",
                category = "Electronics",
                brand = "Apple",
                model = "M3 Pro 18GB / 512GB Space Black",
                serialNumber = "C02G89XYMD6R",
                purchaseDate = now - (350 * dayMillis), // ~11.5 months ago
                purchasePrice = 189900.0,
                warrantyMonths = 12, // 1 Year -> Expiring in ~15 days!
                sellerName = "Imagine Apple Premium Reseller",
                sellerPhone = "+91 80 4123 7890",
                sellerLocation = "Forum Mall, Koramangala",
                notes = "Eligible for AppleCare+ extension within 1 year of original purchase.",
                billPhotoUri = "asset_bill_sample_apple"
            ),
            ItemEntity(
                name = "Sony Bravia 55\" 4K OLED TV",
                category = "Electronics",
                brand = "Sony",
                model = "XR-55A80L Cognitive Processor",
                serialNumber = "SNY-55A8-90214",
                purchaseDate = now - (600 * dayMillis), // ~20 months ago
                purchasePrice = 134000.0,
                warrantyMonths = 36, // 3 Years -> Active (~16 mos left)
                sellerName = "Sony Center",
                sellerPhone = "+91 99000 88776",
                sellerLocation = "Jayanagar 4th Block",
                notes = "Screen protection plan included by dealer. Wall mount installed by technician.",
                billPhotoUri = "asset_bill_sample_sony"
            ),
            ItemEntity(
                name = "Dyson V12 Cordless Vacuum",
                category = "Home",
                brand = "Dyson",
                model = "V12 Detect Slim Total Clean",
                serialNumber = "DYS-V12-77894",
                purchaseDate = now - (750 * dayMillis), // ~25 months ago
                purchasePrice = 45900.0,
                warrantyMonths = 24, // 2 Years -> Expired recently!
                sellerName = "Dyson India Official",
                sellerPhone = "1800 258 6688",
                sellerLocation = "Online Store",
                notes = "Battery replaced once in warranty period. Filters washable monthly.",
                billPhotoUri = "asset_bill_sample_dyson"
            ),
            ItemEntity(
                name = "Royal Enfield Hunter 350",
                category = "Vehicle",
                brand = "Royal Enfield",
                model = "Dapper Ash Dual Channel ABS",
                serialNumber = "ME3J350HUN2024",
                purchaseDate = now - (180 * dayMillis), // 6 months ago
                purchasePrice = 178000.0,
                warrantyMonths = 36, // 3 Years -> Active
                sellerName = "Acclaim Motors Royal Enfield",
                sellerPhone = "+91 80 2664 5500",
                sellerLocation = "Banashankari",
                notes = "Roadside assistance valid till 2027. 2nd free service scheduled for next month.",
                billPhotoUri = "asset_bill_sample_re"
            ),
            ItemEntity(
                name = "Instant Pot Pro Multi-Cooker",
                category = "Kitchen",
                brand = "Instant Pot",
                model = "Pro 10-in-1 5.7L",
                serialNumber = "IP-PRO-60-234",
                purchaseDate = now - (90 * dayMillis),
                purchasePrice = 12999.0,
                warrantyMonths = 24,
                sellerName = "Amazon India",
                sellerPhone = "1800 3000 9009",
                sellerLocation = "Online",
                notes = "Extra silicone sealing ring purchased separately.",
                billPhotoUri = "asset_bill_sample_ip"
            )
        )
    }
}
