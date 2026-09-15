package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HomeVaultViewModel
import com.example.ui.components.SecurityBanner
import com.example.util.FileStorageHelper

enum class ExportReportFormat(val label: String) {
    PDF("PDF Document"),
    CSV("CSV Spreadsheet"),
    JSON("JSON Backup")
}

@Composable
fun ReportsScreen(viewModel: HomeVaultViewModel) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedFormat by remember { mutableStateOf(ExportReportFormat.PDF) }
    var includeReceipts by remember { mutableStateOf(true) }
    var includeEstimatedValue by remember { mutableStateOf(true) }
    var includeSerialNumbers by remember { mutableStateOf(true) }

    val darkNavy = Color(0xFF0B132B)
    val tealAccent = Color(0xFF0D9488)
    val cardBorder = Color(0xFFE2E8F0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("reports_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Description
        item {
            Column {
                Text(
                    text = "Export Reports",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Generate locally-produced inventories for insurance claims, moving documentation, or personal secure backups.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Security Banner
        item {
            SecurityBanner()
        }

        // Choose Format
        item {
            Column {
                Text(
                    text = "Choose Format",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportReportFormat.entries.forEach { format ->
                        val isSelected = selectedFormat == format
                        Surface(
                            color = if (isSelected) darkNavy else Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) darkNavy else cardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFormat = format }
                                .testTag("format_option_${format.name}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = format.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Included Assets & Files Card
        item {
            Column {
                Text(
                    text = "Included Assets & Files",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Include Encrypted Receipts
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Include Encrypted Receipts",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = includeReceipts,
                                onCheckedChange = { includeReceipts = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = tealAccent
                                )
                            )
                        }

                        HorizontalDivider(color = cardBorder, thickness = 1.dp)

                        // Include Estimated Value Stated
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Include Estimated Value Stated",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = includeEstimatedValue,
                                onCheckedChange = { includeEstimatedValue = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = tealAccent
                                )
                            )
                        }

                        HorizontalDivider(color = cardBorder, thickness = 1.dp)

                        // Include Serial Numbers
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Include Serial Numbers",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = includeSerialNumbers,
                                onCheckedChange = { includeSerialNumbers = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = tealAccent
                                )
                            )
                        }
                    }
                }
            }
        }

        // Generate Local Report Action Button
        item {
            Button(
                onClick = {
                    if (items.isEmpty()) {
                        Toast.makeText(context, "No assets to export", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    when (selectedFormat) {
                        ExportReportFormat.PDF -> {
                            val report = FileStorageHelper.generateInventoryReportText(
                                items = items,
                                ownerName = userProfile.name,
                                mobileNumber = userProfile.mobileNumber,
                                residenceAddress = userProfile.residenceAddress
                            )
                            FileStorageHelper.shareReport(context, report)
                        }
                        ExportReportFormat.CSV -> {
                            val uri = FileStorageHelper.exportToCsv(context, items)
                            if (uri != null) {
                                FileStorageHelper.shareCsvFile(context, uri)
                            } else {
                                Toast.makeText(context, "Failed to generate CSV", Toast.LENGTH_SHORT).show()
                            }
                        }
                        ExportReportFormat.JSON -> {
                            val uri = FileStorageHelper.exportToJson(context, items)
                            if (uri != null) {
                                FileStorageHelper.shareFile(context, uri, "application/json", "HomeVault Ledger")
                            } else {
                                Toast.makeText(context, "Failed to generate JSON", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_local_report_button")
            ) {
                Text(
                    text = "Generate Local Report",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Local Processing Disclaimer
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "This report is compiled locally in-memory and downloaded straight to Files.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
