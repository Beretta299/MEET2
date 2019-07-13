package com.karasm.meet.fragments

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.google.android.flexbox.FlexboxLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.karasm.meet.R
import com.karasm.meet.custom_view.ScrollLockMapView
import com.google.android.gms.maps.CameraUpdateFactory
import android.text.method.TextKeyListener.clear

import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.android.material.textfield.TextInputLayout
import com.karasm.meet.MainActivity
import com.karasm.meet.database.DBInstance
import com.karasm.meet.database.dbentities.Category
import com.karasm.meet.database.dbentities.PartyInformation
import com.karasm.meet.functions.*
import com.karasm.meet.server_connect.RetroInstance
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.createparty_title.*
import kotlinx.android.synthetic.main.register_photo.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import java.util.*
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.ArrayList
import kotlin.math.ln


class PartyCreateFragment: Fragment(),TextWatcher,OnMapReadyCallback,View.OnClickListener,DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {


    lateinit var createTitle: EditText
    lateinit  var txtInput: TextInputLayout
    lateinit var createDescription:EditText
    lateinit var txtInputDescr:TextInputLayout
    lateinit var createPartyDate:TextView
    lateinit var textInputLayouPrice:TextInputLayout
    lateinit var createpartyPrice:EditText
    lateinit var textInputLayoPriceInfo:TextInputLayout
    lateinit var createpartyPriceInfo:EditText
    lateinit var txtInputAmount:TextInputLayout
    lateinit var createpartyAmount:EditText
    lateinit var createPartyMap:ScrollLockMapView
    lateinit var createPartyAddress:TextView
    lateinit var layouts:Array<TextInputLayout>
    lateinit var errorS:Array<String>
    lateinit var inputS:Array<EditText>
    lateinit var submitButton: Button
    lateinit var inflaterR:LayoutInflater
    private lateinit var createPartyCategoriesLayout:FlexboxLayout
    lateinit var listUri:ArrayList<Uri>
    lateinit var linLay:LinearLayout
    lateinit var choose_photos:Button
    lateinit var timePick:TextView

    private var googleMap: GoogleMap?=null
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient



