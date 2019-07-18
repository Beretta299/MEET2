package com.karasm.meet.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.karasm.meet.R
import com.karasm.meet.containers.Party
import com.karasm.meet.database.dbentities.PartyInformation
import com.karasm.meet.databinding.PartylistListTemplateBinding
import com.karasm.meet.fragments.PartyCreateFragment
import com.squareup.picasso.Picasso



class RecyclerAdapter(val items:List<PartyInformation>, val context: Context) : RecyclerView.Adapter<RecyclerAdapter.OurViewHolder>(){

    interface ClickListener {
        fun onItemClick(position: Int, v: View)
        fun onItemLongClick(position: Int, v: View)
    }

    var clickListener: ClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): OurViewHolder {
        val inflater=LayoutInflater.from(context)
        val binding= PartylistListTemplateBinding.inflate(inflater,parent,false)
       return OurViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OurViewHolder, position: Int) {
        holder.bindItem(items[position])
    }



    override fun getItemCount(): Int {
        return items.size
    }


    inner class OurViewHolder(val binding:PartylistListTemplateBinding) : RecyclerView.ViewHolder(binding.root),View.OnClickListener,View.OnLongClickListener{
        var backImage:ImageView=binding.backImage
        init {
            backImage.setOnClickListener(this)
        }

        fun bindItem(partyInformation:PartyInformation){
            binding.partyInformation=partyInformation
            binding.amountPeopleCard.text=partyInformation.current.toString()
            binding.totalPeopleCard.text="/"+partyInformation.total
            binding.pricePeopleCard.text=partyInformation.price.toString()+"₴"

            if(partyInformation.images!=""){
            var imageArray=partyInformation.images.split(" ").toTypedArray()
                Picasso.get().load(imageArray[0]).into(binding.backImage)
            }else{
                binding.backImage.background=context!!.getDrawable(R.drawable.party)
            }
        }

        override fun onClick(v: View?) {
            clickListener!!.onItemClick(adapterPosition,v!!)
        }

        override fun onLongClick(v: View?): Boolean {
            clickListener!!.onItemLongClick(adapterPosition,v!!)
            return false
        }
    }

    fun setOnClickItemListener(clickListener: RecyclerAdapter.ClickListener){
        this.clickListener=clickListener
    }

}

