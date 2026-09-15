package com.example.data.repository

import com.example.data.dao.ItemDao
import com.example.data.model.ItemEntity
import kotlinx.coroutines.flow.Flow

class HomeVaultRepository(private val itemDao: ItemDao) {

    val allItems: Flow<List<ItemEntity>> = itemDao.getAllItems()

    fun getItemById(id: Long): Flow<ItemEntity?> = itemDao.getItemById(id)

    suspend fun getItemByIdOnce(id: Long): ItemEntity? = itemDao.getItemByIdOnce(id)

    suspend fun insertItem(item: ItemEntity): Long = itemDao.insertItem(item)

    suspend fun updateItem(item: ItemEntity) = itemDao.updateItem(item)

    suspend fun deleteItem(item: ItemEntity) = itemDao.deleteItem(item)

    suspend fun deleteItemById(id: Long) = itemDao.deleteItemById(id)

    suspend fun insertAll(items: List<ItemEntity>) = itemDao.insertAll(items)
}
