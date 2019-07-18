package com.karasm.meet.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.karasm.meet.services.NetworkService

class MyReceiver:BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        val intent=Intent(context,NetworkService::class.java)
        context!!.startService(intent)

    }
}