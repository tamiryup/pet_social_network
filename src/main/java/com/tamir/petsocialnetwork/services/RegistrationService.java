package com.tamir.petsocialnetwork.services;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.tamir.petsocialnetwork.AWS.cognito.CognitoService;
import com.tamir.petsocialnetwork.dto.SignupRequestDTO;
import com.tamir.petsocialnetwork.dto.AuthResultDTO;
import com.tamir.petsocialnetwork.entities.User;
import com.tamir.petsocialnetwork.exceptions.InvalidAuthData;
import com.tamir.petsocialnetwork.exceptions.InvalidPassword;
import com.tamir.petsocialnetwork.exceptions.InvalidUserException;
import com.tamir.petsocialnetwork.exceptions.UserCollisionException;
import com.tamir.petsocialnetwork.helpers.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class RegistrationService {

    @Autowired
    CognitoService cognitoService;

    @Autowired
    UserService userService;

    public void signup(SignupRequestDTO signupReq) {

        if (userService.existsByEmail(signupReq.getEmail()))
            throw new UserCollisionException("email already exists");
        if (userService.existsByUsername(signupReq.getUserName()))
            throw new UserCollisionException("username already exists");
        if (!isValidPassword(signupReq.getPassword()))
            throw new InvalidPassword("password doesn't match criteria");

        User user = new User(signupReq.getEmail(), signupReq.getUserName(),
                signupReq.getFullName(), signupReq.getBirthDate());
        user = userService.create(user);

        cognitoService.signUp(signupReq.getUserName(), signupReq.getPassword(), signupReq.getEmail(), user.getId());
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

        setResponseCookies(response, authResult);

        return authResultDTO;
    }

    public void setResponseCookies(HttpServletResponse response, AuthenticationResultType authResult) {

        setIdAndAccessCookies(response, authResult);

        //set refresh_token cookie
        Cookie refreshTokenCookie = new Cookie("refresh_token", authResult.getRefreshToken());
        refreshTokenCookie.setMaxAge(60*60*24*365*10 - 5*60); //ten years - 5 minutes
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        //refreshTokenCookie.setSecure(true);

        response.addCookie(refreshTokenCookie);
    }

    public void setIdAndAccessCookies(HttpServletResponse response, AuthenticationResultType authResult) {

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

    private boolean isValidPassword(String password) {
        if (password.length() >= 6) {
            return true;
        }
        return false;
    }
}
