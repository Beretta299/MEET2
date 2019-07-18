package com.karasm.meet.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.karasm.meet.database.DBInstance
import com.karasm.meet.fragments.PartyCreateFragment
import com.karasm.meet.server_connect.RetroInstance
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.app.NotificationManager
import android.R
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import java.security.AccessController.getContext
import android.os.PowerManager
import android.os.Build
import android.view.WindowManager
import androidx.core.content.ContextCompat.getSystemService
import com.karasm.meet.MainActivity


class NetworkService:Service() {




    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        val disposable=DBInstance.getInstance(applicationContext).dbInstanceDao().getParties()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list->
                for(party in list){
                    RetroInstance.getInstance().INTERFACE!!.checkPartyAttenders(party.id!!.toInt())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ str->
                            if(party.current<str.toInt()){
                                val notificationBuilder = NotificationCompat.Builder(applicationContext, "M_CH_ID")

                                val intent=Intent(applicationContext,MainActivity::class.java)
                                intent.putExtra("party","${party.id}")
                                val pendingInstance=PendingIntent.getActivity(applicationContext,1,intent,PendingIntent.FLAG_UPDATE_CURRENT)

                                notificationBuilder.setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setSmallIcon(com.karasm.meet.R.drawable.ic_logo)
                                    .setWhen(System.currentTimeMillis())
                                    .setTicker("Hearty365")
                                    .setContentTitle("${party.title}")
                                    .setContentText("Новий чувак у вписці!")
                                    .setContentIntent(pendingInstance)

                                val notificationManager =
                                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                notificationManager.notify(1, notificationBuilder.build())
                                val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
                                val isScreenOn =
                                    if (Build.VERSION.SDK_INT >= 20) pm.isInteractive else pm.isScreenOn // check if screen is on
                                if (!isScreenOn) {
                                    val wl = pm.newWakeLock(
                                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                        "myApp:notificationLock"
                                    )
                                    wl.acquire(3000) //set your time in milliseconds
                                }
                            }
                        },{stopSelf()},{stopSelf()})
                }
            }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(PartyCreateFragment.TAG_VALUE,"Service stopped")
    }
}