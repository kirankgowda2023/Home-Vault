package com.example.ui.navigation

sealed class Screen {
    object Welcome : Screen()
    object Dashboard : Screen()
    object Inventory : Screen()
    object Alerts : Screen()
    object More : Screen()
    object Reports : Screen()
    object ProfileSettings : Screen()
    data class ItemDetail(val itemId: Long) : Screen()
    data class AddEditItem(val itemId: Long? = null) : Screen()
}

enum class BottomTab(val title: String) {
    HOME("Home"),
    INVENTORY("Inventory"),
    ALERTS("Alerts"),
    MORE("More")
}
