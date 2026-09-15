package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HomeVaultViewModel
import com.example.ui.components.SecurityBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    viewModel: HomeVaultViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var name by remember(currentProfile) { mutableStateOf(currentProfile.name) }
    var mobileNumber by remember(currentProfile) { mutableStateOf(currentProfile.mobileNumber) }
    var residenceAddress by remember(currentProfile) { mutableStateOf(currentProfile.residenceAddress) }

    val darkNavy = Color(0xFF0B132B)
    val tealAccent = Color(0xFF0D9488)
    val cardBorder = Color(0xFFE2E8F0)
    val inputTextColor = Color(0xFF0F172A)
    val inputPlaceholderColor = Color(0xFF94A3B8)
    val inputTextStyle = TextStyle(
        color = inputTextColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    )
    val profileFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = inputTextColor,
        unfocusedTextColor = inputTextColor,
        cursorColor = tealAccent,
        focusedBorderColor = tealAccent,
        unfocusedBorderColor = cardBorder,
        focusedContainerColor = Color(0xFFF8FAFC),
        unfocusedContainerColor = Color(0xFFF8FAFC),
        focusedPlaceholderColor = inputPlaceholderColor,
        unfocusedPlaceholderColor = inputPlaceholderColor,
        focusedLeadingIconColor = tealAccent,
        unfocusedLeadingIconColor = Color(0xFF64748B),
        selectionColors = TextSelectionColors(
            handleColor = tealAccent,
            backgroundColor = Color(0xFFCCFBF1)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile Settings",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("profile_settings_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security reminder banner
            item {
                SecurityBanner()
            }

            // Profile Header Preview
            item {
                Surface(
                    color = darkNavy,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().testTag("profile_preview_card")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(18.dp)
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

                        Column {
                            Text(
                                text = if (name.isNotBlank()) name else "Owner Account",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (residenceAddress.isNotBlank()) residenceAddress else "Oakwood Condo",
                                fontSize = 13.sp,
                                color = Color(0xFF2DD4BF)
                            )
                            if (mobileNumber.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mobileNumber,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            // Form card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "PERSONAL & RESIDENCE DETAILS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )

                        // 1. Name Input
                        Column {
                            Text(
                                text = "Name",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_name_input"),
                                textStyle = inputTextStyle,
                                placeholder = { Text("e.g. John Doe", color = inputPlaceholderColor) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = profileFieldColors
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Visible on your Home screen and warranty export documents",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // 2. Mobile Number Input
                        Column {
                            Text(
                                text = "Mobile Number",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = mobileNumber,
                                onValueChange = { mobileNumber = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_mobile_input"),
                                textStyle = inputTextStyle,
                                placeholder = { Text("e.g. +1 (555) 019-2834", color = inputPlaceholderColor) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = profileFieldColors
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kept locally on device for quick insurance & warranty claims",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // 3. Residence Address Input
                        Column {
                            Text(
                                text = "Residence Address",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = residenceAddress,
                                onValueChange = { residenceAddress = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_address_input"),
                                textStyle = inputTextStyle,
                                placeholder = { Text("e.g. 104 Oakwood Condo, Apt 4B", color = inputPlaceholderColor) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3,
                                colors = profileFieldColors
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Displayed as your home location on the dashboard header",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveUserProfile(name, mobileNumber, residenceAddress)
                            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_profile_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = darkNavy)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Profile",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("cancel_profile_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF64748B)
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Local privacy guarantee card
            item {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "All profile information is stored exclusively in your local device encrypted sandbox. No account creation or server sync required.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
