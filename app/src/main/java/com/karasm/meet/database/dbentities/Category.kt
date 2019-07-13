package com.karasm.meet.database.dbentities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Category(@PrimaryKey(autoGenerate = true) var id:Long?, var categoryName:String)