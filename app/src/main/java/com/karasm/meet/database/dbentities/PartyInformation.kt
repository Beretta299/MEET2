package com.karasm.meet.database.dbentities



import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity
data class PartyInformation(@PrimaryKey(autoGenerate = true) var id:Long?,
                            var title:String,
                            var current:Int,
                            var total:Int,
                            var description:String,
                            var address:String,
                            var categories:String,
                            var price:Int,
                            var priceInfo:String,
                            var date: String,
                            var time: String,
                            var images:String,
                            var promo:String,
                            var authorId:Int):Serializable