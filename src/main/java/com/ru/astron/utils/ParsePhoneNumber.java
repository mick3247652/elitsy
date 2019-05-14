package com.ru.astron.utils;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

public class ParsePhoneNumber {
    public static String parse(String number) {
        PhoneNumberUtil phoneUtil = Repo.phoneUtil;
        Phonenumber.PhoneNumber phone = null;
        try {
            phone = phoneUtil.parseAndKeepRawInput(number, "RU");
        } catch (NumberParseException e) {
            return number;
        }
        if(phone == null) return number;
        boolean isValid = phoneUtil.isValidNumber(phone);
        if(!isValid) return number;
        String ret = phoneUtil.format(phone, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        return ret;
    }

    public static String parseE164(String number) {
        PhoneNumberUtil phoneUtil = Repo.phoneUtil;
        Phonenumber.PhoneNumber phone = null;
        try {
            phone = phoneUtil.parseAndKeepRawInput(number, "RU");
        } catch (NumberParseException e) {
            return number;
        }
        if(phone == null) return number;
        boolean isValid = phoneUtil.isValidNumber(phone);
        if(!isValid) return number;
        String ret = phoneUtil.format(phone, PhoneNumberUtil.PhoneNumberFormat.E164);
        return ret;
    }
}
