package com.karasm.meet.functions

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.karasm.meet.server_connect.RetroInstance
import okhttp3.MediaType
import id.zelory.compressor.Compressor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat


fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.longtoast(message: CharSequence)=
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()

fun Activity.string(message: CharSequence)= println("$message")


fun Context.funct()={
    toast("hellokitty")
}

fun Context.bitmapDescriptorFromVector(vectorResId:Int ): BitmapDescriptor {
    var vectorDrawable: Drawable? = ContextCompat.getDrawable(this, vectorResId)
    vectorDrawable!!.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight())
    var bitmap: Bitmap? = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888)
    var canvas: Canvas = Canvas(bitmap);
    vectorDrawable.draw(canvas);
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun Context.resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    var image = image
    if (maxHeight > 0 && maxWidth > 0) {
        val width = image.width
        val height = image.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }
        image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
        return image
    } else {
        return image
    }
}

fun Context.setLocationText(tv: TextView,latLng:String ){
    RetroInstance.getInstance().INTERFACE!!.getLocationInfo("https://maps.googleapis.com/maps/api/geocode/json?&language=uk",latLng,getString(
        com.karasm.meet.R.string.google_maps_key))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
                response->
            var jsonObj = JSONObject(response.body())
            val arr: JSONArray =jsonObj.get("results") as JSONArray
            jsonObj=arr.getJSONObject(1)
            tv.text= jsonObj.getString("formatted_address")
        },{})
}


fun Context.getDateFormat():SimpleDateFormat{
    var format=SimpleDateFormat("yyyy-MM-dd")
    return format
}


fun Context.uploadFile(fileUri: Uri, id:Int,path:String) {
    // create upload service client


    // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
    // use the FileUtils to get the actual file by uri
    var file = FileUtils.getFile(this, fileUri)
    file= Compressor(this).compressToFile(file)
    // create RequestBody instance from file
    val requestFile = RequestBody.create(
        MediaType.parse(this.contentResolver.getType(fileUri)!!),
        file
    )

    // MultipartBody.Part is used to send also the actual file name
    val body = MultipartBody.Part.createFormData("picture", file.name, requestFile)

    // add another part within the multipart request
    val descriptionString = "hello, this is description speaking"
    val description = RequestBody.create(
        MultipartBody.FORM, descriptionString
    )

    // finally, execute the request
    val call = RetroInstance.getInstance().INTERFACE!!.upload(path,description,body,id.toString())
    call.enqueue(object : Callback<ResponseBody> {
        override fun onResponse(
            call: Call<ResponseBody>,
            response: Response<ResponseBody>
        ) {
            Log.v("Upload", "success")
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e("Upload error:", t.message)
        }
    })
}