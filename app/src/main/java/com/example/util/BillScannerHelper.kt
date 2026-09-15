package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ScannedBillData(
    val name: String? = null,
    val category: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val serialNumber: String? = null,
    val purchasePrice: Double? = null,
    val purchaseDateMillis: Long? = null,
    val purchaseDateFormatted: String? = null,
    val warrantyMonths: Int? = null,
    val sellerName: String? = null,
    val sellerPhone: String? = null,
    val sellerLocation: String? = null,
    val notes: String? = null
)

sealed class BillScanResult {
    data class Success(val data: ScannedBillData) : BillScanResult()
    data class Error(val message: String, val isApiKeyMissing: Boolean = false) : BillScanResult()
}

object BillScannerHelper {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val supportedCategories = listOf(
        "Home", "Electronics", "Vehicle", "Kitchen", "Personal", "Other"
    )

    suspend fun scanBillFromUri(context: Context, imageUri: Uri): BillScanResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext BillScanResult.Error(
                message = "Gemini API key is not configured. Please add GEMINI_API_KEY in AI Studio Secrets.",
                isApiKeyMissing = true
            )
        }

        val bitmap = loadScaledBitmap(context, imageUri)
            ?: return@withContext BillScanResult.Error("Could not process bill image. Please check the file.")

        val base64Image = bitmapToBase64(bitmap)

        try {
            val jsonPayload = buildRequestPayload(base64Image)
            val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext BillScanResult.Error("Gemini API error (${response.code}): ${parseErrorMessage(responseBody)}")
            }

            val extractedJsonText = extractTextFromGeminiResponse(responseBody)
                ?: return@withContext BillScanResult.Error("Unable to extract details from invoice.")

            val parsedData = parseExtractedJson(extractedJsonText)
            BillScanResult.Success(parsedData)
        } catch (e: Exception) {
            e.printStackTrace()
            BillScanResult.Error("Scan failed: ${e.localizedMessage ?: "Unknown network error"}")
        }
    }

    suspend fun scanBillFromFilePath(context: Context, filePath: String): BillScanResult = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists()) {
            return@withContext BillScanResult.Error("Bill file does not exist.")
        }
        val uri = Uri.fromFile(file)
        scanBillFromUri(context, uri)
    }

    private fun buildRequestPayload(base64Data: String): String {
        val prompt = """
            Analyze this bill, invoice, receipt, or cash memo.
            Extract all product, financial, seller, and warranty details.
            Return a raw JSON object with exactly these fields:
            {
              "name": "Exact product/item name (e.g., Samsung Galaxy S23, Whirlpool Refrigerator)",
              "category": "One of: Home, Electronics, Vehicle, Kitchen, Personal, Other",
              "brand": "Brand or manufacturer name",
              "model": "Model code or number if printed",
              "serialNumber": "Serial number, IMEI, or product ID if printed",
              "purchasePrice": 12999.00 (numeric total or unit price paid, without currency symbols),
              "purchaseDate": "YYYY-MM-DD (format date if printed, e.g. 2024-03-15)",
              "warrantyMonths": 12 (integer warranty months if mentioned e.g. 1 year -> 12, 2 years -> 24, or 0 if none),
              "sellerName": "Store, retailer, or vendor name (e.g. Croma, Reliance Digital, Amazon)",
              "sellerPhone": "Store phone number if printed",
              "sellerLocation": "Store address, branch, or city if printed",
              "notes": "Invoice/bill number, tax details, or guarantee terms"
            }
            Return ONLY the valid JSON object. Do not include markdown ticks or explanation.
        """.trimIndent()

        val json = JSONObject()
        val contentsArray = JSONArray()
        val contentObject = JSONObject()
        val partsArray = JSONArray()

        // Text prompt part
        val textPart = JSONObject().apply {
            put("text", prompt)
        }
        partsArray.put(textPart)

        // Image part
        val imagePart = JSONObject().apply {
            val inlineData = JSONObject().apply {
                put("mimeType", "image/jpeg")
                put("data", base64Data)
            }
            put("inlineData", inlineData)
        }
        partsArray.put(imagePart)

        contentObject.put("parts", partsArray)
        contentsArray.put(contentObject)
        json.put("contents", contentsArray)

        val generationConfig = JSONObject().apply {
            put("temperature", 0.1)
            put("responseMimeType", "application/json")
        }
        json.put("generationConfig", generationConfig)

        return json.toString()
    }

    private fun extractTextFromGeminiResponse(responseJson: String): String? {
        return try {
            val root = JSONObject(responseJson)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val firstPart = parts.getJSONObject(0)
            firstPart.optString("text")
        } catch (e: Exception) {
            null
        }
    }

    private fun parseErrorMessage(errorBody: String): String {
        return try {
            val root = JSONObject(errorBody)
            val error = root.optJSONObject("error")
            error?.optString("message") ?: "Invalid response"
        } catch (e: Exception) {
            "Network error"
        }
    }

    internal fun parseExtractedJson(jsonText: String): ScannedBillData {
        var clean = jsonText.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json").trim()
        }
        if (clean.startsWith("```")) {
            clean = clean.removePrefix("```").trim()
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```").trim()
        }

        return try {
            val obj = JSONObject(clean)

            val rawName = obj.optString("name").takeIf { it.isNotBlank() && it != "null" }
            val rawCat = obj.optString("category").takeIf { it.isNotBlank() && it != "null" }
            val matchedCat = normalizeCategory(rawCat)

            val brand = obj.optString("brand").takeIf { it.isNotBlank() && it != "null" }
            val model = obj.optString("model").takeIf { it.isNotBlank() && it != "null" }
            val serial = obj.optString("serialNumber").takeIf { it.isNotBlank() && it != "null" }

            val rawPrice = if (obj.has("purchasePrice")) {
                obj.optDouble("purchasePrice", 0.0).takeIf { !it.isNaN() && it > 0 }
            } else null

            val dateStr = obj.optString("purchaseDate").takeIf { it.isNotBlank() && it != "null" }
            val (dateMillis, formattedDate) = parseDateString(dateStr)

            val warrantyMonths = if (obj.has("warrantyMonths")) {
                val wm = obj.optInt("warrantyMonths", -1)
                if (wm >= 0) wm else null
            } else null

            val sellerName = obj.optString("sellerName").takeIf { it.isNotBlank() && it != "null" }
            val sellerPhone = obj.optString("sellerPhone").takeIf { it.isNotBlank() && it != "null" }
            val sellerLoc = obj.optString("sellerLocation").takeIf { it.isNotBlank() && it != "null" }
            val notes = obj.optString("notes").takeIf { it.isNotBlank() && it != "null" }

            ScannedBillData(
                name = rawName,
                category = matchedCat,
                brand = brand,
                model = model,
                serialNumber = serial,
                purchasePrice = rawPrice,
                purchaseDateMillis = dateMillis,
                purchaseDateFormatted = formattedDate,
                warrantyMonths = warrantyMonths,
                sellerName = sellerName,
                sellerPhone = sellerPhone,
                sellerLocation = sellerLoc,
                notes = notes
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ScannedBillData()
        }
    }

    private fun normalizeCategory(rawCat: String?): String? {
        if (rawCat.isNullOrBlank()) return null
        for (cat in supportedCategories) {
            if (cat.equals(rawCat.trim(), ignoreCase = true)) {
                return cat
            }
        }
        // Substring matching
        val lower = rawCat.lowercase()
        return when {
            lower.contains("electronic") || lower.contains("phone") || lower.contains("mobile") ||
                    lower.contains("laptop") || lower.contains("computer") || lower.contains("tv") ||
                    lower.contains("audio") || lower.contains("headphone") || lower.contains("earphone") ||
                    lower.contains("speaker") || lower.contains("gadget") || lower.contains("camera") -> "Electronics"
            lower.contains("kitchen") || lower.contains("fridge") || lower.contains("refrigerator") ||
                    lower.contains("microwave") || lower.contains("oven") || lower.contains("cook") ||
                    lower.contains("blender") || lower.contains("chimney") -> "Kitchen"
            lower.contains("vehicle") || lower.contains("car") || lower.contains("bike") ||
                    lower.contains("scooter") || lower.contains("auto") -> "Vehicle"
            lower.contains("home") || lower.contains("furniture") || lower.contains("mattress") ||
                    lower.contains("sofa") || lower.contains("bed") || lower.contains("ac") ||
                    lower.contains("air conditioner") -> "Home"
            lower.contains("personal") || lower.contains("watch") || lower.contains("cloth") ||
                    lower.contains("wear") || lower.contains("bag") || lower.contains("shoe") -> "Personal"
            else -> "Other"
        }
    }

    private fun parseDateString(rawDate: String?): Pair<Long?, String?> {
        if (rawDate.isNullOrBlank()) return Pair(null, null)
        val formats = listOf(
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "yyyy/MM/dd",
            "dd MMM yyyy",
            "MMM dd, yyyy"
        )

        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
                val date = sdf.parse(rawDate.trim())
                if (date != null) {
                    val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    return Pair(date.time, displayFormat.format(date))
                }
            } catch (_: Exception) {
            }
        }

        return Pair(null, rawDate)
    }

    private fun loadScaledBitmap(context: Context, uri: Uri, maxDimension: Int = 1600): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            if (uri.scheme == "file") {
                val path = uri.path ?: return null
                FileInputStream(File(path)).use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
            }

            val origWidth = options.outWidth
            val origHeight = options.outHeight
            if (origWidth <= 0 || origHeight <= 0) return null

            var sampleSize = 1
            while (origWidth / sampleSize > maxDimension || origHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap = if (uri.scheme == "file") {
                val path = uri.path ?: return null
                FileInputStream(File(path)).use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                }
            } ?: return null

            val width = bitmap.width
            val height = bitmap.height
            val maxDim = maxOf(width, height)
            if (maxDim > maxDimension) {
                val scale = maxDimension.toFloat() / maxDim
                val targetW = (width * scale).toInt()
                val targetH = (height * scale).toInt()
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
