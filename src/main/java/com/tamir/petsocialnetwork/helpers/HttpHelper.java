package com.tamir.petsocialnetwork.helpers;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class HttpHelper {

    public static String[] cookieNames = new String[]{"id_token", "access_token", "refresh_token"};

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

    public static void setResponseCookies(HttpServletResponse response, AuthenticationResultType authResult) {
        HttpHelper.setIdAndAccessCookies(response, authResult);

        //set refresh_token cookie
        Cookie refreshTokenCookie = new Cookie("refresh_token", authResult.getRefreshToken());
        refreshTokenCookie.setMaxAge(60*60*24*365*10 - 5*60); //ten years - 5 minutes
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        //refreshTokenCookie.setSecure(true);

        response.addCookie(refreshTokenCookie);
    }

    public static void setIdAndAccessCookies(HttpServletResponse response, AuthenticationResultType authResult) {

        //set id_token cookie
        Cookie idTokenCookie = new Cookie("id_token", authResult.getIdToken());
        idTokenCookie.setMaxAge(authResult.getExpiresIn() - 5*60);
        idTokenCookie.setPath("/");
        idTokenCookie.setHttpOnly(true);
        //idTokenCookie.setSecure(true);

        //set access_token cookie
        Cookie accessTokenCookie = new Cookie("access_token", authResult.getAccessToken());
        accessTokenCookie.setMaxAge(authResult.getExpiresIn() - 5*60);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);
        //accessTokenCookie.setSecure(true);

        response.addCookie(idTokenCookie);
        response.addCookie(accessTokenCookie);
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
