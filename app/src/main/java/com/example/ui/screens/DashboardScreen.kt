package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ItemEntity
import com.example.data.model.WarrantyState
import com.example.ui.HomeVaultViewModel
import com.example.ui.components.FigmaWarrantyBadge
import com.example.ui.components.HomeVaultCubeIcon
import com.example.ui.components.SecurityBanner
import com.example.util.WarrantyEngine

@Composable
fun DashboardScreen(
    viewModel: HomeVaultViewModel,
    onNavigateToItemDetail: (Long) -> Unit,
    onNavigateToAddItem: () -> Unit,
    onNavigateToInventoryWithCategory: (String) -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val expiringItems by viewModel.expiringItems.collectAsStateWithLifecycle()
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val cardBorder = Color(0xFFE2E8F0)
    val darkNavy = Color(0xFF0B132B)
    val tealAccent = Color(0xFF00897B)

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Owner Name & Residence -> Encrypted pill
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (userProfile.name.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.testTag("home_user_name_row")
                                ) {
                                    Text(
                                        text = "Hello, ",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = userProfile.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0D9488),
                                        modifier = Modifier.testTag("home_user_name")
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                            } else {
                                Text(
                                    text = "My Residence",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            Text(
                                text = if (userProfile.residenceAddress.isNotBlank()) userProfile.residenceAddress else "Oakwood Condo",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.testTag("home_residence_name")
                            )
                        }

                        // Encrypted pill
                        Surface(
                            color = Color(0xFFD1FAE5),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("home_encrypted_pill")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF059669))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Encrypted",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }
                }
            }

            // Security Banner
            item {
                SecurityBanner()
            }

            // 2 Side-by-side Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Asset Value
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_asset_value")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Asset Value",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = WarrantyEngine.formatCurrency(stats.totalValuation),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stats.totalItems} Total Items",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Card 2: Warranties
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_warranties")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Warranties",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${stats.activeCount} Active",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D9488)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stats.expiringCount} Expiring Soon",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }
            }

            // Upcoming Expiry Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAlerts() }
                        .testTag("upcoming_expiry_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Upcoming Expiry",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                            Text(
                                text = "${expiringItems.size} items",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFB45309)
                            )
                        }

                        if (expiringItems.isNotEmpty()) {
                            val nearest = expiringItems.first()
                            val info = WarrantyEngine.getWarrantyInfo(nearest.purchaseDate, nearest.warrantyMonths)

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFFDE68A), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = nearest.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (info.daysRemaining <= 0) "Expired" else "Expires in ${info.daysRemaining} days",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }

            // Recent Assets Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Assets",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "View All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = tealAccent,
                        modifier = Modifier
                            .clickable { onNavigateToInventoryWithCategory("All") }
                            .testTag("view_all_assets_button")
                    )
                }
            }

            // Recent Assets List Items (White card with cube icon, details, price, badge)
            if (allItems.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No assets added yet",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                items(allItems.take(5)) { item ->
                    val info = WarrantyEngine.getWarrantyInfo(item.purchaseDate, item.warrantyMonths)

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToItemDetail(item.id) }
                            .testTag("recent_item_card_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Box
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                HomeVaultCubeIcon(
                                    modifier = Modifier.size(22.dp),
                                    tint = Color(0xFF0D9488),
                                    strokeWidth = 2f
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Name & Category
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.category,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            // Price & Badge
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = WarrantyEngine.formatCurrency(item.purchasePrice),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                FigmaWarrantyBadge(
                                    state = info.state,
                                    daysRemaining = info.daysRemaining,
                                    humanReadable = info.humanReadableRemaining
                                )
                            }
                        }
                    }
                }
            }
        }

        // Figma Capsule Floating Action Button: "+ Add Item" in Navy
        Surface(
            color = darkNavy,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp)
                .clickable { onNavigateToAddItem() }
                .testTag("dashboard_add_item_fab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Item",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
