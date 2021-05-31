package com.tamir.followear.helpers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    public static boolean isEmail(String str) {
        Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        Matcher mat = pattern.matcher(str);
        return mat.matches();
    }

    public static boolean isValidUsername(String username) {
        return username.matches("[a-zA-Z0-9._]*");
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

    public static boolean doesContainHebrew(String str) {
        return str.matches(".*[א-ת]+.*");
    }

    /**
     * Replaces all occurrences of char1 with char2
     * and the other way around
     */
    public static String replaceBothWays(String str, char char1, char char2) {
        StringBuilder sb = new StringBuilder(str);
        for (int i = 0; i < str.length(); i++) {
            if (sb.charAt(i) == char1) {
                sb.setCharAt(i, char2);
            } else if (sb.charAt(i) == char2) {
                sb.setCharAt(i, char1);
            }
        }
        return sb.toString();
    }

    public static String encodeUrl(String url) throws IOException {
        return URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
    }

}
