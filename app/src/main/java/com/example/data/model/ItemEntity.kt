package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // Home, Electronics, Vehicle, Kitchen, Personal, Other
    val brand: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val purchaseDate: Long, // Epoch milliseconds
    val purchasePrice: Double = 0.0,
    val warrantyMonths: Int = 12, // 0 = No warranty
    val sellerName: String = "",
    val sellerPhone: String = "",
    val sellerLocation: String = "",
    val notes: String = "",
    val itemPhotoUri: String? = null,
    val billPhotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class WarrantyState {
    ACTIVE,
    EXPIRING_SOON,
    EXPIRED,
    NO_WARRANTY
}

data class WarrantyInfo(
    val state: WarrantyState,
    val expiryDateMillis: Long?,
    val daysRemaining: Long,
    val humanReadableRemaining: String,
    val isExpiringSoon: Boolean
)
