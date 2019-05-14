package com.ru.ruchurch.ui

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.ru.ruchurch.R
import com.ru.ruchurch.models.PhoneSelectViewModel
import com.ru.ruchurch.ui.ActionBarActivity.configureActionBar
import com.ru.ruchurch.ui.util.MenuDoubleTabUtil
import kotlinx.android.synthetic.main.activity_phone_select.*
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.ru.ruchurch.adapters.PhoneSelectRecyclerAdapter
import com.ru.ruchurch.models.ContactPhone
import android.content.Intent
import android.app.Activity
import android.view.Menu
import android.provider.ContactsContract

class PhoneSelectActivity : AppCompatActivity() {
    private lateinit var model: PhoneSelectViewModel
    private val REQUEST_PERMISION_CODE = 6688
    private val REQUEST_ADD_CONTACT_CODE = 7788

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_select)
        model = ViewModelProviders.of(this).get(PhoneSelectViewModel::class.java)

        setSupportActionBar(toolbar as Toolbar)
        configureActionBar(supportActionBar)
        title = resources.getString(R.string.select_contact_from_address_book_title)

        requestPermissionContact()
    }

    private fun requestPermissionContact() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            PermisionContactGranted()
        } else {
            //запросить разрешение
            ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.READ_CONTACTS), REQUEST_PERMISION_CODE)
        }
    }
    private fun PermisionContactGranted() {
        //Разрешение на чтение контактов предоставлено
        //можно читать
        Log.v("Read contacts:","Разрешение на чтение контактов предоставлено пользователем");
        reloadAddressBook()
        configureRecycler()
    }

    private fun reloadAddressBook(){
        val cursor = contentResolver.query(ContactPhone.CONTENT_URI,
                arrayOf(ContactPhone.DISPLAY_NAME, ContactPhone.PHONE_NUMBER, ContactPhone.PHOTO_FULL_URI),
                null, null, null)
        if(cursor == null) {finish();return;}//Нету контактов
        if(cursor.getCount() == 0) {finish();return;}//Нету контактов
        model.reloadAddressBook(cursor)
        cursor.close()
    }

    private fun onItemClick(phone: String?){
        phone?: return
        Log.v("PHONE", "Need add contact {$phone}")
        val intent = Intent()
        intent.putExtra("phone", phone)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun configureRecycler(){
        val adapter = PhoneSelectRecyclerAdapter()
        adapter.setItems(model.contactList)
        adapter.onItemClick = {v -> onItemClick(v)}
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (MenuDoubleTabUtil.shouldIgnoreTap()) {
            return false
        }
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_add_contact -> {
                val intent = Intent(Intent.ACTION_INSERT)
                intent.type = ContactsContract.Contacts.CONTENT_TYPE
                //startActivity(intent)
                startActivityForResult(intent,REQUEST_ADD_CONTACT_CODE)
                //finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_ADD_CONTACT_CODE) {
            reloadAddressBook()
            (recycler.adapter as PhoneSelectRecyclerAdapter).setItems(model.contactList)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISION_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Разрешение таки получено
                PermisionContactGranted()
            } else finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.phone_select, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
