package com.karasm.meet.fragments

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.karasm.meet.R
import com.karasm.meet.containers.Party
import com.karasm.meet.database.dbentities.UserEntity
import com.karasm.meet.functions.getDateFormat
import com.karasm.meet.functions.resize
import com.karasm.meet.functions.toast
import com.karasm.meet.functions.uploadFile
import com.karasm.meet.server_connect.RetroInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RegisterFragment: Fragment() ,TextWatcher,View.OnClickListener,DatePickerDialog.OnDateSetListener{


    lateinit var txtInputName:TextInputLayout
    lateinit var txtInputEmail:TextInputLayout
    lateinit var txtInputPhone: TextInputLayout
    lateinit var txtInputAbout:TextInputLayout
    lateinit var txtInputPassword:TextInputLayout
    lateinit var txtInputPasswordRepeat:TextInputLayout
    lateinit var editName:EditText
    lateinit var editEmail:EditText
    lateinit var editPhone:EditText
    lateinit var editAbout:EditText
    lateinit var password: TextInputEditText
    lateinit var editpasswordRepeat:TextInputEditText
    lateinit var txtDate: TextView
    lateinit var choose_photo:Button
    lateinit var submit_button:Button
    lateinit var passwordText:String
    lateinit var passwordRepeatText:String
    lateinit var image_profile: ImageView
    lateinit var inputBoxes:Array<EditText>
    lateinit var errorBoxes:Array<String>
    lateinit var txtInputLayouts:Array<TextInputLayout>
    lateinit var register:Button
    var imageUri: Uri?=null

    companion object{
        const val GALLERY_REQUEST_CODE=1

        fun newInstance():RegisterFragment{
            return RegisterFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View=inflater.inflate(R.layout.register_layout,container,false)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.title=""
        ((activity!! as AppCompatActivity)).setSupportActionBar(toolbar)

        ((activity!! as AppCompatActivity)).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        ((activity!! as AppCompatActivity)).supportActionBar!!.setDisplayShowHomeEnabled(true)
        txtInputName=view.findViewById(R.id.txtInputRegisterName)
        txtInputEmail=view.findViewById(R.id.txtInputRegisterEmail)
        txtInputPhone=view.findViewById(R.id.txtInputRegisterPhone)
        txtInputAbout=view.findViewById(R.id.txtInputAbout)
        editName=view.findViewById(R.id.registerNameBox)
        editEmail=view.findViewById(R.id.registerEmailBox)
        editPhone=view.findViewById(R.id.registerPhoneBox)
        editAbout=view.findViewById(R.id.aboutRegisterBox)
        txtDate=view.findViewById(R.id.registerDate)
        choose_photo=view.findViewById(R.id.choose_photo)
        submit_button=view.findViewById(R.id.register_submit)
        txtInputPassword=view.findViewById(R.id.txtInputRegisterPassword)
        password=view.findViewById(R.id.registerPasswordBox)
        txtInputPasswordRepeat=view.findViewById(R.id.txtInputRegisterRepeatPassword)
        editpasswordRepeat=view.findViewById(R.id.registerRepeatPasswordBox)
        image_profile=view.findViewById(R.id.register_profile_photo)
        editEmail.addTextChangedListener(this)
        editPhone.addTextChangedListener(this)
        editpasswordRepeat.addTextChangedListener(this)
        password.addTextChangedListener(this)
        submit_button.setOnClickListener(this)
        choose_photo.setOnClickListener(this)
        txtDate.setOnClickListener(this)
        editName.addTextChangedListener(this)
        editAbout.addTextChangedListener(this)
        txtDate.text=context!!.getDateFormat().format(Calendar.getInstance().time)
        inputBoxes=arrayOf(editName,editPhone,editEmail,password,editpasswordRepeat,editAbout)
        errorBoxes= arrayOf(getString(R.string.registerErrorName),getString(R.string.registerErrorPhone),getString(R.string.registerErrorEmail),getString(R.string.registerErrorPassword),getString(R.string.registerErrorPasswordRepeat),getString(R.string.registerErrorAbout))
        txtInputLayouts=arrayOf(txtInputName,txtInputPhone,txtInputEmail,txtInputPassword,txtInputPasswordRepeat,txtInputAbout)
        return view
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId== android.R.id.home){
            fragmentManager!!.popBackStack()
        }


        return super.onOptionsItemSelected(item)
    }


    override fun onClick(v: View?) {
        when(v!!.id){
            choose_photo.id->{
                if (ActivityCompat.checkSelfPermission(context!!,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        PartyCreateFragment.PERMISSION_REQUEST_EXTERNAL_STORAGE
                    )
                }
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(intent,GALLERY_REQUEST_CODE)
            }
            submit_button.id->{

                var state=false
                for((i,value) in inputBoxes.withIndex()){
                    with(value){
                        if(text.toString()==""){
                            txtInputLayouts[i].error=errorBoxes[i]
                            state=true
                        }else{
                            txtInputLayouts[i].error=""
                        }
                    }
                }
                if(state) return

                val Progdialog= Dialog(context)
                Progdialog.setContentView(com.karasm.meet.R.layout.dialogs_progressbar)
                Progdialog.show()
                val call=RetroInstance.getInstance().INTERFACE!!.insertUser(UserEntity(null,editName.text.toString(),txtDate.text.toString(),editEmail.text.toString(),password.text.toString(),editPhone.text.toString(),"",editAbout.text.toString()))
                call.enqueue(object:Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        context!!.toast(t.toString())
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if(response.code()==200){
                            Progdialog.dismiss()
                            if(imageUri!=null){
                                val id:Int=response.body().toString().toInt()
                                    context!!.uploadFile(imageUri!!,id,"register")
                            }
                                context!!.toast("Вітаємо з реєстрацією!")
                                getActivity()!!.supportFragmentManager.beginTransaction().replace(R.id.container, LoginFragment.newInstance())
                                    .addToBackStack(null)
                                    .commit()
                        }else{
                            context!!.toast(getString(R.string.registerErrorEmailExist))
                        }
                    }
                })
            }
            txtDate.id->{
                DatePickerDialog(
                    context, this, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(
                        Calendar.DAY_OF_MONTH
                    )
                ).show()
            }
        }

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            val cal=Calendar.getInstance()
            cal.set(year,month,dayOfMonth)

            txtDate.text=context!!.getDateFormat().format(cal.time)
    }

    override fun afterTextChanged(s: Editable?) {
        if(passwordText!=passwordRepeatText){
           txtInputPasswordRepeat.error=getString(R.string.registerErrorPasswordRepeat)
        }else{
            txtInputPasswordRepeat.error=""
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        passwordText=password.text!!.trim().toString()
        passwordRepeatText=editpasswordRepeat.text!!.trim().toString()
        when(s.hashCode()){
            editEmail.text.hashCode()->{
                if(!editEmail.text.matches(android.util.Patterns.EMAIL_ADDRESS.toRegex())){
                    txtInputEmail.error=getString(R.string.registerErrorEmail)
                }else{
                    txtInputEmail.error=""
                }
            }
            editPhone.text.hashCode()->{
                if(!editPhone.text.matches(Regex("^\\+?(38)?\\(?[0-9]{3}\\)?[0-9]{3}\\-?[0-9]{2}\\-?[0-9]{2}$"))){
                    txtInputPhone.error=getString(R.string.registerErrorPhone)
                }else{
                    txtInputPhone.error=""
                }
            }
            password.text.hashCode()->{
                if(!password.text!!.matches(Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$"))){
                    txtInputPassword.error=getString(R.string.registerErrorPassword)
                }else{
                    txtInputPassword.error=""
                }
            }
            editName.text.hashCode()->{
                if(editName.text.toString()==""){
                    txtInputName.error=getString(R.string.registerErrorName)
                }else{
                    txtInputName.error=""
                }
            }
            editAbout.text.hashCode()->{
                if(editAbout.text.toString()==""){
                    txtInputAbout.error=getString(R.string.registerErrorAbout)
                }else{
                    txtInputAbout.error=""
                }
            }

        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === Activity.RESULT_OK){
            if(requestCode== GALLERY_REQUEST_CODE){
                if(data!!.data!=null){
                    imageUri =data!!.data


                    val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(activity!!.contentResolver, imageUri)

                    image_profile.setImageBitmap(context!!.resize(bitmap,300,500))


                }
            }
        }
    }
}