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

    public AuthResultDTO signIn(String username, String password) {
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
        authResultDTO.setIdToken(authResult.getIdToken());
        authResultDTO.setAccessToken(authResult.getAccessToken());
        authResultDTO.setRefreshToken(authResult.getRefreshToken());

        return authResultDTO;
    }

    private boolean isValidPassword(String password) {
        if (password.length() >= 6) {
            return true;
        }
        return false;
    }
}
