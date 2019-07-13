package com.karasm.meet.fragments

import android.app.Dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.karasm.meet.R
import com.karasm.meet.database.DBInstance
import com.karasm.meet.database.dbentities.UserEntity
import com.karasm.meet.server_connect.RetroInstance
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class LoginFragment: Fragment(),View.OnClickListener{


    lateinit var txtInputLogin: TextInputLayout
    lateinit var txtInputPassword:TextInputLayout
    lateinit var loginBox:EditText
    lateinit var passwordBox: TextInputEditText
    lateinit var submitButton: Button
    lateinit var registrationButton: TextView
    lateinit var dialog: Dialog
    companion object{
        fun newInstance():LoginFragment{
            return LoginFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View=inflater.inflate(com.karasm.meet.R.layout.login_layout,container,false)
        txtInputLogin=view.findViewById(com.karasm.meet.R.id.txtInputLogin)
        txtInputPassword=view.findViewById(com.karasm.meet.R.id.txtInputPassword)
        loginBox=view.findViewById(com.karasm.meet.R.id.loginBox)
        passwordBox=view.findViewById(com.karasm.meet.R.id.passwordBox)
        submitButton=view.findViewById(com.karasm.meet.R.id.loginButton)
        registrationButton=view.findViewById(com.karasm.meet.R.id.registerButton)
        registrationButton.setOnClickListener(this)
        submitButton.setOnClickListener(this)
        dialog=Dialog(context)
        dialog.setContentView(com.karasm.meet.R.layout.dialogs_progressbar)
        return view
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            submitButton.id->{


                sentData()
            }
            registrationButton.id->{
                val transaction = activity!!.supportFragmentManager
                    .beginTransaction()

                    //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                     transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right,R.anim.slide_out_right,R.anim.slide_out_left)

                    transaction.replace(R.id.container, RegisterFragment.newInstance())
                    .addToBackStack(null)
                    .commit()
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item!!.itemId== android.R.id.home){
            fragmentManager!!.popBackStack()
        }


        return super.onOptionsItemSelected(item)
    }

    fun sentData(){
        dialog.show()
        val call=RetroInstance.getInstance().INTERFACE!!.login(loginBox.text.toString(),passwordBox.text.toString())
        call.enqueue(object:Callback<UserEntity>{
            override fun onFailure(call: Call<UserEntity>, t: Throwable) {
            Log.d(PartyCreateFragment.TAG_VALUE,t.toString())
            }

            override fun onResponse(call: Call<UserEntity>, response: Response<UserEntity>) {
                val user:UserEntity=response.body() as UserEntity
                dialog.dismiss()
                if(user.name!=null){
                    Single.fromCallable {  DBInstance.getInstance(context!!).dbInstanceDao().insertUser(user)

                    }.doOnError{Log.d(PartyCreateFragment.TAG_VALUE,"$it")}
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe()

                    val transaction = activity!!.supportFragmentManager
                        .beginTransaction()

                    transaction.setCustomAnimations(R.anim.card_flip_right_enter,
                        R.anim.card_flip_right_exit,
                        R.anim.card_flip_left_enter,
                        R.anim.card_flip_left_exit)

                    transaction.replace(R.id.container, PartyListFragment.newInstance())
                        .addToBackStack(null)
                        .commit()



                }else{
                    txtInputLogin.error=context!!.getString(R.string.loginError)
                    txtInputPassword.error=context!!.getString(R.string.loginError)
                }
            }
        })
    }

}