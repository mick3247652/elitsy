package com.ru.astron.models

import android.provider.ContactsContract
import com.ru.astron.utils.ParsePhoneNumber


class ContactPhone {

    var name: String? = null
        private set
    var phone: String? = null
        private set
    var photo_thumbanall_uri: String? = ""

    val isNoEmptyPhoto: Boolean
        get() = photo_thumbanall_uri != null

    constructor(name: String?, phone: String?) {
        this.name = name
        this.phone = convertPhoneNumber(phone)
    }

    constructor(name: String?, phone: String?, photo_thumbanall_uri: String?) {
        this.name = name
        this.phone = convertPhoneNumber(phone)
        this.photo_thumbanall_uri = photo_thumbanall_uri
    }

    override fun toString(): String {
        return "Name: $name Phone: $phone"
    }

    private fun convertPhoneNumber(number: String?): String? {
        number?: return null
        var ret = number
        //удалить все не цифровые символы
        ret = number.replace("[^0-9+]+".toRegex(), "")
        //ret = ret.replace("^8".toRegex(), "+7")

        return ParsePhoneNumber.parseE164(ret)
    }

    companion object {
        val CONTACT_ID = ContactsContract.Contacts._ID
        val DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME
        val HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER
        val PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER
        val PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        val CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val PHOTO_URI = ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
        val PHOTO_FULL_URI = ContactsContract.Contacts.PHOTO_URI
    }
}

object ConvertPhoneNumber {
    fun convert(number: String?): String? {
        number?: return null
        var ret = number
        //удалить все не цифровые символы
        ret = number.replace("[^0-9+]+".toRegex(), "")
        //ret = ret.replace("^8".toRegex(), "+7")

        return ParsePhoneNumber.parseE164(ret)
    }
}