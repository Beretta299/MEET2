package com.karasm.meet.custom_view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView

class ScrollLockMapView: MapView {

    constructor(context: Context):super(context)

    constructor(context: Context,attributeSet: AttributeSet):super(context,attributeSet)

    constructor(context:Context,attributeSet: AttributeSet,defStyle:Int):super(context,attributeSet,defStyle)

    constructor(context: Context,options:GoogleMapOptions):super(context,options)


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        getParent().requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }
}