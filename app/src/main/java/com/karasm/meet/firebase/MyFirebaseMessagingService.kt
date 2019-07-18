package com.karasm.meet.firebase

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.karasm.meet.fragments.PartyCreateFragment
import android.os.PowerManager
import android.os.Build
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.karasm.meet.MainActivity
import com.karasm.meet.R
import android.app.NotificationChannel
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.squareup.picasso.Picasso


class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)
        val intent= Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("party","${message!!.data["partyID"]}")
        val pendingInstance=
            PendingIntent.getActivity(applicationContext,1,intent, PendingIntent.FLAG_UPDATE_CURRENT)

        var notificationManager: NotificationManager = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = 1
        val channelId = "channel-01"
        val channelName = "Channel Name"
        val importance = NotificationManager.IMPORTANCE_HIGH

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                channelId, channelName, importance
            )
            notificationManager.createNotificationChannel(mChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)

        val bitmap=Picasso.get().load(message!!.data["profilePhoto"]).get()
        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_notificationlogo)
            .setWhen(System.currentTimeMillis())
            .setTicker("Hearty365")
            .setLargeIcon(bitmap)
            .setContentTitle("${message!!.data["title"]}")
            .setContentText("${message!!.data["body"]}")
            .setContentIntent(pendingInstance)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationBuilder.setColor(resources.getColor(R.color.colorPrimary,application.theme))
        }else{
            notificationBuilder.setColor(resources.getColor(R.color.colorPrimary))
        }


        notificationManager.notify(1, notificationBuilder.build())
        Log.d(PartyCreateFragment.TAG_VALUE,"Message")

    }

    override fun onNewToken(p0: String?) {
        Log.d(PartyCreateFragment.TAG_VALUE,"$p0")
    }

}