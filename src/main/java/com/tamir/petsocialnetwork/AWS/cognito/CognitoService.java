package com.tamir.petsocialnetwork.AWS.cognito;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.tamir.petsocialnetwork.AWS.MyAWSCredentials;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@NoArgsConstructor
public class CognitoService {

    Logger logger = LoggerFactory.getLogger(CognitoService.class);

    @Autowired
    private MyAWSCredentials myAWSCreds;

    private AWSCognitoIdentityProvider cognitoProvider;

    @Value("${ps.cognito.client-id}")
    private String cogAppClientId;

    @Value("${ps.cognito.pool-id}")
    private String cogPoolId;

    @PostConstruct
    private void init() {
        cognitoProvider = AWSCognitoIdentityProviderClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(myAWSCreds.getCredentials()))
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    public void signUp(String username, String password, String email, long customId){
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setClientId(cogAppClientId);
        signUpRequest.setUsername(username);
        signUpRequest.setPassword(password);

        List<AttributeType> attributes = fillAttributeTypes(email, ""+customId);
        signUpRequest.setUserAttributes(attributes);

        SignUpResult result = cognitoProvider.signUp(signUpRequest);
        logger.info("cognito signup result: " + result);
        AdminConfirmSignUpResult confirmResult = adminConfirmSignup(username);
        logger.info("cognito confirm result: " + confirmResult);
    }

    private AdminConfirmSignUpResult adminConfirmSignup(String username){

        AdminConfirmSignUpRequest confirmSignUpRequest = new AdminConfirmSignUpRequest();
        confirmSignUpRequest.setUsername(username);
        confirmSignUpRequest.setUserPoolId(cogPoolId);

        AdminConfirmSignUpResult result = cognitoProvider.adminConfirmSignUp(confirmSignUpRequest);
        return result;

    }

    private ConfirmSignUpResult confirmSignup(String username, String confirmationCode){

        ConfirmSignUpRequest confirmationRequest = new ConfirmSignUpRequest();
        confirmationRequest.setUsername(username);
        confirmationRequest.setConfirmationCode(confirmationCode);
        confirmationRequest.setClientId(cogAppClientId);

        ConfirmSignUpResult confirmationResult = cognitoProvider.confirmSignUp(confirmationRequest);

        return confirmationResult;
    }


    private List<AttributeType> fillAttributeTypes(String email, String customId){
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


    public AuthenticationResultType performAuth(String username, String password){

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

    public AuthenticationResultType performRefresh(String refreshToken){

        AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
        authRequest.setAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH);
        authRequest.setUserPoolId(cogPoolId);
        authRequest.setClientId(cogAppClientId);
        authRequest.addAuthParametersEntry("REFRESH_TOKEN", refreshToken);

        AdminInitiateAuthResult authChallenge = cognitoProvider.adminInitiateAuth(authRequest);
        AuthenticationResultType result = authChallenge.getAuthenticationResult();

        return result;
    }

}
