package com.karasm.meet.server_connect

import com.karasm.meet.database.dbentities.PartyInformation
import com.karasm.meet.database.dbentities.UserEntity
import io.reactivex.Flowable
import io.reactivex.Observable
import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*
import retrofit2.http.POST
import retrofit2.http.Multipart




interface IRetroInterface {

@GET("list.php")
fun getParties(@Query("search") search:String) : Observable<retrofit2.Response<List<PartyInformation>>>


    @GET("list_meets.php")
    fun getMeets(@Query("id") id:Int,@Query("search") search:String) : Flowable<retrofit2.Response<List<PartyInformation>>>

    @GET("list_my_parties.php")
    fun getMyParties(@Query("id") id:Int,@Query("search") search:String) : Flowable<retrofit2.Response<List<PartyInformation>>>

@POST("index.php")
fun insertParties(@Body partyInformation: PartyInformation):Call<String>

    @FormUrlEncoded
@POST("author.php")
fun getAuthor(@Field("id") id:Int):Call<UserEntity>

@GET
fun getLocationInfo(@Url url:String,@Query("latlng") latLng:String,@Query("key")key:String):Call<String>

    @Multipart
    @POST("{path}.php")
    fun upload(@Path(value = "path",encoded = true) path:String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part,
    @Query("id") check:String): Call<ResponseBody>

    @GET("user_list.php")
    fun getUsersInfo(@Query("id") id:Int):Call<String>


    @GET("party_id.php")
    fun getPartyById(@Query("id")id:Int):Call<PartyInformation>

    @GET("meet_to_party.php")
    fun meetParty(@Query("partyID")id:Int,@Query("userID")usID:Int):Flowable<retrofit2.Response<String>>

@FormUrlEncoded
@POST("login.php")
fun login(@Field("login") login:String,@Field("password") password:String):Call<UserEntity>

    @POST("register.php")
    fun insertUser(@Body userEntity: UserEntity):Call<String>

}