    companion object{
        fun newInstance():PartyCreateFragment{
            return PartyCreateFragment()
        }

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val TAG_VALUE="TAG_VALUE"
        const val GALLERY_REQUEST_CODE=2
        const val PERMISSION_REQUEST_EXTERNAL_STORAGE=3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        setUpMap()
        setHasOptionsMenu(true)
        listUri=ArrayList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View=inflater.inflate(com.karasm.meet.R.layout.createparty_layout,container,false)
        val toolbar: Toolbar = view.findViewById(com.karasm.meet.R.id.toolbar)

        ((activity!! as AppCompatActivity)).setSupportActionBar(toolbar)

        ((activity!! as AppCompatActivity)).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        createTitle =view.findViewById(com.karasm.meet.R.id.createpartyTitle)
        txtInput=view.findViewById(com.karasm.meet.R.id.txtInputTitle)
        createDescription=view.findViewById(com.karasm.meet.R.id.createpartyDescription)
        txtInputDescr=view.findViewById(com.karasm.meet.R.id.txtInputDescr)
        createPartyAddress=view.findViewById(com.karasm.meet.R.id.createPartyAddress)
        createPartyDate=view.findViewById(com.karasm.meet.R.id.createpartyDate)
        textInputLayouPrice=view.findViewById(com.karasm.meet.R.id.textInputLayouPrice)
        textInputLayoPriceInfo=view.findViewById(com.karasm.meet.R.id.textInputLayoPriceInfo)
        createpartyPrice=view.findViewById(com.karasm.meet.R.id.createpartyPrice)
        createpartyPriceInfo=view.findViewById(com.karasm.meet.R.id.createpartyPriceInfo)
        txtInputAmount=view.findViewById(com.karasm.meet.R.id.txtInputAmount)
        createpartyAmount=view.findViewById(com.karasm.meet.R.id.createpartyAmount)
        createPartyMap=view.findViewById(com.karasm.meet.R.id.createPartyMap)
        createPartyMap.onCreate(savedInstanceState)
        createPartyMap.onResume()
        createPartyMap.getMapAsync(this)
        createPartyCategoriesLayout=view.findViewById(com.karasm.meet.R.id.createPartyCategories)
        createTitle.addTextChangedListener(this)
        createDescription.addTextChangedListener(this)
        timePick=view.findViewById(R.id.createPartyTime)
        timePick.setOnClickListener(this)
        createpartyPrice.addTextChangedListener(this)
        createpartyPriceInfo.addTextChangedListener(this)
        createpartyAmount.addTextChangedListener(this)
        submitButton=view.findViewById(R.id.createPartySubmit)
        submitButton.setOnClickListener(this)
        createPartyDate.setOnClickListener(this)

        createPartyDate.text=context!!.getDateFormat().format(Calendar.getInstance().time)
        linLay=view.findViewById(R.id.createPartyhorizgallery)
        layouts= arrayOf(txtInputDescr,txtInput,txtInputAmount,textInputLayoPriceInfo,textInputLayouPrice)
        with(context){
        errorS= arrayOf(getString(R.string.errorDescription),getString(R.string.errorTitle),getString(R.string.errorAmount),getString(R.string.errorPriceInfo),getString(R.string.errorPrice))
        inputS= arrayOf(createDescription,createTitle,createpartyAmount,createpartyPriceInfo,createpartyPrice)
        }

        DBInstance.getInstance(context!!).dbInstanceDao().getCategories()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                catList->
                for( category in catList){
                    var textBox: TextView =TextView(context)

                    textBox.textSize=18f
                    textBox.text=category.categoryName
                    textBox.isClickable=true
                    textBox.background=context!!.getDrawable(com.karasm.meet.R.drawable.category_container)
                    val typeface = ResourcesCompat.getFont(context!!, com.karasm.meet.R.font.ptsansbold)
                    textBox.setTypeface(typeface)
                    textBox.setTag(R.id.ID_TAG,category.id)
                    textBox.setTag(R.id.PRESSED_TAG,false)

                    textBox.setOnClickListener(object:View.OnClickListener{
                        override fun onClick(v: View?) {
                            if((v!!.getTag(R.id.PRESSED_TAG) as Boolean)==false){
                                v!!.background=context!!.getDrawable(R.drawable.category_container_pressed)
                                v!!.setTag(R.id.PRESSED_TAG,true)
                            }else{
                                v!!.background=context!!.getDrawable(com.karasm.meet.R.drawable.category_container)
                                v!!.setTag(R.id.PRESSED_TAG,false)
                            }
                            context!!.toast(v!!.getTag(R.id.ID_TAG).toString())
                        }
                    })
                    textBox.setTextColor(ContextCompat.getColor(context!!,com.karasm.meet.R.color.background_material_light))
                    val params = FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 16, 16, 16)
                    textBox.layoutParams=params
                    createPartyCategoriesLayout.addView(textBox)
                }
            }

        inflaterR=LayoutInflater.from(context)

        choose_photos=view.findViewById(R.id.createPartyAddPhoto)
        choose_photos.setOnClickListener(this)



        return view
    }


    private fun setUpMap(){
        if (ActivityCompat.checkSelfPermission(context!!,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val cal=Calendar.getInstance()
        cal.set(year,month,dayOfMonth)

        createPartyDate.text=context!!.getDateFormat().format(cal.time)
    }

    override fun onMapReady(p0: GoogleMap?) {

        googleMap = p0
        val lngG:LatLng=LatLng(47.939314, 33.417100)
        val marker=googleMap!!.addMarker(MarkerOptions().position(lngG).title("Местоположение").icon(context!!.bitmapDescriptorFromVector( com.karasm.meet.R.drawable.shape)))
        googleMap!!.setOnMapClickListener(object:GoogleMap.OnMapClickListener{
            override fun onMapClick(p0: LatLng?) {

                getLocationInfo(p0!!,marker)
            }
        })
        if (!googleMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, com.karasm.meet.R.raw.mapstyle))) {
            Log.d("PicassoTest", "Error loading")
        }

        getLocation(marker)

    }


    fun getLocation(marker:Marker){
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {


            googleMap!!.getUiSettings().setMyLocationButtonEnabled(true);
            fusedLocationClient.lastLocation.addOnSuccessListener(activity!!) { location ->
                lastLocation=Location("")
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    lastLocation=location
                    val currentLatLng = LatLng(location.latitude, location.longitude)


                    getLocationInfo(currentLatLng,marker)

                }else{
                    val lngG:LatLng=LatLng(47.939314, 33.417100)
                    getLocationInfo(lngG,marker)
                }
            }
        } else {
            context!!.toast("Oshibka")
        }

    }

    fun getLocationInfo(llng:LatLng,marker:Marker){
        lastLocation.latitude=llng.latitude
        lastLocation.longitude=llng.longitude
        marker.position=llng
        googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(llng, 12f))
