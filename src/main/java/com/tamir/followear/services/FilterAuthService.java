package com.tamir.followear.services;

import com.nimbusds.jwt.JWTClaimsSet;
import com.tamir.followear.AWS.cognito.JWTValidator;
import com.tamir.followear.exceptions.InvalidToken;
import com.tamir.followear.exceptions.NoAuthException;
import com.tamir.followear.helpers.HttpHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@Qualifier("filterAuthService")
public class FilterAuthService extends AuthService {

    @Autowired
    private JWTValidator jwtValidator;

    /**
     * Validates the idToken by checking it's a valid jwt,
     * and that the 'custom:id' matches the userId from the path
     *
     * @param idToken
     *
     * @return the id token claimSet (if successful)
     */
    protected JWTClaimsSet tryValidateIdToken(String idToken, HttpServletRequest request) {
        String reqUserId = HttpHelper.getPathPartByIndex(request, 3);
        JWTClaimsSet claimsSet = jwtValidator.validateIdToken(idToken);
        if(claimsSet.getClaim("custom:id") == null) {
            throw new InvalidToken("id token missing custom:id");
        }
        if (!((String) claimsSet.getClaim("custom:id")).equals(reqUserId)) {
            throw new NoAuthException("id requested and id in token didn't match");
        }

        return claimsSet;
    }

}
