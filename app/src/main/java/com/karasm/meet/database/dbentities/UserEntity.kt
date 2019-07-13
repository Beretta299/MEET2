package com.karasm.meet.database.dbentities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class UserEntity(@PrimaryKey(autoGenerate = true) var id:Long?,
                      var name:String,
                      var dateOfBirth: String,
                      var email:String,
                      var password:String,
                      var phone:String,
                      var photo:String,
                      var about:String)