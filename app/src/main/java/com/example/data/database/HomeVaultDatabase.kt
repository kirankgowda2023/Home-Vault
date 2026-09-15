package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ItemDao
import com.example.data.model.ItemEntity
import com.example.util.WarrantyEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ItemEntity::class], version = 1, exportSchema = false)
abstract class HomeVaultDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: HomeVaultDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HomeVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HomeVaultDatabase::class.java,
                    "homevault_database.db"
                )
                    .addCallback(HomeVaultDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class HomeVaultDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val itemDao = database.itemDao()
                    if (itemDao.getItemCount() == 0) {
                        itemDao.insertAll(WarrantyEngine.getSampleSeedItems())
                    }
                }
            }
        }
    }
}
