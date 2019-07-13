package com.karasm.meet.fragments

import android.Manifest
import android.os.Bundle

import android.view.*
import com.google.android.flexbox.FlexboxLayout
import com.squareup.picasso.Picasso

import com.karasm.meet.database.DBInstance
import com.karasm.meet.database.dbentities.PartyInformation
import com.karasm.meet.functions.toast
import android.R
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.karasm.meet.custom_view.ScrollLockMapView
import com.karasm.meet.database.dbentities.UserEntity
import com.karasm.meet.functions.bitmapDescriptorFromVector
import com.karasm.meet.functions.longtoast
import com.karasm.meet.functions.setLocationText
import com.karasm.meet.server_connect.RetroInstance
import com.miguelcatalan.materialsearchview.MaterialSearchView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.partyinfo_map.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class PartyInfoFragment: Fragment(),View.OnClickListener,OnMapReadyCallback, SwipeRefreshLayout.OnRefreshListener{

    var scrollLockMapView: ScrollLockMapView?=null
    private var googleMap: GoogleMap? = null
    var latLng:List<String>?=null
    var title:String?=null
    var partyInfoAddress:TextView?=null
    lateinit var authorNumber:TextView
    lateinit var submitButton: Button
    lateinit var partyInf:PartyInformation
    lateinit var swipeRefresh:SwipeRefreshLayout
    lateinit var objView:View


    companion object{
        fun newInstance():PartyInfoFragment{
            return PartyInfoFragment()
        }
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpMap()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        objView=inflater.inflate(com.karasm.meet.R.layout.fragment_partyinfo,container,false)

        scrollLockMapView=objView.findViewById(com.karasm.meet.R.id.mapView)
        scrollLockMapView!!.onCreate(savedInstanceState)
        scrollLockMapView!!.onResume()

        scrollLockMapView!!.getMapAsync(this)
        var bundle=this.arguments

        if(bundle!=null){
            partyInf=((bundle.getSerializable("party")) as PartyInformation)!!
        }
        latLng=partyInf!!.address.split(", ")
        title=partyInf!!.title
        partyInfoAddress=objView.findViewById(com.karasm.meet.R.id.partyInfoAddress)
        swipeRefresh=objView.findViewById(com.karasm.meet.R.id.partyInfoRefresh)
        swipeRefresh.isRefreshing=true
        swipeRefresh.setOnRefreshListener(this)
        initInformation(objView,partyInf.id!!.toInt())
        return objView
    }


    override fun onRefresh() {
        initInformation(objView,partyInf.id!!.toInt())
    }

    private fun initInformation(view: View,id:Int){
        val toolbar: Toolbar = view.findViewById(com.karasm.meet.R.id.toolbar)

        ((activity!! as AppCompatActivity)).setSupportActionBar(toolbar)

        ((activity!! as AppCompatActivity)).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        ((activity!! as AppCompatActivity)).supportActionBar!!.setDisplayShowHomeEnabled(true)

        val partyCall=RetroInstance.getInstance().INTERFACE!!.getPartyById(id)
        partyCall.enqueue(object:Callback<PartyInformation>{
            override fun onFailure(call: Call<PartyInformation>, t: Throwable) {

            }

            override fun onResponse(call: Call<PartyInformation>, response: Response<PartyInformation>) {
                if(response.code()==200){
                    val partyInformation=response.body() as PartyInformation

                    swipeRefresh.isRefreshing=false
                    val linLay:LinearLayout=view.findViewById(com.karasm.meet.R.id.gallery_cont)

                    val inflatr=LayoutInflater.from(context)
                    val partyInfoTitl:TextView=view.findViewById(com.karasm.meet.R.id.partyInfoTitle)
                    val partyInfoDescr:TextView=view.findViewById(com.karasm.meet.R.id.partyInfoDescription)
                    val partyInfoDate:TextView=view.findViewById(com.karasm.meet.R.id.partyInfoDate)
                    val partyInfoCurrent:TextView=view.findViewById(com.karasm.meet.R.id.partyInfoCurrent)
                    val partyInfoTotal:TextView=view.findViewById(com.karasm.meet.R.id.partyInfoTotal)
                    val partyInfoPrice:TextView=view.findViewById(com.karasm.meet.R.id.partyInfoPrice)
                    val partyID:TextView=view.findViewById(com.karasm.meet.R.id.numberOfParty)
                    submitButton=view.findViewById(com.karasm.meet.R.id.createPartySubmit)
                    submitButton.setOnClickListener(this@PartyInfoFragment)
                    partyInfoAddress=view.findViewById(com.karasm.meet.R.id.partyInfoAddress)

                    partyID.text="#${partyInformation!!.id}"
                    val id=partyInformation!!.id
                    getUsersInformation(id!!.toInt(),view)
                    latLng=partyInformation!!.address.split(", ")
                    getAuthorInformation(partyInformation.authorId,view)
                    title=partyInformation!!.title
                    partyInfoTitl.text=partyInformation!!.title
                    partyInfoDescr.text=partyInformation!!.description
                    partyInfoCurrent.text=partyInformation!!.current.toString()
                    partyInfoDate.text=partyInformation!!.date
                    partyInfoPrice.text="${partyInformation!!.price}₴ или ${partyInformation!!.priceInfo}"
                    partyInfoTotal.text="/"+partyInformation!!.total

                    if(partyInformation!!.images!=""){
                        val arr=partyInformation!!.images.split(" ")

                        linLay.removeAllViews()
                        for(tmp in arr ){
                            val tmpV:View=inflatr.inflate(com.karasm.meet.R.layout.partyinfo_gallery,linLay,false)
                            val imgV:ImageView=tmpV.findViewById(com.karasm.meet.R.id.imageContainer)
                            imgV.setOnClickListener(object:View.OnClickListener{
                                override fun onClick(v: View?) {
                                    val clickedImg:ImageView=v as ImageView
                                    val dialog: Dialog =Dialog(context)
                                    dialog.setContentView(com.karasm.meet.R.layout.dialog_layout)
                                    val img:ImageView=dialog.findViewById(com.karasm.meet.R.id.dialogImage)
                                    img.background=clickedImg.drawable
                                    dialog.show()
                                }
                            })
                            Picasso.get().load(tmp).fit().centerCrop().into(imgV)
                            linLay.addView(tmpV)
                        }
                    }

                    val flexBox:FlexboxLayout=view.findViewById(com.karasm.meet.R.id.categoryContainer)
                    Log.d(PartyCreateFragment.TAG_VALUE,"["+partyInformation.categories+"]")

                    if(partyInformation.categories!="") {
                        val arr2 = partyInformation.categories.split(" ")



                        DBInstance.getInstance(context!!)!!.dbInstanceDao().getCategories()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe{
                                categories->
                                flexBox.removeAllViews()
                                for (txt in arr2) {
                                    var textBox: TextView = TextView(context)
                                    val index = txt.toInt()
                                    Log.d(PartyCreateFragment.TAG_VALUE,"${index-1}")
                                    textBox.textSize = 18f
                                    textBox.text = categories[index].categoryName
                                    textBox.isClickable = true
                                    textBox.background = context!!.getDrawable(com.karasm.meet.R.drawable.category_container)
                                    val typeface = ResourcesCompat.getFont(context!!, com.karasm.meet.R.font.ptsansbold)
                                    textBox.setTypeface(typeface)
                                    textBox.setOnClickListener(this@PartyInfoFragment)
                                    textBox.setTextColor(
                                        ContextCompat.getColor(
                                            context!!,
                                            com.karasm.meet.R.color.background_material_light
                                        )
                                    )
                                    val params = FlexboxLayout.LayoutParams(
                                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    params.setMargins(0, 16, 16, 16)
                                    textBox.layoutParams = params
                                    flexBox.addView(textBox)
                                }
                            }

                    }
                }
            }
        })

    }
    override fun onClick(v: View?) {
        when(v!!.id){
            authorNumber.id->{
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${authorNumber.text}")
                startActivity(intent)
            }
            submitButton.id->{

                DBInstance.getInstance(context!!).dbInstanceDao().getUser()
                    .subscribeOn(Schedulers.io())
                    .flatMap { user->
                        RetroInstance.getInstance().INTERFACE!!.meetParty(partyInf.id!!.toInt(),user.id!!.toInt())
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{
                        responce->
                                if(responce.code()==200){
                                    context!!.longtoast("Ви успішно зареєструвалися на вечірку, зачекайте кілька хвилин або поновіть сторінку")
                                    initInformation(objView,partyInf.id!!.toInt())
                                }else{
                                    context!!.longtoast("Ви вже зареєстровані")
                                }
                    }
            }

        }

    }
    fun getUsersInformation(id:Int,view:View){
        val userContainer:FlexboxLayout=view.findViewById(com.karasm.meet.R.id.usersContainer)
        val inflater=LayoutInflater.from(context!!)
        val call=RetroInstance.getInstance().INTERFACE!!.getUsersInfo(id)
        call.enqueue(object:Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {

            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                val jsonArray:JSONArray= JSONArray(response.body())

                userContainer.removeAllViews()
                for(i in 0..jsonArray.length()-1){
                    val jsonObj=JSONObject(jsonArray[i].toString())

            val tmpl=inflater.inflate(com.karasm.meet.R.layout.partyinfo_users_template,userContainer,false)
            val cardView: CardView =tmpl.findViewById(com.karasm.meet.R.id.cardViewAuthor)
            val framgeLayout:FrameLayout=cardView.getChildAt(0) as FrameLayout
            var image=framgeLayout.getChildAt(0) as ImageView
                    if(jsonObj.getString("photo")!=""){
            Picasso.get().load(jsonObj.getString("photo")).fit().centerCrop().into(image)
                    }
                    image.tag=jsonObj.getInt("id")
            if(cardView.parent!=null){
                val viewG=cardView.parent as ViewGroup
                viewG.removeView(cardView)
            }

                    image.setOnClickListener(object:View.OnClickListener{
                        override fun onClick(v: View?) {
                            val img=v as ImageView
                            val Progdialog=Dialog(context)
                            Progdialog.setContentView(com.karasm.meet.R.layout.dialogs_progressbar)
                            Progdialog.show()
                            val call=RetroInstance.getInstance().INTERFACE!!.getAuthor(img.tag.toString().toInt())
                            call.enqueue(object:Callback<UserEntity>{
                                override fun onFailure(call: Call<UserEntity>, t: Throwable) {

                                }

                                override fun onResponse(call: Call<UserEntity>, response: Response<UserEntity>) {
                                    if(response.code()==200){
                                        Progdialog.dismiss()
                                        val user=response.body() as UserEntity
                                    val dialog: Dialog =Dialog(context)
                                    dialog.setContentView(com.karasm.meet.R.layout.partyinfo_userdialog_layout)
                                    val imG:ImageView=dialog.findViewById(com.karasm.meet.R.id.userPhoto)
                                        if(user.photo!=""){
                                        Picasso.get().load(user.photo).fit().centerCrop().into(imG)
                                        }
                                        val name:TextView=dialog.findViewById(com.karasm.meet.R.id.userName)
                                        val phone:TextView=dialog.findViewById(com.karasm.meet.R.id.userNumber)
                                        val description:TextView=dialog.findViewById(com.karasm.meet.R.id.userDescription)
                                        name.text=user.name
                                        phone.text=user.phone
                                        description.text=user.about
                                        dialog.show()
                                    }
                                }

                            })

                        }
                    })
            userContainer.addView(cardView)
                }
            }
        })


    }

    fun getAuthorInformation(id:Int,view:View){
        val authorPhoto:ImageView=view.findViewById(com.karasm.meet.R.id.authorPhotoImage)
        val authorName:TextView=view.findViewById(com.karasm.meet.R.id.partyInfoAuthorName)
        authorNumber=view.findViewById(com.karasm.meet.R.id.partyInfoAuthorNumber)
        val authorDescritpion:TextView=view.findViewById(com.karasm.meet.R.id.partyInfoAuthorDescription)
        authorNumber.setOnClickListener(this)
        val call=RetroInstance.getInstance().INTERFACE!!.getAuthor(id)
        call.enqueue(object :Callback<UserEntity>{
            override fun onFailure(call: Call<UserEntity>, t: Throwable) {
                Log.d(PartyCreateFragment.TAG_VALUE,t.toString())
            }

            override fun onResponse(call: Call<UserEntity>, response: Response<UserEntity>) {
                val user=response.body()
                with(user!!){
                    if(photo!=""){
                        Picasso.get().load(photo).into(authorPhoto)
                        authorName.text=name
                        authorDescritpion.text=about
                        authorNumber.text=phone
                    }
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item!!.itemId==R.id.home){
            fragmentManager!!.popBackStack()
        }


        return super.onOptionsItemSelected(item)
    }


    private fun setUpMap(){
        if (ActivityCompat.checkSelfPermission(context!!,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    override fun onMapReady(map: GoogleMap) {

        try{
        googleMap = map

        // For showing a move to my location button
            if (ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap!!.setMyLocationEnabled(true)
            }

        // For dropping a marker at a point on the Map
        val sydney = LatLng(latLng!![0].toDouble(), latLng!![1].toDouble())
        googleMap!!.addMarker(MarkerOptions().position(sydney).title(title!!).snippet("Местоположение").icon(context!!.bitmapDescriptorFromVector(com.karasm.meet.R.drawable.shape)))

            if(!googleMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(context,com.karasm.meet.R.raw.mapstyle))){
                Log.d("PicassoTest","Error loading")
            }


        context!!.setLocationText(partyInfoAddress!!,"${sydney.latitude}, ${sydney.longitude}")
//        val coder:Geocoder= Geocoder(context, Locale.getDefault())
//        var listAddress:List<Address>
//        listAddress=coder.getFromLocation(latLng!![0].toDouble(),latLng!![1].toDouble(),1)
//        Log.d("PicassoTest","${listAddress[0].maxAddressLineIndex}")
//        partyInfoAddress!!.text="${listAddress[0].getAddressLine(0)}"

        // For zooming automatically to the location of the marker
        val cameraPosition = CameraPosition.Builder().target(sydney).zoom(12f).build()
        googleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        }catch (e: Resources.NotFoundException){
            Log.d("ERR","Not found")
        }

    }
    override fun onResume() {
        super.onResume()
        scrollLockMapView!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        scrollLockMapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        scrollLockMapView!!.onStop()
    }
     override fun onPause() {
         scrollLockMapView!!.onPause()
        super.onPause()
    }

     override fun onDestroy() {
         scrollLockMapView!!.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        scrollLockMapView!!.onLowMemory()
    }

}
