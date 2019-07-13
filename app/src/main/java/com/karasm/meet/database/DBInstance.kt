package com.karasm.meet.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.karasm.meet.database.dbentities.Category
import com.karasm.meet.database.dbentities.PartyInformation
import com.karasm.meet.database.dbentities.UserEntity

@Database(entities=arrayOf(UserEntity::class,PartyInformation::class,Category::class),version = 6)
abstract class DBInstance: RoomDatabase() {

    abstract fun dbInstanceDao():IDBDao

    companion object{

        private var INSTANCE:DBInstance?=null

        fun getInstance(context: Context):DBInstance{
                if(INSTANCE==null){
                    synchronized(DBInstance::class){
                        INSTANCE= Room.databaseBuilder(context.applicationContext,DBInstance::class.java,"meet.db")
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            return INSTANCE!!
        }

        fun destroyINSTANCE(){
            INSTANCE=null
        }
    }


}