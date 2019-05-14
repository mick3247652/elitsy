package com.ru.astron.utils;

public class GenerateCode {
    private static String code = "123456";
    private GenerateCode() {}

    public static void generate(){
        int c = (int)Math.round(Math.random() * 1000000.0);
        if(c >= 1000000) c = 123456;
        code = String.format("%06d",c);
    }

    public static String getCode(){
        return code;
    }
}
