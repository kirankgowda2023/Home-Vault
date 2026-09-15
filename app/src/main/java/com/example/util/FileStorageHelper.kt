package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ItemEntity
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileStorageHelper {

    fun saveMediaToInternalStorage(context: Context, sourceUri: Uri, subFolder: String): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) return null

            val folder = File(context.filesDir, subFolder).apply {
                if (!exists()) mkdirs()
            }

            val fileName = "${subFolder}_${System.currentTimeMillis()}.jpg"
            val destFile = File(folder, fileName)

            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportToCsv(context: Context, items: List<ItemEntity>): Uri? {
        return try {
            val cacheFolder = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }
            val dateTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val csvFile = File(cacheFolder, "HomeVault_Inventory_$dateTag.csv")

            csvFile.printWriter().use { writer ->
                // Header
                writer.println("Item Name,Category,Brand,Model,Serial Number,Purchase Date,Purchase Price,Warranty Months,Warranty Status,Expiry Date,Seller,Seller Phone,Seller Location,Has Bill,Notes")
                for (item in items) {
                    val warranty = WarrantyEngine.getWarrantyInfo(item.purchaseDate, item.warrantyMonths)
                    val purchaseDateStr = WarrantyEngine.formatDate(item.purchaseDate)
                    val expiryDateStr = warranty.expiryDateMillis?.let { WarrantyEngine.formatDate(it) } ?: "N/A"
                    val hasBill = if (!item.billPhotoUri.isNullOrBlank()) "YES" else "NO"

                    val line = listOf(
                        escapeCsv(item.name),
                        escapeCsv(item.category),
                        escapeCsv(item.brand),
                        escapeCsv(item.model),
                        escapeCsv(item.serialNumber),
                        escapeCsv(purchaseDateStr),
                        item.purchasePrice.toString(),
                        item.warrantyMonths.toString(),
                        warranty.state.name,
                        escapeCsv(expiryDateStr),
                        escapeCsv(item.sellerName),
                        escapeCsv(item.sellerPhone),
                        escapeCsv(item.sellerLocation),
                        hasBill,
                        escapeCsv(item.notes)
                    ).joinToString(",")
                    writer.println(line)
                }
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                csvFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateInventoryReportText(
        items: List<ItemEntity>,
        ownerName: String = "",
        mobileNumber: String = "",
        residenceAddress: String = ""
    ): String {
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("       HOMEVAULT INVENTORY REPORT         ")
        sb.appendLine("   Your Home. Your Assets. Your Warranties.")
        sb.appendLine("==========================================")
        if (ownerName.isNotBlank()) {
            sb.appendLine("Owner Name: $ownerName")
        }
        if (mobileNumber.isNotBlank()) {
            sb.appendLine("Mobile No:  $mobileNumber")
        }
        if (residenceAddress.isNotBlank()) {
            sb.appendLine("Residence:  $residenceAddress")
        }
        sb.appendLine("Generated:  $dateStr")
        sb.appendLine("Storage:    100% Offline & Private (On-device)")
        sb.appendLine()

        val totalValue = items.sumOf { it.purchasePrice }
        val categoryGroups = items.groupBy { it.category }

        sb.appendLine("--- VALUATION BY CATEGORY ---")
        categoryGroups.forEach { (category, categoryItems) ->
            val catTotal = categoryItems.sumOf { it.purchasePrice }
            sb.appendLine("• $category (${categoryItems.size} items): ${WarrantyEngine.formatCurrency(catTotal)}")
        }
        sb.appendLine("------------------------------------------")
        sb.appendLine("TOTAL INVENTORY VALUE: ${WarrantyEngine.formatCurrency(totalValue)}")
        sb.appendLine("TOTAL ITEMS OWNED: ${items.size}")
        val billsCount = items.count { !it.billPhotoUri.isNullOrBlank() }
        sb.appendLine("BILLS SECURED: $billsCount / ${items.size}")
        sb.appendLine("==========================================")
        sb.appendLine()

        sb.appendLine("--- ITEMIZED ASSET LEDGER ---")
        categoryGroups.forEach { (category, categoryItems) ->
            sb.appendLine()
            sb.appendLine("[$category.toUpperCase(Locale.ROOT)]")
            categoryItems.forEachIndexed { index, item ->
                val warranty = WarrantyEngine.getWarrantyInfo(item.purchaseDate, item.warrantyMonths)
                val purchaseDateStr = WarrantyEngine.formatDate(item.purchaseDate)
                val expiryStr = warranty.expiryDateMillis?.let { WarrantyEngine.formatDate(it) } ?: "None"
                val billStatus = if (!item.billPhotoUri.isNullOrBlank()) "✓ Bill on file" else "No bill attached"

                sb.appendLine("${index + 1}. ${item.name}")
                if (item.brand.isNotBlank() || item.model.isNotBlank()) {
                    sb.appendLine("   Brand/Model: ${listOf(item.brand, item.model).filter { it.isNotBlank() }.joinToString(" ")}")
                }
                if (item.serialNumber.isNotBlank()) {
                    sb.appendLine("   Serial No: ${item.serialNumber}")
                }
                sb.appendLine("   Purchase: ${WarrantyEngine.formatCurrency(item.purchasePrice)} on $purchaseDateStr")
                sb.appendLine("   Warranty: ${warranty.state.name} (${warranty.humanReadableRemaining}, Expires: $expiryStr)")
                if (item.sellerName.isNotBlank()) {
                    sb.appendLine("   Seller: ${item.sellerName} ${if (item.sellerPhone.isNotBlank()) "(${item.sellerPhone})" else ""}")
                }
                sb.appendLine("   Documents: $billStatus")
                if (item.notes.isNotBlank()) {
                    sb.appendLine("   Notes: ${item.notes}")
                }
                sb.appendLine()
            }
        }
        sb.appendLine("==========================================")
        sb.appendLine("End of HomeVault Report.")
        return sb.toString()
    }

    fun shareReport(context: Context, reportText: String, subject: String = "HomeVault Inventory Report") {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, reportText)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Inventory Report")
        context.startActivity(shareIntent)
    }

    fun shareCsvFile(context: Context, csvUri: Uri) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, csvUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Inventory CSV")
        context.startActivity(shareIntent)
    }

    fun exportToJson(context: Context, items: List<ItemEntity>): Uri? {
        return try {
            val cacheFolder = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }
            val dateTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val jsonFile = File(cacheFolder, "HomeVault_Backup_$dateTag.json")

            val jsonArray = StringBuilder()
            jsonArray.append("[\n")
            items.forEachIndexed { index, item ->
                jsonArray.append("  {\n")
                jsonArray.append("    \"id\": ${item.id},\n")
                jsonArray.append("    \"name\": \"${escapeJson(item.name)}\",\n")
                jsonArray.append("    \"category\": \"${escapeJson(item.category)}\",\n")
                jsonArray.append("    \"brand\": \"${escapeJson(item.brand)}\",\n")
                jsonArray.append("    \"model\": \"${escapeJson(item.model)}\",\n")
                jsonArray.append("    \"serialNumber\": \"${escapeJson(item.serialNumber)}\",\n")
                jsonArray.append("    \"purchaseDate\": ${item.purchaseDate},\n")
                jsonArray.append("    \"purchasePrice\": ${item.purchasePrice},\n")
                jsonArray.append("    \"warrantyMonths\": ${item.warrantyMonths},\n")
                jsonArray.append("    \"sellerName\": \"${escapeJson(item.sellerName)}\",\n")
                jsonArray.append("    \"notes\": \"${escapeJson(item.notes)}\"\n")
                jsonArray.append("  }${if (index < items.size - 1) "," else ""}\n")
            }
            jsonArray.append("]")

            jsonFile.writeText(jsonArray.toString())

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                jsonFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String, title: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }

    private fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun backupToGoogleDrive(context: Context, items: List<ItemEntity>): Boolean {
        return try {
            val csvUri = exportToCsv(context, items) ?: return false
            val driveIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, csvUri)
                putExtra(Intent.EXTRA_SUBJECT, "HomeVault_Backup_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                `package` = "com.google.android.apps.docs"
            }
            try {
                context.startActivity(driveIntent)
            } catch (e: Exception) {
                // If Google Drive package isn't directly launchable, open Android system chooser with Drive option
                val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, csvUri)
                    putExtra(Intent.EXTRA_SUBJECT, "HomeVault Inventory Backup (Google Drive)")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }, "Save Backup to Google Drive")
                context.startActivity(chooser)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun openGoogleDriveLink(context: Context) {
        try {
            val driveIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(driveIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun escapeCsv(value: String): String {
        val sanitized = value.replace("\"", "\"\"")
        return if (sanitized.contains(",") || sanitized.contains("\n") || sanitized.contains("\"")) {
            "\"$sanitized\""
        } else {
            sanitized
        }
    }
}
