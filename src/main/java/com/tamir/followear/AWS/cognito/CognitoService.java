package com.tamir.followear.AWS.cognito;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.tamir.followear.AWS.MyAWSCredentials;
import com.tamir.followear.dto.ChangePasswordDTO;
import com.tamir.followear.exceptions.CognitoException;
import com.tamir.followear.exceptions.InvalidPassword;
import com.tamir.followear.exceptions.NoAuthException;
import com.tamir.followear.helpers.HttpHelper;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@NoArgsConstructor
public class CognitoService {

    Logger logger = LoggerFactory.getLogger(CognitoService.class);

    @Autowired
    private MyAWSCredentials myAWSCreds;

    private AWSCognitoIdentityProvider cognitoProvider;

    @Value("${fw.cognito.client-id}")
    private String cogAppClientId;

    @Value("${fw.cognito.pool-id}")
    private String cogPoolId;

    @Value("${spring.profiles}")
    private String env;

    @PostConstruct
    private void init() {
        cognitoProvider = AWSCognitoIdentityProviderClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(myAWSCreds.getCredentials()))
                .withRegion(Regions.EU_WEST_1)
                .build();
    }

    public void signUp(String username, String password, String email, long customId) {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setClientId(cogAppClientId);
        signUpRequest.setUsername(username);
        signUpRequest.setPassword(password);

        List<AttributeType> attributes = fillAttributeTypes(email, "" + customId);
        signUpRequest.setUserAttributes(attributes);

        SignUpResult result = cognitoProvider.signUp(signUpRequest);
        logger.info("cognito signup result: " + result);
        //user is auto-confirmed using the lambda function (email is auto verified)
    }

    private AdminConfirmSignUpResult adminConfirmSignup(String username) {

        AdminConfirmSignUpRequest confirmSignUpRequest = new AdminConfirmSignUpRequest();
        confirmSignUpRequest.setUsername(username);
        confirmSignUpRequest.setUserPoolId(cogPoolId);

        AdminConfirmSignUpResult result = cognitoProvider.adminConfirmSignUp(confirmSignUpRequest);
        return result;

    }

    private ConfirmSignUpResult confirmSignup(String username, String confirmationCode) {

        ConfirmSignUpRequest confirmationRequest = new ConfirmSignUpRequest();
        confirmationRequest.setUsername(username);
        confirmationRequest.setConfirmationCode(confirmationCode);
        confirmationRequest.setClientId(cogAppClientId);

        ConfirmSignUpResult confirmationResult = cognitoProvider.confirmSignUp(confirmationRequest);

        return confirmationResult;
    }


    private List<AttributeType> fillAttributeTypes(String email, String customId) {
        List<AttributeType> attributes = new ArrayList<>();

        AttributeType attributeTypeEmail = new AttributeType();
        attributeTypeEmail.setName("email");
        attributeTypeEmail.setValue(email);
        attributes.add(attributeTypeEmail);

        AttributeType attributeTypeId = new AttributeType();
        attributeTypeId.setName("custom:id");
        attributeTypeId.setValue(customId);
        attributes.add(attributeTypeId);

        return attributes;
    }


    public AuthenticationResultType performAuth(String username, String password) {

        AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
        authRequest.setAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH);
        authRequest.setUserPoolId(cogPoolId);
        authRequest.setClientId(cogAppClientId);

        authRequest.addAuthParametersEntry("USERNAME", username);
        authRequest.addAuthParametersEntry("PASSWORD", password);
        AdminInitiateAuthResult authChallenge = cognitoProvider.adminInitiateAuth(authRequest);
        AuthenticationResultType result = authChallenge.getAuthenticationResult();

        return result;

    }

    public AuthenticationResultType performRefresh(String refreshToken) {

        AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
        authRequest.setAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH);
        authRequest.setUserPoolId(cogPoolId);
        authRequest.setClientId(cogAppClientId);
        authRequest.addAuthParametersEntry("REFRESH_TOKEN", refreshToken);

        AdminInitiateAuthResult authChallenge = cognitoProvider.adminInitiateAuth(authRequest);
        AuthenticationResultType result = authChallenge.getAuthenticationResult();

        return result;
    }


    public String getAccessToken(HttpServletRequest request) {

        Map<String, String> cookieValueMap = HttpHelper.getCookieValueMapFromRequest(request);

        if (cookieValueMap.containsKey("access_token")) {
            return cookieValueMap.get("access_token");
        } else if (cookieValueMap.containsKey("refresh_token")) {

            try {
                String refreshToken = cookieValueMap.get("refresh_token");
                AuthenticationResultType resultType = performRefresh(refreshToken);
                return resultType.getAccessToken();
            } catch (AWSCognitoIdentityProviderException notAuthEx) {
                throw new NoAuthException(notAuthEx.getMessage());
            }

        } else {
            throw new NoAuthException();
        }
    }

    public ForgotPasswordResult forgotPassword(String username) {
        ForgotPasswordResult forgotPasswordRes;
        ForgotPasswordRequest forgotPasswordReq = new ForgotPasswordRequest()
                .withClientId(cogAppClientId)
                .withUsername(username);

        forgotPasswordRes = cognitoProvider.forgotPassword(forgotPasswordReq);
        return forgotPasswordRes;
    }

    public ConfirmForgotPasswordResult confirmForgotPassword(String username, String newPassword,
                                                             String confirmationCode) {
        ConfirmForgotPasswordResult confForgotPasswordRes;
        ConfirmForgotPasswordRequest confirmForgotPasswordReq = new ConfirmForgotPasswordRequest()
                .withClientId(cogAppClientId)
                .withConfirmationCode(confirmationCode)
                .withUsername(username)
                .withPassword(newPassword);

        confForgotPasswordRes = cognitoProvider.confirmForgotPassword(confirmForgotPasswordReq);
        return confForgotPasswordRes;
    }

    public void updadeEmailAttribute(String username, String email) {
        AdminUpdateUserAttributesRequest request = new AdminUpdateUserAttributesRequest();

        List<AttributeType> attributes = new ArrayList<>();

        AttributeType attributeTypeEmail = new AttributeType();
        attributeTypeEmail.setName("email");
        attributeTypeEmail.setValue(email);
        attributes.add(attributeTypeEmail);

        //mark email as verified
        AttributeType attributeTypeEmailVerification = new AttributeType();
        attributeTypeEmailVerification.setName("email_verified");
        attributeTypeEmailVerification.setValue("true");
        attributes.add(attributeTypeEmailVerification);

        request.setUserAttributes(attributes);
        request.setUsername(username);
        request.setUserPoolId(cogPoolId);

        cognitoProvider.adminUpdateUserAttributes(request);
    }

    public void changePassword(String oldPassword, String newPassword, HttpServletRequest servletRequest) {
        String accessToken = getAccessToken(servletRequest);

        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setAccessToken(accessToken);
        changePasswordRequest.setPreviousPassword(oldPassword);
        changePasswordRequest.setProposedPassword(newPassword);

        try {
            cognitoProvider.changePassword(changePasswordRequest);
        } catch (InvalidPasswordException e) {
            throw new InvalidPassword();
        } catch (AWSCognitoIdentityProviderException e) {
            throw new CognitoException(e.getMessage());
        }
    }

    public boolean isValidPassword(String password) {
        if (env.equals("prod")) {

            Pattern passwordPattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");
            Matcher mat = passwordPattern.matcher(password);
            return mat.matches() && password.length() >= 8;

        } else if (password.length() >= 6) {
            return true;
        }

        return false;
    }

}
