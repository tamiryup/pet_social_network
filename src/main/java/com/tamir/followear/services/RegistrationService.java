package com.tamir.followear.services;

import com.amazonaws.services.cognitoidp.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamir.followear.AWS.cognito.CognitoService;
import com.tamir.followear.dto.SignupRequestDTO;
import com.tamir.followear.dto.AuthResultDTO;
import com.tamir.followear.entities.User;
import com.tamir.followear.exceptions.*;
import com.tamir.followear.helpers.AWSHelper;
import com.tamir.followear.helpers.HttpHelper;
import com.tamir.followear.helpers.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class RegistrationService {

    @Autowired
    private CognitoService cognitoService;

    @Autowired
    private UserService userService;

    @Autowired
    private CsrfService csrfService;

    public void signup(SignupRequestDTO signupReq) {
        if(!StringHelper.isValidUsername(signupReq.getUserName()))
            throw new InvalidUsernameException();
        if(!StringHelper.isEmail(signupReq.getEmail()))
            throw new InvalidEmailException();

        signupReq.setUserName(signupReq.getUserName().toLowerCase());

        if (userService.existsByEmail(signupReq.getEmail()))
            throw new UserCollisionException("email already exists");
        if (userService.existsByUsername(signupReq.getUserName()))
            throw new UserCollisionException("username already exists");
        if (!cognitoService.isValidPassword(signupReq.getPassword()))
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
        boolean isEmail = StringHelper.isEmail(username);

        if(!isEmail) {
            username = username.toLowerCase();
        }

        try {
            authResult = cognitoService.performAuth(username, password);
        } catch (UserNotFoundException e) {
            throw new InvalidUserException();
        } catch (NotAuthorizedException e) {
            throw new InvalidAuthData(e.getMessage());
        }

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

    public AuthResultDTO codeLogin(HttpServletResponse response, String code) {

        AuthenticationResultType authResult = cognitoService.performCodeGrantFlow(code);

        GetUserResult userResult = cognitoService.getUser(authResult.getAccessToken());
        List<AttributeType> attributes = userResult.getUserAttributes();
        Map<String, String> attributesMap = AWSHelper.createMapFromAttributeTypes(attributes);

        User user;
        String providerName = getIdentityProviderName(attributesMap);
        if (providerName.equals("Facebook")){
            user = userService.createUserFromCognitoAttrFacebook(attributesMap, userResult.getUsername());
        } else { //provider is apple
            user = userService.createUserFromCognitoAttrApple(attributesMap, userResult.getUsername());
        }

        AuthResultDTO ret = new AuthResultDTO(user);

        //if user signs up for the first time
        if(!attributesMap.containsKey("custom:id")) {
            ret.setNew(true);
            try {
                cognitoService.updateCustomIdAndPreferredUsername(authResult.getAccessToken(),
                        user.getUsername(), user.getId());
            } catch (Exception e) {
                userService.delete(user);
                throw new CognitoException(e.getMessage());
            }
        }

        if(attributesMap.get("email_verified").equals("false")) {
            cognitoService.markEmailAsVerified(user.getUsername());
        }

        // refresh the tokens so the id token has the updated claims
        if(!attributesMap.containsKey("custom:id")) {
            try {
                authResult = cognitoService.performRefresh(authResult.getRefreshToken());
            } catch (AWSCognitoIdentityProviderException notAuthEx) {
                throw new NoAuthException(notAuthEx.getMessage());
            }
        }

        HttpHelper.setResponseCookies(response, authResult);
        HttpHelper.setUserIdCookie(response, user.getId());
        csrfService.setCsrfCookie(response);

        return ret;
    }

    private String getIdentityProviderName(Map<String, String> attributesMap) {
        String res = "";
        String identitiesJson = attributesMap.get("identities");

        try {
            List<Map<String, String>> identitiesMapList = new ObjectMapper().readValue(identitiesJson,
                    new TypeReference<List<Map<String, String>>>() {
                    });
            Map<String, String> identetiesMap = identitiesMapList.get(0);
            res = identetiesMap.get("providerName");
        } catch (Exception e){
            e.printStackTrace();
        }

        return res;
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
        if(!cognitoService.isValidPassword(newPassword))
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
