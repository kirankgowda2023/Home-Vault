package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.HomeVaultDatabase
import com.example.data.model.ItemEntity
import com.example.data.model.UserProfile
import com.example.data.model.WarrantyState
import com.example.data.repository.HomeVaultRepository
import com.example.util.BillScanResult
import com.example.util.BillScannerHelper
import com.example.util.FileStorageHelper
import com.example.util.NotificationHelper
import com.example.util.WarrantyEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption {
    PURCHASE_DATE_DESC,
    PURCHASE_DATE_ASC,
    PRICE_DESC,
    PRICE_ASC,
    EXPIRY_DATE_ASC,
    NAME_ASC
}

data class DashboardStats(
    val totalItems: Int = 0,
    val totalValuation: Double = 0.0,
    val expiringCount: Int = 0,
    val expiredCount: Int = 0,
    val activeCount: Int = 0,
    val billsStoredCount: Int = 0,
    val categoryCounts: Map<String, Int> = emptyMap(),
    val categoryValuations: Map<String, Double> = emptyMap()
)

class HomeVaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeVaultRepository

    init {
        val db = HomeVaultDatabase.getDatabase(application, viewModelScope)
        repository = HomeVaultRepository(db.itemDao())
        NotificationHelper.createNotificationChannel(application)
    }

    val allItems: StateFlow<List<ItemEntity>> = repository.allItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("All")
    val selectedStatusFilter = _selectedStatusFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.PURCHASE_DATE_DESC)
    val sortOption = _sortOption.asStateFlow()

    val filteredItems: StateFlow<List<ItemEntity>> = combine(
        allItems,
        _searchQuery,
        _selectedCategoryFilter,
        _selectedStatusFilter,
        _sortOption
    ) { items, query, category, status, sort ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.name.contains(query, ignoreCase = true) ||
                item.brand.contains(query, ignoreCase = true) ||
                item.model.contains(query, ignoreCase = true) ||
                item.serialNumber.contains(query, ignoreCase = true) ||
                item.sellerName.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || item.category.equals(category, ignoreCase = true)

            val warrantyInfo = WarrantyEngine.getWarrantyInfo(item.purchaseDate, item.warrantyMonths)
            val matchesStatus = when (status) {
                "All" -> true
                "Active" -> warrantyInfo.state == WarrantyState.ACTIVE
                "Expiring Soon" -> warrantyInfo.state == WarrantyState.EXPIRING_SOON
                "Expired" -> warrantyInfo.state == WarrantyState.EXPIRED
                "Has Bill" -> !item.billPhotoUri.isNullOrBlank()
                "Missing Bill" -> item.billPhotoUri.isNullOrBlank()
                else -> true
            }

            matchesQuery && matchesCategory && matchesStatus
        }.sortedWith(
            when (sort) {
                SortOption.PURCHASE_DATE_DESC -> compareByDescending { it.purchaseDate }
                SortOption.PURCHASE_DATE_ASC -> compareBy { it.purchaseDate }
                SortOption.PRICE_DESC -> compareByDescending { it.purchasePrice }
                SortOption.PRICE_ASC -> compareBy { it.purchasePrice }
                SortOption.NAME_ASC -> compareBy { it.name.lowercase() }
                SortOption.EXPIRY_DATE_ASC -> compareBy {
                    WarrantyEngine.calculateExpiryDate(it.purchaseDate, it.warrantyMonths) ?: Long.MAX_VALUE
                }
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dashboardStats: StateFlow<DashboardStats> = allItems.combine(MutableStateFlow(Unit)) { items, _ ->
        var totalValue = 0.0
        var expiring = 0
        var expired = 0
        var active = 0
        var bills = 0
        val catCounts = mutableMapOf<String, Int>()
        val catValuations = mutableMapOf<String, Double>()

        for (item in items) {
            totalValue += item.purchasePrice
            if (!item.billPhotoUri.isNullOrBlank()) {
                bills++
            }
            val cat = item.category.ifBlank { "Other" }
            catCounts[cat] = (catCounts[cat] ?: 0) + 1
            catValuations[cat] = (catValuations[cat] ?: 0.0) + item.purchasePrice

            val warranty = WarrantyEngine.getWarrantyInfo(item.purchaseDate, item.warrantyMonths)
            when (warranty.state) {
                WarrantyState.EXPIRING_SOON -> expiring++
                WarrantyState.EXPIRED -> expired++
                WarrantyState.ACTIVE -> active++
                WarrantyState.NO_WARRANTY -> {}
            }
        }

        DashboardStats(
            totalItems = items.size,
            totalValuation = totalValue,
            expiringCount = expiring,
            expiredCount = expired,
            activeCount = active,
            billsStoredCount = bills,
            categoryCounts = catCounts,
            categoryValuations = catValuations
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )

    val expiringItems: StateFlow<List<ItemEntity>> = allItems.combine(MutableStateFlow(Unit)) { items, _ ->
        items.filter { item ->
            val w = WarrantyEngine.getWarrantyInfo(item.purchaseDate, item.warrantyMonths)
            w.state == WarrantyState.EXPIRING_SOON || w.state == WarrantyState.EXPIRED
        }.sortedBy {
            WarrantyEngine.calculateExpiryDate(it.purchaseDate, it.warrantyMonths) ?: Long.MAX_VALUE
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setStatusFilter(status: String) {
        _selectedStatusFilter.value = status
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun insertItem(item: ItemEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insertItem(item)
            onComplete?.invoke(id)
        }
    }

    fun updateItem(item: ItemEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.updateItem(item)
            onComplete?.invoke()
        }
    }

    fun deleteItem(item: ItemEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteItem(item)
            onComplete?.invoke()
        }
    }

    fun saveMediaFile(uri: Uri, folder: String): String? {
        return FileStorageHelper.saveMediaToInternalStorage(getApplication(), uri, folder)
    }

    fun scanBillFromUri(
        uri: Uri,
        onComplete: (savedBillPath: String?, result: BillScanResult) -> Unit
    ) {
        viewModelScope.launch {
            val savedPath = saveMediaFile(uri, "bills")
            val result = BillScannerHelper.scanBillFromUri(getApplication(), uri)
            onComplete(savedPath, result)
        }
    }

    fun scanBillFromFile(
        filePath: String,
        onComplete: (result: BillScanResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = BillScannerHelper.scanBillFromFilePath(getApplication(), filePath)
            onComplete(result)
        }
    }

    fun triggerTestNotification(item: ItemEntity? = null) {
        val targetItem = item ?: expiringItems.value.firstOrNull() ?: allItems.value.firstOrNull()
        if (targetItem != null) {
            val warranty = WarrantyEngine.getWarrantyInfo(targetItem.purchaseDate, targetItem.warrantyMonths)
            val message = if (warranty.state == WarrantyState.EXPIRING_SOON) {
                "Your ${targetItem.name} warranty expires in ${warranty.daysRemaining} days. Keep your bill ready!"
            } else {
                "Warranty check for ${targetItem.name}: ${warranty.humanReadableRemaining}"
            }
            NotificationHelper.sendWarrantyAlertNotification(
                getApplication(),
                targetItem.id.toInt(),
                targetItem.name,
                message
            )
        }
    }

    private val prefs = getApplication<Application>().getSharedPreferences("homevault_profile_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadUserProfile(): UserProfile {
        return UserProfile(
            name = prefs.getString("profile_name", "") ?: "",
            mobileNumber = prefs.getString("profile_mobile", "") ?: "",
            residenceAddress = prefs.getString("profile_address", "") ?: ""
        )
    }

    fun saveUserProfile(name: String, mobileNumber: String, residenceAddress: String) {
        val trimmedName = name.trim()
        val trimmedMobile = mobileNumber.trim()
        val trimmedAddress = residenceAddress.trim()

        prefs.edit()
            .putString("profile_name", trimmedName)
            .putString("profile_mobile", trimmedMobile)
            .putString("profile_address", trimmedAddress)
            .apply()

        _userProfile.value = UserProfile(
            name = trimmedName,
            mobileNumber = trimmedMobile,
            residenceAddress = trimmedAddress
        )
    }
}
