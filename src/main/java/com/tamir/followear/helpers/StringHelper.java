package com.tamir.followear.helpers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
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

    public static String removeCommas(String str) {
        String newStr = str.replace(",", "");
        return newStr;
    }

    public static String formatDouble(double d) {
        NumberFormat numberFormat = DecimalFormat.getNumberInstance(Locale.US);
        String str = numberFormat.format(d);
        return str;
    }

}
