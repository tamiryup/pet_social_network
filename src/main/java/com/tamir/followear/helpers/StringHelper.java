package com.tamir.followear.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    public static boolean isEmail(String str){
        Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        Matcher mat = pattern.matcher(str);
        return mat.matches();
    }

    public static boolean isValidPassword(String password) {
        if (password.length() >= 6) {
            return true;
        }
        return false;
    }

}