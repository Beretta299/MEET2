package com.karasm.meet.fragments

import android.app.PendingIntent.getActivity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.karasm.meet.adapters.RecyclerAdapter
import com.karasm.meet.containers.Party
import com.karasm.meet.R
import com.karasm.meet.database.DBInstance
import com.karasm.meet.database.dbentities.PartyInformation
import com.karasm.meet.functions.*
import com.karasm.meet.server_connect.RetroInstance
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.partylist_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class PartyListFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener,View.OnKeyListener {


    var searchView: MaterialSearchView?=null
    lateinit var queryVal:String
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    companion object{
        fun newInstance():PartyListFragment{
            return PartyListFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    fun setProfile(navView: NavigationView){
        DBInstance.getInstance(context!!).dbInstanceDao().getUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                user->
                val header: View =navView.inflateHeaderView(R.layout.navview_header)
                val picture:ImageView=header.findViewById(R.id.profilePhoto)
                val title: TextView =header.findViewById(R.id.nameProfile)
                val about:TextView=header.findViewById(R.id.aboutProfile)
                title.text=user.name
                about.text=user.about
                if(user.photo!=""){
                    Picasso.get().load(user.photo).into(picture)
                }
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View=inflater.inflate(R.layout.fragment_list,container,false)
        var activity=activity as Context

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        ((activity!! as AppCompatActivity)).setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawer_layout)
        val navView: NavigationView = view.findViewById(R.id.nav_view)
        val rcView: RecyclerView =view.findViewById(R.id.rcView)
        setProfile(navView)
        queryVal=""


        swipeRefreshLayout=view.findViewById(R.id.partyListSwipe)
        swipeRefreshLayout.setOnRefreshListener {
            rcAdapter(rcView,queryVal)
        }


        searchView=view.findViewById(R.id.searchView)
        rcAdapter(rcView,queryVal)

        Observable.create<String> { subscriber->
            searchView!!.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    queryVal=query!!
                    rcAdapter(rcView,queryVal)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    subscriber.onNext(newText!!)
                    return false
                }
            })
        }.debounce(500,TimeUnit.MILLISECONDS).subscribe({
            text->
            queryVal=text
            rcAdapter(rcView,queryVal)
        }
        )

        searchView!!.setOnSearchViewListener(object:MaterialSearchView.SearchViewListener{
            override fun onSearchViewClosed() {
//                queryVal=""
//                rcAdapter(rcView,queryVal)
            }

            override fun onSearchViewShown() {
                searchView!!.setQuery(queryVal,false)
            }
        })
        val toggle = ActionBarDrawerToggle(
            getActivity(), drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)














        return view
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when(p0.itemId){
            R.id.create_party->{
                var frag=PartyCreateFragment.newInstance()
                val transaction = activity!!.supportFragmentManager
                    .beginTransaction()

                //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right,R.anim.slide_out_right,R.anim.slide_out_left)
                transaction.replace(R.id.container, frag)
                    .addToBackStack(null)
                    .commit()
            }
            R.id.nav_exit->{

                val transaction = activity!!.supportFragmentManager
                    .beginTransaction()

                //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right,R.anim.slide_out_right,R.anim.slide_out_left)
                transaction.replace(R.id.container, LoginFragment.newInstance())
                    .addToBackStack(null)
                    .commit()
                Single.fromCallable{
                    DBInstance.getInstance(context!!).dbInstanceDao().deleteUser()
                }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()

            }
            R.id.nav_list->{
                var frag: PartyListFragment = PartyListFragment.newInstance()
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
            R.id.nav_my_list->{
                var bundle = Bundle()
                bundle.putSerializable("meet", false)
                var frag: PartyListFragment = PartyListFragment.newInstance()
                frag.arguments = bundle
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
            R.id.nav_my_meets->{
                var bundle = Bundle()
                bundle.putSerializable("meet", true)
                var frag: PartyListFragment = PartyListFragment.newInstance()
                frag.arguments = bundle
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

        }
        return false
    }





    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity!!.menuInflater.inflate(R.menu.main,menu)
        val item = menu!!.findItem(R.id.action_search)
        searchView!!.setMenuItem(item)
    }

    fun rcAdapter(RCView:RecyclerView,query:String){

        var bundle=this.arguments

        if(bundle!=null){
            val boolState=((bundle.getSerializable("meet")) as Boolean)!!
            if(boolState){
                    DBInstance.getInstance(context!!).dbInstanceDao().getUser()
                        .subscribeOn(Schedulers.io())
                        .flatMap { user->
                            RetroInstance.getInstance().INTERFACE!!.getMeets(user.id!!.toInt(),query)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                                response->
                            if(response.code()==200) {
                                val party=response.body() as List<PartyInformation>
                                loadParties(party,RCView)
                            }
                        }
            }else{
                DBInstance.getInstance(context!!).dbInstanceDao().getUser()
                    .subscribeOn(Schedulers.io())
                    .flatMap { user-> RetroInstance.getInstance().INTERFACE!!.getMyParties(user.id!!.toInt(),query)}
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        responce->
                        if(responce.code()==200) {
                            val party=responce.body() as List<PartyInformation>
                            loadParties(party,RCView)
                        }
                    },{Log.d(PartyCreateFragment.TAG_VALUE,"$it")}
                )
            }
        }else{
            val disposable:Disposable=RetroInstance.getInstance().INTERFACE!!.getParties(query).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { responce ->
                        if (responce.code() == 200) {
                            val party = responce.body() as List<PartyInformation>
                            loadParties(party, RCView)
                        }
                    },{error->}
            )
        }


    }



    fun loadParties(parties:List<PartyInformation>,RCView: RecyclerView){
        swipeRefreshLayout.isRefreshing=false

        var recyclerAdapter = RecyclerAdapter(parties, context!!)
        RCView.layoutManager = LinearLayoutManager(context!!)
        RCView.adapter = recyclerAdapter


        recyclerAdapter.setOnClickItemListener(object : RecyclerAdapter.ClickListener {
            override fun onItemClick(position: Int, v: View) {

                var bundle = Bundle()
                bundle.putSerializable("party", parties[position])
                var frag: PartyInfoFragment = PartyInfoFragment.newInstance()
                frag.arguments = bundle
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

            override fun onItemLongClick(position: Int, v: View) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }



    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event!!.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
            if (searchView!!.isSearchOpen) {
                searchView!!.closeSearch()
            }
        }
        return false
    }

}