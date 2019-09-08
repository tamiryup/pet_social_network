package com.tamir.followear.services;

import com.amazonaws.services.cognitoidp.model.*;
import com.tamir.followear.AWS.cognito.CognitoService;
import com.tamir.followear.dto.SignupRequestDTO;
import com.tamir.followear.dto.AuthResultDTO;
import com.tamir.followear.entities.User;
import com.tamir.followear.exceptions.*;
import com.tamir.followear.helpers.HttpHelper;
import com.tamir.followear.helpers.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Service
public class RegistrationService {

    @Autowired
    CognitoService cognitoService;

    @Autowired
    UserService userService;

    @Autowired
    CsrfService csrfService;

    public void signup(SignupRequestDTO signupReq) {

        if (userService.existsByEmail(signupReq.getEmail()))
            throw new UserCollisionException("email already exists");
        if (userService.existsByUsername(signupReq.getUserName()))
            throw new UserCollisionException("username already exists");
        if (!StringHelper.isValidPassword(signupReq.getPassword()))
            throw new InvalidPassword();

        User user = new User(signupReq.getEmail(), signupReq.getUserName(),
                signupReq.getFullName(), signupReq.getBirthDate());
        user = userService.create(user);

        try {
            cognitoService.signUp(signupReq.getUserName(), signupReq.getPassword(), signupReq.getEmail(), user.getId());
        } catch (Exception e) {
            userService.delete(user);
            throw new CognitoException(e.getMessage());
        }

    }

    public AuthResultDTO signIn(HttpServletResponse response, String username, String password) {
        User user;
        AuthenticationResultType authResult;

        try {
            authResult = cognitoService.performAuth(username, password);
        } catch (UserNotFoundException e) {
            throw new InvalidUserException();
        } catch (NotAuthorizedException e) {
            throw new InvalidAuthData(e.getMessage());
        }

        boolean isEmail = StringHelper.isEmail(username);

        if (isEmail) {
            user = userService.findByEmail(username);
        } else {
            user = userService.findByUsername(username);
        }

        AuthResultDTO authResultDTO = new AuthResultDTO();
        authResultDTO.setUserId(user.getId());
        authResultDTO.setUserName(user.getUsername());

        HttpHelper.setResponseCookies(response, authResult);
        HttpHelper.setUserIdCookie(response, user.getId());
        csrfService.setCsrfCookie(response);

        return authResultDTO;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> cookieMap = HttpHelper.getCookieValueMapFromRequest(request);

        for(String cookieName : cookieMap.keySet()) {
            Cookie cookie = new Cookie(cookieName, "");
            cookie.setMaxAge(0);
            cookie.setPath("/");

            response.addCookie(cookie);
        }
    }

    public ForgotPasswordResult resetPassword(String username) {
        ForgotPasswordResult forgotPasswordRes;

        try {
            forgotPasswordRes = cognitoService.forgotPassword(username);
        } catch (UserNotFoundException e) {
            throw new InvalidUserException();
        } catch (Exception e) {
            throw new CognitoException(e.getMessage());
        }

        return forgotPasswordRes;
    }

    public ConfirmForgotPasswordResult setNewPassword(String username, String newPassword, String confirmationCode) {
        if(!StringHelper.isValidPassword(newPassword))
            throw new InvalidPassword();

        ConfirmForgotPasswordResult confForgotPasswordRes;

        try {
            confForgotPasswordRes = cognitoService.confirmForgotPassword(username, newPassword, confirmationCode);
        } catch (UserNotFoundException e) {
            throw new InvalidUserException();
        } catch (InvalidPasswordException e) {
            throw new InvalidPassword();
        } catch (CodeMismatchException | ExpiredCodeException e) {
            throw new InvalidCode(e.getMessage());
        } catch (Exception e) {
            throw new CognitoException(e.getMessage());
        }

        return confForgotPasswordRes;
    }
}
