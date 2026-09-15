package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HomeVaultCubeIcon
import com.example.ui.navigation.BottomTab
import com.example.ui.navigation.Screen
import com.example.ui.screens.AddEditItemScreen
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.ItemDetailScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.WelcomeScreen

@Composable
fun HomeVaultApp(viewModel: HomeVaultViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var currentTab by remember { mutableStateOf(BottomTab.HOME) }

    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()

    val isMainTabScreen = currentScreen is Screen.Dashboard ||
        currentScreen is Screen.Inventory ||
        currentScreen is Screen.Alerts ||
        currentScreen is Screen.More

    // Handle back button
    BackHandler(enabled = !isMainTabScreen) {
        currentScreen = when (currentTab) {
            BottomTab.HOME -> Screen.Dashboard
            BottomTab.INVENTORY -> Screen.Inventory
            BottomTab.ALERTS -> Screen.Alerts
            BottomTab.MORE -> Screen.More
        }
    }

    val darkNavy = Color(0xFF0B132B)
    val cardBorder = Color(0xFFE2E8F0)
    val inactiveGrey = Color(0xFF94A3B8)
    val tealAccent = Color(0xFF0D9488)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = if (isMainTabScreen) ScaffoldDefaults.contentWindowInsets else WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isMainTabScreen) {
                Surface(
                    color = Color.White,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .testTag("main_navigation_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Home Tab
                        val isHomeSelected = currentTab == BottomTab.HOME && currentScreen is Screen.Dashboard
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    currentTab = BottomTab.HOME
                                    currentScreen = Screen.Dashboard
                                }
                                .padding(vertical = 6.dp)
                                .testTag("tab_home")
                        ) {
                            Icon(
                                imageVector = if (isHomeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home",
                                tint = if (isHomeSelected) darkNavy else inactiveGrey,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Home",
                                fontSize = 11.sp,
                                fontWeight = if (isHomeSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isHomeSelected) darkNavy else inactiveGrey
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            if (isHomeSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 16.dp, height = 2.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(tealAccent)
                                )
                            }
                        }

                        // 2. Inventory Tab (Isometric Cube Icon)
                        val isInventorySelected = currentTab == BottomTab.INVENTORY && currentScreen is Screen.Inventory
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    currentTab = BottomTab.INVENTORY
                                    currentScreen = Screen.Inventory
                                }
                                .padding(vertical = 6.dp)
                                .testTag("tab_inventory")
                        ) {
                            HomeVaultCubeIcon(
                                tint = if (isInventorySelected) darkNavy else inactiveGrey,
                                strokeWidth = if (isInventorySelected) 2.5f else 2f,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Inventory",
                                fontSize = 11.sp,
                                fontWeight = if (isInventorySelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isInventorySelected) darkNavy else inactiveGrey
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            if (isInventorySelected) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 16.dp, height = 2.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(tealAccent)
                                )
                            }
                        }

                        // 3. Alerts Tab
                        val isAlertsSelected = currentTab == BottomTab.ALERTS && currentScreen is Screen.Alerts
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    currentTab = BottomTab.ALERTS
                                    currentScreen = Screen.Alerts
                                }
                                .padding(vertical = 6.dp)
                                .testTag("tab_alerts")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (stats.expiringCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFFDC2626),
                                            contentColor = Color.White
                                        ) {
                                            Text("${stats.expiringCount}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isAlertsSelected) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                    contentDescription = "Alerts",
                                    tint = if (isAlertsSelected) darkNavy else inactiveGrey,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Alerts",
                                fontSize = 11.sp,
                                fontWeight = if (isAlertsSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAlertsSelected) darkNavy else inactiveGrey
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            if (isAlertsSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 16.dp, height = 2.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(tealAccent)
                                )
                            }
                        }

                        // 4. More Tab
                        val isMoreSelected = currentTab == BottomTab.MORE && currentScreen is Screen.More
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    currentTab = BottomTab.MORE
                                    currentScreen = Screen.More
                                }
                                .padding(vertical = 6.dp)
                                .testTag("tab_more")
                        ) {
                            Icon(
                                imageVector = if (isMoreSelected) Icons.Filled.Menu else Icons.Outlined.Menu,
                                contentDescription = "More",
                                tint = if (isMoreSelected) darkNavy else inactiveGrey,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "More",
                                fontSize = 11.sp,
                                fontWeight = if (isMoreSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isMoreSelected) darkNavy else inactiveGrey
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            if (isMoreSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 16.dp, height = 2.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(tealAccent)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                when (screen) {
                    is Screen.Welcome -> {
                        WelcomeScreen(
                            onGetStarted = {
                                currentTab = BottomTab.HOME
                                currentScreen = Screen.Dashboard
                            }
                        )
                    }

                    is Screen.Dashboard -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToItemDetail = { id -> currentScreen = Screen.ItemDetail(id) },
                            onNavigateToAddItem = { currentScreen = Screen.AddEditItem(null) },
                            onNavigateToInventoryWithCategory = { category ->
                                viewModel.setCategoryFilter(category)
                                currentTab = BottomTab.INVENTORY
                                currentScreen = Screen.Inventory
                            },
                            onNavigateToAlerts = {
                                currentTab = BottomTab.ALERTS
                                currentScreen = Screen.Alerts
                            }
                        )
                    }

                    is Screen.Inventory -> {
                        InventoryScreen(
                            viewModel = viewModel,
                            onNavigateToItemDetail = { id -> currentScreen = Screen.ItemDetail(id) },
                            onNavigateToAddItem = { currentScreen = Screen.AddEditItem(null) }
                        )
                    }

                    is Screen.Alerts -> {
                        AlertsScreen(
                            viewModel = viewModel,
                            onNavigateToItemDetail = { id -> currentScreen = Screen.ItemDetail(id) }
                        )
                    }

                    is Screen.More -> {
                        MoreScreen(
                            viewModel = viewModel,
                            onNavigateToReports = { currentScreen = Screen.Reports },
                            onNavigateToWelcome = { currentScreen = Screen.Welcome },
                            onNavigateToProfileSettings = { currentScreen = Screen.ProfileSettings }
                        )
                    }

                    is Screen.Reports -> {
                        ReportsScreen(viewModel = viewModel)
                    }

                    is Screen.ProfileSettings -> {
                        ProfileSettingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                currentScreen = Screen.More
                            }
                        )
                    }

                    is Screen.ItemDetail -> {
                        ItemDetailScreen(
                            itemId = screen.itemId,
                            viewModel = viewModel,
                            onNavigateBack = {
                                currentScreen = when (currentTab) {
                                    BottomTab.HOME -> Screen.Dashboard
                                    BottomTab.INVENTORY -> Screen.Inventory
                                    BottomTab.ALERTS -> Screen.Alerts
                                    BottomTab.MORE -> Screen.More
                                }
                            },
                            onNavigateToEdit = { id ->
                                currentScreen = Screen.AddEditItem(id)
                            }
                        )
                    }

                    is Screen.AddEditItem -> {
                        AddEditItemScreen(
                            itemId = screen.itemId,
                            viewModel = viewModel,
                            onNavigateBack = {
                                if (screen.itemId != null) {
                                    currentScreen = Screen.ItemDetail(screen.itemId)
                                } else {
                                    currentScreen = when (currentTab) {
                                        BottomTab.HOME -> Screen.Dashboard
                                        BottomTab.INVENTORY -> Screen.Inventory
                                        BottomTab.ALERTS -> Screen.Alerts
                                        BottomTab.MORE -> Screen.More
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
