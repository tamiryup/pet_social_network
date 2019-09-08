package com.tamir.followear.services;

import com.nimbusds.jwt.JWTClaimsSet;
import com.tamir.followear.AWS.cognito.JWTValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@Qualifier("defaultAuthService")
public class DefaultAuthService extends AuthService {

    @Autowired
    private JWTValidator jwtValidator;

    @Override
    protected JWTClaimsSet tryValidateIdToken(String idToken, HttpServletRequest request) {
        JWTClaimsSet claimsSet =  jwtValidator.validateIdToken(idToken);
        return claimsSet;
    }
}
