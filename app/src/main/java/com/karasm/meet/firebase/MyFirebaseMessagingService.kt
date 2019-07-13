package com.karasm.meet.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.karasm.meet.fragments.PartyCreateFragment

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        Log.d(PartyCreateFragment.TAG_VALUE,"Message")
    }

    override fun onNewToken(p0: String?) {
        Log.d(PartyCreateFragment.TAG_VALUE,"$p0")
    }
}