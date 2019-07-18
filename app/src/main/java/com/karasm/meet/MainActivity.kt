package com.karasm.meet

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.util.Log
import com.karasm.meet.database.DBInstance
import com.karasm.meet.database.dbentities.Category
import com.karasm.meet.server_connect.RetroInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.karasm.meet.functions.FileUtils
import okhttp3.MediaType
import android.content.Intent
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.karasm.meet.broadcast_receivers.MyReceiver
import com.karasm.meet.services.NetworkService
import com.karasm.meet.database.dbentities.UserEntity
import com.karasm.meet.fragments.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var listURI:ArrayList<Uri>

    private fun uploadFile(fileUri: Uri,id:Int) {
        // create upload service client


        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        val file = FileUtils.getFile(this, fileUri)

        // create RequestBody instance from file
        val requestFile = RequestBody.create(
            MediaType.parse(contentResolver.getType(fileUri)!!),
            file
        )

        // MultipartBody.Part is used to send also the actual file name
        val body = MultipartBody.Part.createFormData("picture", file.name, requestFile)

        // add another part within the multipart request
        val descriptionString = "hello, this is description speaking"
        val description = RequestBody.create(
            okhttp3.MultipartBody.FORM, descriptionString
        )

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(PartyCreateFragment.TAG_VALUE, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                //val msg = getString(R.string.msg_token_fmt, token)
                //Log.d(TAG, msg)

                //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })

        if(intent.extras!=null){
            RetroInstance.getInstance().INTERFACE!!.getPartyByI(intent.extras.getString("party").toInt())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    party->
                    var bundle = Bundle()
                    bundle.putSerializable("party", party)
                    var frag: PartyInfoFragment = PartyInfoFragment.newInstance()
                    frag.arguments = bundle
                    val transaction = supportFragmentManager
                        .beginTransaction()

                    transaction.setCustomAnimations(R.anim.card_flip_right_enter,
                        R.anim.card_flip_right_exit,
                        R.anim.card_flip_left_enter,
                        R.anim.card_flip_left_exit)

                    transaction.replace(R.id.container, frag)
                        .addToBackStack(null)
                        .commit()
                }

        }




        val call=RetroInstance.getInstance().INTERFACE!!.getAuthor(26)
        call.enqueue(object:Callback<UserEntity>{
            override fun onFailure(call: Call<UserEntity>, t: Throwable) {

            }

            override fun onResponse(call: Call<UserEntity>, response: Response<UserEntity>) {

            }
        })

        DBInstance.getInstance(this).dbInstanceDao().ifCategoriesExist()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                result->
                if(result==0){
                    parseCategories()
                }
            }

        DBInstance.getInstance(this).dbInstanceDao().ifUserExist()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({result->
                var fragment: Fragment =LoginFragment.newInstance()

                if(result==1){fragment=PartyListFragment.newInstance() }
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container,fragment)
                    .commit()
            },{})


        val deintent = Intent(applicationContext, MyReceiver::class.java)
        val dIntent = PendingIntent.getBroadcast(
            this, 0,
            deintent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarms = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarms.cancel(dIntent)


//        val intent = Intent(applicationContext, MyReceiver::class.java)
//        // Create a PendingIntent to be triggered when the alarm goes off
//        val pIntent = PendingIntent.getBroadcast(
//            this, 0,
//            intent, PendingIntent.FLAG_UPDATE_CURRENT
//        )
//        val firstMillis = System.currentTimeMillis() // alarm is set right away
//        val alarm = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarm.setInexactRepeating(
//            AlarmManager.RTC_WAKEUP, firstMillis,
//            30000, pIntent
//        )

            FirebaseMessaging.getInstance().subscribeToTopic("news")
        FirebaseMessaging.getInstance().subscribeToTopic("crews")

    }

    fun parseCategories(){
        val parser = resources.getXml(R.xml.categories)
        var i=0
        try{
            Single.fromCallable{
            while (parser.eventType!=XmlPullParser.END_DOCUMENT){
                if(parser.attributeCount>0){
                    with(DBInstance.getInstance(this@MainActivity).dbInstanceDao()){

                            //deleteCategories()
                            insertCategory(Category(i.toLong(),"${parser.getAttributeValue(0)}"))
                            i++


                    }
                }

                parser.next()
            }
            }.doOnError {  }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe()
        }catch (exc:XmlPullParserException){

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in getSupportFragmentManager().getFragments()) {
        fragment.onActivityResult(requestCode, resultCode, data);
    }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
//        when (item.itemId) {
//            R.id.nav_home -> {
//                // Handle the camera action
//            }
//            R.id.nav_gallery -> {
//
//            }
//            R.id.nav_slideshow -> {
//
//            }
//            R.id.nav_tools -> {
//
//            }
//            R.id.nav_share -> {
//
//            }
//            R.id.nav_send -> {
//
//            }
//        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        DBInstance.destroyINSTANCE()
    }
}
