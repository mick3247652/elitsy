package com.ru.ruchurch.adapters

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.ru.ruchurch.R
import android.provider.MediaStore
import android.net.Uri
import android.view.View
import com.ru.ruchurch.models.ContactPhone
import com.ru.ruchurch.utils.ParsePhoneNumber
import java.io.IOException


class PhoneSelectRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.phone_select_card, viewGroup, false) as ConstraintLayout
        return object : RecyclerView.ViewHolder(v) {}
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val v = viewHolder.itemView as ConstraintLayout
        val photo = v.findViewById<ImageView>(R.id.photo)
        val name = v.findViewById<TextView>(R.id.name)
        val phone = v.findViewById<TextView>(R.id.phone)
        name.text = items[position].name
        var phoneValue = items[position].phone ?: ""
        phoneValue = ParsePhoneNumber.parse(phoneValue)
        phone.text = phoneValue
        try {
            if (items[position].isNoEmptyPhoto)
                photo.setImageBitmap(MediaStore.Images.Media.getBitmap(v.context.contentResolver,
                        Uri.parse(items[position].photo_thumbanall_uri)))
            else
                photo.setImageBitmap(null)
        } catch (e: IOException) {
            photo.setImageBitmap(null)
        }
        v.setOnClickListener { onItemClick(items[position].phone) }
        //v.setBackgroundColor(v.resources.getColor(R.color.contact_select))
    }

    var onItemClick: (String?) -> Unit = { }

    private var selectedItem: ConstraintLayout? = null
    private var selectedContactPhone: ContactPhone? = null

    private fun selectItem(layout: ConstraintLayout?, contactPhone: ContactPhone?) {
        if (selectedItem != null) {
            //убрать выделение с выбранного ранее элемента и назначить ему клик на выделение
            layout ?: selectedItem?.setBackgroundColor(layout!!.resources.getColor(R.color.white))
            selectedItem?.setOnClickListener { selectItem(selectedItem, selectedContactPhone) }
            val t = selectedItem!!.findViewById<TextView>(R.id.title)
            t.visibility = View.GONE

        }

        selectedItem = layout
        selectedContactPhone = contactPhone
        if (layout != null) {
            selectedItem?.setBackgroundColor(layout.resources.getColor(R.color.contact_select))
            val t = selectedItem!!.findViewById<TextView>(R.id.title)
            t.visibility = View.VISIBLE
        }
        selectedItem?.setOnClickListener { onItemClick(contactPhone?.phone) }
    }

    private var items = mutableListOf<ContactPhone>()

    fun setItems(list: List<ContactPhone>) {
        items = list.toMutableList()
        notifyDataSetChanged()
        Log.w("ADAPTER", "setItems list size = ${list.size}")
    }

}