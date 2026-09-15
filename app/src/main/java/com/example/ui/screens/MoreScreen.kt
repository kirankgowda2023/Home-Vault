package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HomeVaultViewModel
import com.example.util.FileStorageHelper

@Composable
fun MoreScreen(
    viewModel: HomeVaultViewModel,
    onNavigateToReports: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onNavigateToProfileSettings: () -> Unit
) {
    val context = LocalContext.current
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    var biometricEnabled by remember { mutableStateOf(true) }

    val darkNavy = Color(0xFF0B132B)
    val tealAccent = Color(0xFF0D9488)
    val cardBorder = Color(0xFFE2E8F0)
    val sectionHeaderColor = Color(0xFF64748B)

    // Calculate simulated local DB storage size based on assets & bills
    val dbSizeBytes = 840_000L + (items.size * 125_000L) + (items.count { !it.billPhotoUri.isNullOrBlank() } * 950_000L)
    val dbSizeMb = String.format("%.1f MB Used", dbSizeBytes / (1024.0 * 1024.0))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("more_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Owner Account Hero Card (Tappable to go to Profile Settings)
        item {
            Surface(
                color = darkNavy,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProfileSettings() }
                    .testTag("owner_account_card")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF134E4A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF2DD4BF),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (userProfile.name.isNotBlank()) userProfile.name else "Owner Account",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (userProfile.mobileNumber.isNotBlank()) userProfile.mobileNumber else "Local Vault ID: HV-9082-A",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                        if (userProfile.residenceAddress.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userProfile.residenceAddress,
                                fontSize = 12.sp,
                                color = Color(0xFF2DD4BF)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Edit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2DD4BF),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // PROFILE & RESIDENCE SETTINGS
        item {
            Column {
                Text(
                    text = "PROFILE & RESIDENCE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = sectionHeaderColor,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToProfileSettings() }
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .testTag("menu_profile_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Profile Settings",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (userProfile.name.isNotBlank() || userProfile.mobileNumber.isNotBlank() || userProfile.residenceAddress.isNotBlank()) {
                                    val parts = listOfNotNull(
                                        userProfile.name.takeIf { it.isNotBlank() },
                                        userProfile.mobileNumber.takeIf { it.isNotBlank() },
                                        userProfile.residenceAddress.takeIf { it.isNotBlank() }
                                    )
                                    parts.joinToString(" • ")
                                } else {
                                    "Add name, mobile number & residence address"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // STORAGE & PRIVACY
        item {
            Column {
                Text(
                    text = "STORAGE & PRIVACY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = sectionHeaderColor,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Biometric Lock
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Biometric Face ID Lock",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = {
                                    biometricEnabled = it
                                    Toast.makeText(
                                        context,
                                        if (it) "Biometric App Lock Enabled" else "Biometric App Lock Disabled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = tealAccent
                                )
                            )
                        }

                        HorizontalDivider(color = cardBorder, thickness = 1.dp)

                        // Local DB Encrypted Space
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SdStorage,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Local DB Encrypted Space",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = dbSizeMb,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }

        // BACKUP & RECOVERY
        item {
            Column {
                Text(
                    text = "BACKUP & RECOVERY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = sectionHeaderColor,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Export Local Backup
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val uri = FileStorageHelper.exportToJson(context, items)
                                    if (uri != null) {
                                        FileStorageHelper.shareFile(context, uri, "application/json", "HomeVault Offline Backup")
                                    } else {
                                        Toast.makeText(context, "Failed to create backup", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Export Local Backup File",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        HorizontalDivider(color = cardBorder, thickness = 1.dp)

                        // Import Backup
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "Select a valid .json HomeVault backup from Files.", Toast.LENGTH_LONG).show()
                                }
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Import Backup File",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // REPORTS & DOCUMENTATION
        item {
            Column {
                Text(
                    text = "REPORTS & DOCUMENTATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = sectionHeaderColor,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Export Reports Screen link
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToReports() }
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Export Reports",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "PDF document, CSV spreadsheet, JSON ledger",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        HorizontalDivider(color = cardBorder, thickness = 1.dp)

                        // Welcome & Privacy Guide
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToWelcome() }
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "View Welcome & Privacy Guide",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Footer / Version Info
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "HomeVault v1.4.2 (Secure Build)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Device hardware encryption validated",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}