//        val coder: Geocoder = Geocoder(context, Locale.getDefault())
//        var listAddress:List<Address>
//        listAddress=coder.getFromLocation(llng.latitude,llng.longitude,1)
        context!!.setLocationText(createPartyAddress,"${llng.latitude}, ${llng.longitude}")
        //createPartyAddress.text="${listAddress[0].getAddressLine(0)}"

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item!!.itemId==android.R.id.home){
            fragmentManager!!.popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        var i=0
        for(elem in inputS){
            with(elem){
                if(text.hashCode()==s.hashCode()){
                if(text.toString()==""){
                    layouts[i].error=errorS[i]
                }else{
                    layouts[i].error=""
                }
                }
            }
            i++
        }

        }



    override fun onClick(v: View?) {
        when(v!!.id){

                submitButton.id->{
                    var i=0
                    var state=false
                    for(elem in inputS){
                        with(elem){
                            if(text.toString()==""){
                                state=true
                                layouts[i].error=errorS[i]
                            }else{
                                layouts[i].error=""
                            }
                        }
                        i++
                    }
                    if(!state){
                        val Progdialog= Dialog(context)
                        Progdialog.setContentView(com.karasm.meet.R.layout.dialogs_progressbar)
                        Progdialog.show()
                        var category=""
                        for(i in 0..createPartyCategoriesLayout.childCount-1){
                            with(createPartyCategoriesLayout){
                                if((getChildAt(i).getTag(R.id.PRESSED_TAG) as Boolean)==true){
                                    category+="${getChildAt(i).getTag(R.id.ID_TAG)} "
                                }
                            }
                        }
                        val lnt=(category.length-1)
                        if(category!=""){
                            category=category.substring(0,category.length-1)
                        }
                        DBInstance.getInstance(context!!).dbInstanceDao().getUser()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe{
                                user->
                                val call=RetroInstance.getInstance().INTERFACE!!.insertParties(PartyInformation(null,createTitle.text.toString(),0,createpartyAmount.text.toString().toInt(),createDescription.text.toString(),"${lastLocation.latitude}, ${lastLocation.longitude}",category,createpartyPrice.text.toString().toInt(),createpartyPriceInfo.text.toString(),createPartyDate.text.toString(),timePick.text.toString(),"","",user.id!!.toInt()))
                                call.enqueue(object:Callback<String>{
                                    override fun onFailure(call: Call<String>, t: Throwable) {

                                    }

                                    override fun onResponse(call: Call<String>, response: Response<String>) {
                                        if(response.code()==200){
                                            if(listUri!=null){
                                                val id:Int=response.body().toString().toInt()
                                                for(uri in listUri){
                                                    context!!.uploadFile(uri,id,"index")
                                                }
                                            }
                                        }
                                        Progdialog.dismiss()
                                        val frag=PartyListFragment.newInstance()
                                        val transaction = activity!!.supportFragmentManager
                                            .beginTransaction()

                                        transaction.setCustomAnimations(R.anim.card_flip_right_enter,
                                            R.anim.card_flip_right_exit,
                                            R.anim.card_flip_left_enter,
                                            R.anim.card_flip_left_exit)

                                        transaction.replace(R.id.container, frag)
                                            .addToBackStack(null)
                                            .commit()
                                    }
                                })
                            }

                    }
        }
           choose_photos.id-> {
               if (ActivityCompat.checkSelfPermission(context!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_EXTERNAL_STORAGE)
                }
                val intent :Intent = Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select pictures"), GALLERY_REQUEST_CODE)
           }
            createPartyDate.id->{
                DatePickerDialog(
                    context, this, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(
                        Calendar.DAY_OF_MONTH
                    )
                ).show()
            }
            timePick.id->{
                TimePickerDialog(context!!,this,0,0,true).show()
            }
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        var hour="${hourOfDay}"
        var minutes="${minute}"
        if(hourOfDay<10){
            hour="0${hour}"
        }
        if(minute<10){
            minutes="0${minutes}"
        }
        timePick.text="${hour}:${minutes}"
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === Activity.RESULT_OK){

            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    if(data!!.clipData != null) {
                        Log.d(TAG_VALUE,"NOTNULL")

                        var count :Int= data!!.clipData!!.itemCount

                        for(i in 0 until(count)){
                            val imageUri: Uri = data.clipData.getItemAt(i).uri
                            listUri.add(imageUri)
                            val tmpView=inflaterR.inflate(R.layout.createparty_pickimage_template,linLay,false)
                            val imgV: ImageView =tmpView.findViewById(R.id.image_template)
                            val bitmap:Bitmap = MediaStore.Images.Media.getBitmap(activity!!.contentResolver, imageUri);

                            imgV.setImageBitmap(context!!.resize(bitmap,300,500))
                            val txtCross:TextView=tmpView.findViewById(R.id.closeImage)

                            txtCross.setOnClickListener {
                                listUri.removeAt(linLay.indexOfChild(tmpView))
                                linLay.removeView(tmpView)
                                linLay.forceLayout()

                            }
                            linLay.addView(tmpView)
                        }

                    }
                    if(data!!.data!=null){
                        val imageUri:Uri=data!!.data

                        listUri.add(imageUri)
                        val tmpView=inflaterR.inflate(R.layout.createparty_pickimage_template,linLay,false)
                        val imgV: ImageView =tmpView.findViewById(R.id.image_template)
                        val bitmap:Bitmap = MediaStore.Images.Media.getBitmap(activity!!.contentResolver, imageUri);

                        imgV.setImageBitmap(context!!.resize(bitmap,300,500))
                        val txtCross:TextView=tmpView.findViewById(R.id.closeImage)

                        txtCross.setOnClickListener {
                            listUri.removeAt(linLay.indexOfChild(tmpView))
                            linLay.removeView(tmpView)
                            linLay.forceLayout()

                        }
                        linLay.addView(tmpView)

                    }
                }
            }
        }
    }



    override fun onResume() {
        super.onResume()
        createPartyMap!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        createPartyMap!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        createPartyMap!!.onStop()
    }
    override fun onPause() {
        createPartyMap!!.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        createPartyMap!!.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        createPartyMap!!.onLowMemory()
    }

}