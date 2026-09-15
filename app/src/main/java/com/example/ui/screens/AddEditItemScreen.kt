package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.ItemEntity
import com.example.ui.HomeVaultViewModel
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.BrandEmeraldPrimary
import com.example.util.BillScanResult
import com.example.util.WarrantyEngine
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    itemId: Long?,
    viewModel: HomeVaultViewModel,
    onNavigateBack: () -> Unit
) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val existingItem = remember(itemId, items) { items.find { it.id == itemId } }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var name by remember(existingItem) { mutableStateOf(existingItem?.name ?: "") }
    var category by remember(existingItem) { mutableStateOf(existingItem?.category ?: "Electronics") }
    var brand by remember(existingItem) { mutableStateOf(existingItem?.brand ?: "") }
    var model by remember(existingItem) { mutableStateOf(existingItem?.model ?: "") }
    var serialNumber by remember(existingItem) { mutableStateOf(existingItem?.serialNumber ?: "") }
    var purchasePriceStr by remember(existingItem) {
        mutableStateOf(existingItem?.purchasePrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
    }
    var purchaseDateMillis by remember(existingItem) {
        mutableLongStateOf(existingItem?.purchaseDate ?: System.currentTimeMillis())
    }
    var warrantyMonths by remember(existingItem) {
        mutableIntStateOf(existingItem?.warrantyMonths ?: 12)
    }
    var sellerName by remember(existingItem) { mutableStateOf(existingItem?.sellerName ?: "") }
    var sellerPhone by remember(existingItem) { mutableStateOf(existingItem?.sellerPhone ?: "") }
    var sellerLocation by remember(existingItem) { mutableStateOf(existingItem?.sellerLocation ?: "") }
    var notes by remember(existingItem) { mutableStateOf(existingItem?.notes ?: "") }
    var itemPhotoPath by remember(existingItem) { mutableStateOf(existingItem?.itemPhotoUri) }
    var billPhotoPath by remember(existingItem) { mutableStateOf(existingItem?.billPhotoUri) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showOptionalFields by remember { mutableStateOf(existingItem != null) }

    var isScanningBill by remember { mutableStateOf(false) }
    var scanStatusMessage by remember { mutableStateOf<String?>(null) }
    var scanErrorMessage by remember { mutableStateOf<String?>(null) }
    var scanSuccessMessage by remember { mutableStateOf<String?>(null) }

    fun processScannedResult(result: BillScanResult) {
        when (result) {
            is BillScanResult.Success -> {
                val data = result.data
                var filledCount = 0
                data.name?.let { if (it.isNotBlank()) { name = it; filledCount++ } }
                data.category?.let { if (it.isNotBlank()) { category = it; filledCount++ } }
                data.brand?.let { if (it.isNotBlank()) { brand = it; filledCount++ } }
                data.model?.let { if (it.isNotBlank()) { model = it; filledCount++ } }
                data.serialNumber?.let { if (it.isNotBlank()) { serialNumber = it; filledCount++ } }
                data.purchasePrice?.let {
                    if (it > 0) {
                        purchasePriceStr = if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
                        filledCount++
                    }
                }
                data.purchaseDateMillis?.let {
                    purchaseDateMillis = it
                    filledCount++
                }
                data.warrantyMonths?.let {
                    warrantyMonths = it
                    filledCount++
                }
                data.sellerName?.let { if (it.isNotBlank()) { sellerName = it; filledCount++ } }
                data.sellerPhone?.let { if (it.isNotBlank()) { sellerPhone = it; filledCount++ } }
                data.sellerLocation?.let { if (it.isNotBlank()) { sellerLocation = it; filledCount++ } }
                data.notes?.let { if (it.isNotBlank()) { notes = it; filledCount++ } }

                val hasOptional = brand.isNotBlank() || model.isNotBlank() || serialNumber.isNotBlank() ||
                        sellerName.isNotBlank() || sellerPhone.isNotBlank() || sellerLocation.isNotBlank() || notes.isNotBlank()
                if (hasOptional) {
                    showOptionalFields = true
                }

                scanSuccessMessage = if (filledCount > 0) {
                    "✓ Auto-filled $filledCount product details from bill!"
                } else {
                    "Bill attached! Details could not be parsed automatically; you can enter them manually."
                }
            }
            is BillScanResult.Error -> {
                scanErrorMessage = if (result.isApiKeyMissing) {
                    "Gemini API key needed. Configure GEMINI_API_KEY in Secrets panel for AI bill scanning."
                } else {
                    "Could not extract details (${result.message}). Please fill fields manually."
                }
            }
        }
    }

    val scanBillPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isScanningBill = true
            scanErrorMessage = null
            scanSuccessMessage = null
            scanStatusMessage = "Analyzing bill with Gemini AI..."

            viewModel.scanBillFromUri(uri) { savedPath, result ->
                isScanningBill = false
                if (savedPath != null) {
                    billPhotoPath = savedPath
                }
                processScannedResult(result)
            }
        }
    }

    fun rescanAttachedBill() {
        focusManager.clearFocus()
        keyboardController?.hide()
        val currentPath = billPhotoPath ?: return
        isScanningBill = true
        scanErrorMessage = null
        scanSuccessMessage = null
        scanStatusMessage = "Re-analyzing attached bill..."
        viewModel.scanBillFromFile(currentPath) { result ->
            isScanningBill = false
            processScannedResult(result)
        }
    }

    val categories = listOf("Home", "Electronics", "Vehicle", "Kitchen", "Personal", "Other")

    // Photo pickers
    val itemPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val saved = viewModel.saveMediaFile(uri, "photos")
            if (saved != null) itemPhotoPath = saved
        }
    }

    val billPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val saved = viewModel.saveMediaFile(uri, "bills")
            if (saved != null) billPhotoPath = saved
        }
    }

    // DatePicker
    val calendar = Calendar.getInstance().apply { timeInMillis = purchaseDateMillis }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            purchaseDateMillis = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val performSave: () -> Unit = {
        focusManager.clearFocus()
        keyboardController?.hide()
        if (name.isBlank()) {
            Toast.makeText(context, "Please enter an item name (Required)", Toast.LENGTH_SHORT).show()
        } else {
            val price = purchasePriceStr.toDoubleOrNull() ?: 0.0

            if (existingItem != null) {
                viewModel.updateItem(
                    existingItem.copy(
                        name = name.trim(),
                        category = category,
                        brand = brand.trim(),
                        model = model.trim(),
                        serialNumber = serialNumber.trim(),
                        purchasePrice = price,
                        purchaseDate = purchaseDateMillis,
                        warrantyMonths = warrantyMonths,
                        sellerName = sellerName.trim(),
                        sellerPhone = sellerPhone.trim(),
                        sellerLocation = sellerLocation.trim(),
                        notes = notes.trim(),
                        itemPhotoUri = itemPhotoPath,
                        billPhotoUri = billPhotoPath,
                        updatedAt = System.currentTimeMillis()
                    )
                ) {
                    Toast.makeText(context, "Asset updated successfully!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            } else {
                viewModel.insertItem(
                    ItemEntity(
                        name = name.trim(),
                        category = category,
                        brand = brand.trim(),
                        model = model.trim(),
                        serialNumber = serialNumber.trim(),
                        purchasePrice = price,
                        purchaseDate = purchaseDateMillis,
                        warrantyMonths = warrantyMonths,
                        sellerName = sellerName.trim(),
                        sellerPhone = sellerPhone.trim(),
                        sellerLocation = sellerLocation.trim(),
                        notes = notes.trim(),
                        itemPhotoUri = itemPhotoPath,
                        billPhotoUri = billPhotoPath
                    )
                ) {
                    Toast.makeText(context, "Asset secured in HomeVault!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingItem != null) "Edit Asset" else "Add New Asset",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("add_item_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("add_item_back_button")
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(Color(0xFFF8FAFC))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("add_edit_item_screen")
        ) {
            // Security Banner
            com.example.ui.components.SecurityBanner(modifier = Modifier.padding(bottom = 16.dp))
            // Smart Bill Scanner (Optional)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("smart_bill_scanner_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BrandEmeraldPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = BrandEmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "SCAN BILL TO AUTO-FILL",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Optional • Powered by Gemini AI",
                                    fontSize = 11.sp,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "OPTIONAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Take or pick a photo of your receipt or tax invoice. Product name, brand, model, price, date, warranty, and seller will be filled automatically.",
                        fontSize = 12.sp,
                        color = Color(0xFF374151),
                        lineHeight = 16.sp
                    )

                    // Scanning indicator
                    if (isScanningBill) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.5.dp,
                                        color = BrandEmeraldPrimary
                                    )
                                    Text(
                                        text = scanStatusMessage ?: "Scanning invoice with Gemini AI...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF166534)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = BrandEmeraldPrimary,
                                    trackColor = Color(0xFFDCFCE7)
                                )
                            }
                        }
                    }

                    // Success Message
                    if (scanSuccessMessage != null && !isScanningBill) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF166534),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = scanSuccessMessage!!,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF166534)
                                    )
                                }
                                IconButton(
                                    onClick = { scanSuccessMessage = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFF166534),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Error or Info Message
                    if (scanErrorMessage != null && !isScanningBill) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = scanErrorMessage!!,
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                                IconButton(
                                    onClick = { scanErrorMessage = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFF92400E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                scanBillPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isScanningBill,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandEmeraldPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("scan_bill_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (billPhotoPath != null) "Scan Another Bill" else "Scan Bill / Invoice",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Direct Upload Bill Button
                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                billPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("quick_upload_bill_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (billPhotoPath != null) "Replace Bill" else "Upload Bill",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (billPhotoPath != null) {
                            OutlinedButton(
                                onClick = { rescanAttachedBill() },
                                enabled = !isScanningBill,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandEmeraldPrimary),
                                border = BorderStroke(1.dp, BrandEmeraldPrimary),
                                modifier = Modifier
                                    .height(46.dp)
                                    .testTag("rescan_current_bill_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Re-scan",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Required Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CORE ASSET INFO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Item Name *
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Item Name *") },
                        placeholder = { Text("e.g. Samsung Refrigerator, iPhone 15") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Selector *
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            categoryDropdownExpanded = !categoryDropdownExpanded
                        }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category *") },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(category),
                                    contentDescription = null,
                                    tint = BrandEmeraldPrimary
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("category_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getCategoryIcon(cat),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        category = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Purchase Price *
                    OutlinedTextField(
                        value = purchasePriceStr,
                        onValueChange = { purchasePriceStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Purchase Price (₹) *") },
                        placeholder = { Text("e.g. 48000") },
                        leadingIcon = {
                            Text(
                                text = "₹",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandEmeraldPrimary,
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("purchase_price_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Purchase Date *
                    OutlinedTextField(
                        value = WarrantyEngine.formatDate(purchaseDateMillis),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Purchase Date *") },
                        trailingIcon = {
                            IconButton(onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                datePickerDialog.show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Pick Date",
                                    tint = BrandEmeraldPrimary
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                datePickerDialog.show()
                            }
                            .testTag("purchase_date_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Core Bill / Invoice Upload Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BILL / INVOICE PHOTO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        if (billPhotoPath != null) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "ATTACHED ✓",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (billPhotoPath == null) {
                        // Core Upload Bill Dropzone / Action
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    billPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("upload_bill_dropzone")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = BrandEmeraldPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = BrandEmeraldPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Upload Bill / Invoice",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Tap to choose invoice or bill photo",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        billPhotoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmeraldPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("core_upload_bill_button")
                                ) {
                                    Text("Upload", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Attached bill preview inside Core Asset Info
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("core_bill_preview")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val billFile = File(billPhotoPath!!)
                                if (billFile.exists()) {
                                    AsyncImage(
                                        model = billFile,
                                        contentDescription = "Attached Bill Preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFE2E8F0),
                                        modifier = Modifier.size(54.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Receipt,
                                                contentDescription = null,
                                                tint = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Invoice / Bill Attached",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Saved securely on device",
                                        fontSize = 11.sp,
                                        color = BrandEmeraldPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "Change Bill",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandEmeraldPrimary,
                                            modifier = Modifier
                                                .clickable {
                                                    billPhotoPicker.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                }
                                                .testTag("replace_bill_button")
                                        )
                                        Text("•", fontSize = 12.sp, color = Color.Gray)
                                        Text(
                                            text = "Remove",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEF4444),
                                            modifier = Modifier
                                                .clickable { billPhotoPath = null }
                                                .testTag("remove_bill_button")
                                        )
                                    }
                                }
                            }
                        }

                        if (!isScanningBill) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { rescanAttachedBill() }
                                    .testTag("scan_from_attached_bill_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = BrandEmeraldPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Auto-fill details from this bill with AI",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Warranty Engine Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WARRANTY PERIOD *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val warrantyPresets = listOf(
                        0 to "None",
                        6 to "6 Mos",
                        12 to "1 Year",
                        24 to "2 Years",
                        36 to "3 Years",
                        60 to "5 Years"
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(warrantyPresets) { (months, label) ->
                            val isSelected = warrantyMonths == months
                            FilterChip(
                                selected = isSelected,
                                onClick = { warrantyMonths = months },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandEmeraldPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("warranty_chip_$months")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val previewExpiry = WarrantyEngine.calculateExpiryDate(purchaseDateMillis, warrantyMonths)
                    if (previewExpiry != null) {
                        val previewInfo = WarrantyEngine.getWarrantyInfo(purchaseDateMillis, warrantyMonths)
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Expires on: ${WarrantyEngine.formatDate(previewExpiry)} (${previewInfo.humanReadableRemaining})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No warranty reminder will be calculated.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Product Photo (Optional) Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("item_photo_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRODUCT / ASSET PHOTO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "OPTIONAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (itemPhotoPath == null) {
                        OutlinedButton(
                            onClick = {
                                itemPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("attach_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Upload Product Photo (Optional)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val itemFile = File(itemPhotoPath!!)
                                if (itemFile.exists()) {
                                    AsyncImage(
                                        model = itemFile,
                                        contentDescription = "Item Photo Preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(8.dp))
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Product Photo Attached",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "Change",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandEmeraldPrimary,
                                            modifier = Modifier.clickable {
                                                itemPhotoPicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                        )
                                        Text("•", fontSize = 12.sp, color = Color.Gray)
                                        Text(
                                            text = "Remove",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEF4444),
                                            modifier = Modifier.clickable { itemPhotoPath = null }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Optional Details Toggle
            OutlinedButton(
                onClick = { showOptionalFields = !showOptionalFields },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (showOptionalFields) "Hide Optional Details ▲" else "Add Optional Details (Brand, Serial, Seller) ▼",
                    fontSize = 13.sp
                )
            }

            if (showOptionalFields) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "OPTIONAL METADATA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = brand,
                                onValueChange = { brand = it },
                                label = { Text("Brand") },
                                placeholder = { Text("e.g. Samsung") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = model,
                                onValueChange = { model = it },
                                label = { Text("Model") },
                                placeholder = { Text("e.g. RT42...") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = serialNumber,
                            onValueChange = { serialNumber = it },
                            label = { Text("Serial Number") },
                            placeholder = { Text("e.g. SN123456789") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = sellerName,
                            onValueChange = { sellerName = it },
                            label = { Text("Seller / Retailer") },
                            placeholder = { Text("e.g. Croma, Amazon, Reliance") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = sellerPhone,
                                onValueChange = { sellerPhone = it },
                                label = { Text("Seller Phone") },
                                placeholder = { Text("+91 98...") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sellerLocation,
                                onValueChange = { sellerLocation = it },
                                label = { Text("Location") },
                                placeholder = { Text("City/Store") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes / Service Records") },
                            placeholder = { Text("e.g. Compressor warranty 10 years, service coupon...") },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Submit & Secure Action at End of Form
            Button(
                onClick = performSave,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B132B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_item_button")
            ) {
                Text(
                    text = if (existingItem != null) "Save Changes" else "Securely Save Asset",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Cancel Action
            OutlinedButton(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onNavigateBack()
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("cancel_item_button")
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
