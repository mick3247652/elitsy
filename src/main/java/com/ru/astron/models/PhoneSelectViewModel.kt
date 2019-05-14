package com.ru.astron.models

import android.arch.lifecycle.ViewModel
import android.database.Cursor



class PhoneSelectViewModel: ViewModel() {
    val contactList = mutableListOf<ContactPhone>()

    init {

    }

    fun reloadAddressBook(cursor: Cursor){
        contactList.clear()
        while (cursor.moveToNext()) {
            val displayName: String? = cursor.getString(cursor.getColumnIndex(ContactPhone.DISPLAY_NAME))
            val phoneNumber: String? = cursor.getString(cursor.getColumnIndex(ContactPhone.PHONE_NUMBER))
            val photoURI: String? = cursor.getString(cursor.getColumnIndex(ContactPhone.PHOTO_FULL_URI))
            displayName?: continue
            phoneNumber?: continue
            val contact = ContactPhone(displayName,phoneNumber,photoURI)
            if(!testPhone(contact)) contactList.add(contact)
        }
    }

    fun testPhone(contactPhone: ContactPhone): Boolean {
        for(c in contactList) if(c.phone.equals(contactPhone.phone)) return true
        return false
    }
}