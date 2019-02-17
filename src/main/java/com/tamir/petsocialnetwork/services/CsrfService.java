package com.tamir.petsocialnetwork.services;

import com.tamir.petsocialnetwork.RandomString;
import com.tamir.petsocialnetwork.exceptions.CsrfException;
import com.tamir.petsocialnetwork.helpers.HttpHelper;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Service
public class CsrfService {

    public static final String csrfCookieName = "XSRF-TOKEN";

    public static final String csrfHeaderName = "x-xsrf-token";

    private RandomString randomString = new RandomString(32);

    public void setCsrfCookie(HttpServletResponse response) {

        Cookie csrfCookie = new Cookie(csrfCookieName, randomString.nextString());
        csrfCookie.setMaxAge(-1);
        csrfCookie.setPath("/");
        csrfCookie.setHttpOnly(false);
        //csrfCookie.setSecure(true);

        response.addCookie(csrfCookie);
    }

    public void validateCsrf(HttpServletRequest request) {
        Map<String, String> cookieMap = HttpHelper.getCookieValueMapFromRequest(request);
        Map<String, String> headerMap = HttpHelper.getHeadersInfo(request);

        if(!cookieMap.containsKey(csrfCookieName)) {
            throw new CsrfException("Missing csrf cookie");
        }
        if(!headerMap.containsKey(csrfHeaderName)) {
            throw new CsrfException("Missing csrf header");
        }

        if(!cookieMap.get(csrfCookieName).equals(headerMap.get(csrfHeaderName))) {
            throw new CsrfException("Header and cookie values don't match");
        }
    }
}
