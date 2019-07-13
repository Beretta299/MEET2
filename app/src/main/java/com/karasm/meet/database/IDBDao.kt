package com.karasm.meet.database


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.karasm.meet.database.dbentities.Category
import com.karasm.meet.database.dbentities.PartyInformation
import com.karasm.meet.database.dbentities.UserEntity
import io.reactivex.Flowable
import io.reactivex.Observable
import java.util.*

@Dao
interface IDBDao {
    //-----------------------------------------------USERENTITY----------------------------------------------------------
    @Insert
    fun insertUser(userEntity: UserEntity)

    @Query("SELECT * FROM UserEntity limit 1")
    fun getUser():Flowable<UserEntity>

    @Query("SELECT EXISTS(SELECT * FROM `UserEntity`)")
    fun ifUserExist():Flowable<Int>

    @Query("DELETE FROM UserEntity")
    fun deleteUser()
    //-----------------------------------------------PARTIES------------------------------------------------------------
    @Insert
    fun insertParty(partyInformation: PartyInformation)

    @Insert
    fun insertParties(listOfParties:List<PartyInformation>)

    @Query("SELECT * FROM PartyInformation")
    fun getParties():List<PartyInformation>

    @Query("SELECT * FROM PartyInformation WHERE id=:neededId")
    fun getParty(neededId:Int):PartyInformation

    @Query("DELETE FROM PartyInformation")
    fun deleteParties()

    @Query("SELECT * FROM PartyInformation WHERE title LIKE '%2%'")
    fun getParty2():List<PartyInformation>

    @Query("SELECT * FROM PartyInformation WHERE title LIKE :queryS")
    fun getPartyQuery(queryS:String):List<PartyInformation>
    //-----------------------------------------------Categories------------------------------------------------------------
    @Insert
    fun insertCategory(category: Category)

    @Query("SELECT * FROM Category WHERE id=:neededId")
    fun getCategory(neededId: Int):Category

    @Query("SELECT * FROM Category")
    fun getCategories():Flowable<List<Category>>

    @Query("DELETE FROM Category")
    fun deleteCategories()

    @Query("SELECT EXISTS(SELECT * FROM `Category`)")
    fun ifCategoriesExist():Flowable<Int>


}