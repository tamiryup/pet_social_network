package com.tamir.petsocialnetwork.helpers;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class HttpHelper {

    public static Map<String, Cookie> getCookieMapFromRequest(HttpServletRequest request) {
        Map<String, Cookie> cookieMap = new HashMap<>();

        Cookie[] cookieArr = request.getCookies();
        for(Cookie cookie : cookieArr) {
            cookieMap.put(cookie.getName(), cookie);
        }

        return cookieMap;
    }

    public static Map<String, String> getCookieValueMapFromRequest(HttpServletRequest request) {
        Map<String, String> cookeValuesMap = new HashMap<>();

        Cookie[] cookieArr = request.getCookies();
        for(Cookie cookie : cookieArr) {
            cookeValuesMap.put(cookie.getName(), cookie.getValue());
        }

        return cookeValuesMap;
    }

    public static String[] getPathParts(HttpServletRequest request) {
        String pathInfo = request.getRequestURI();
        String[] pathParts = pathInfo.split("/");
        return pathParts;
    }

    /**
     *
     * @param request
     * @param index starts from 1
     * @return
     */
    public static String getPathPartByIndex(HttpServletRequest request, int index) {
        String[] pathParts = getPathParts(request);
        return pathParts[index];
    }
}
