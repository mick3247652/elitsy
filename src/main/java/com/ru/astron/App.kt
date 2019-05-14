package com.ru.astron

import android.app.Application
import com.ru.astron.utils.Repo
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

class App: Application(){
    override fun onCreate() {
        super.onCreate()

        Repo.phoneUtil = PhoneNumberUtil.createInstance(applicationContext)
    }
}