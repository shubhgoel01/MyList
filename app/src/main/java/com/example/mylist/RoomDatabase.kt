package com.example.mylist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

@Entity(tableName="Items_table")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    var name:String,
    var quantity:String,
    var isEditing:Boolean=false
)

@Dao
interface ItemDao
{
    @Insert
    suspend fun insert(item:Item)

    @Delete
    suspend fun delete(item:Item)

    @Update
    suspend fun update(item:Item)

    @Query("Select *from Items_table")
    fun getAllData(): LiveData<List<Item>> //should not be a suspend function because need to talk to ui, whenever it is updated, ui should be updated and must run in main thread
}

@Database(entities=[Item::class],version= 1,exportSchema=false)
abstract class ItemDatabase: RoomDatabase(){
    abstract fun itemDao():ItemDao

    companion object {
        @Volatile
        private var INSTANCE: ItemDatabase? = null

        //WHILE CREATING A DATABASE WE NEED TO PASS THE CONTEXT OF THE APPLICATION
        //so that only one instance of database is there for the whole application
        fun getDatabase(context: Context): ItemDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ItemDatabase::class.java,
                    "item_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ItemRepository(private val itemDao: ItemDao) {
    val allItems: LiveData<List<Item>> = itemDao.getAllData()

    suspend fun insert(item: Item) {
        itemDao.insert(item)
    }

    suspend fun update(item: Item) {
        itemDao.update(item)
    }

    suspend fun delete(item: Item) {
        itemDao.delete(item)
    }
}
