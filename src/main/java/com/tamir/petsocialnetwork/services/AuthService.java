package com.tamir.petsocialnetwork.services;

import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.tamir.petsocialnetwork.AWS.cognito.CognitoService;
import com.tamir.petsocialnetwork.AWS.cognito.JWTValidator;
import com.tamir.petsocialnetwork.exceptions.InvalidToken;
import com.tamir.petsocialnetwork.exceptions.NoAuthException;
import com.tamir.petsocialnetwork.helpers.HttpHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private CognitoService cognitoService;

    @Autowired
    private JWTValidator jwtValidator;

    @Autowired
    private RegistrationService registrationService;

    /**
     * Authenticate the request from server - throws if not authorized,
     * otherwise does nothing (lets the regular flow continue)
     * Flow:
     *      if 'id_token' exists and valid - continue
     *      otherwise - try to refresh the tokens using the 'refresh_token'
     *      if 'refresh_token' valid - try to validate the new id token,
     *                                 if valid update token cookies and continue
     *                                 otherwise throw the exception
     *      if 'refresh_token' invalid - throw the exception
     *
     * @param request Http request which is supposed to contain the 'id_token' and 'refresh_token'
     *                in the cookies
     * @param response Http response in case we need to set new cookies
     *
     * @throws NoAuthException when not authorized
     */
    public void authenticateRequest(HttpServletRequest request, HttpServletResponse response) {

        Map<String, String> cookieValueMap = HttpHelper.getCookieValueMapFromRequest(request);
        String reqUserId = HttpHelper.getPathPartByIndex(request, 2);

        if (cookieValueMap.containsKey("id_token")) {
            String idToken = cookieValueMap.get("id_token");
            try {
                tryValidateIdToken(idToken, reqUserId);
            } catch (InvalidToken invalidTokenEx) {
                tryPerfromRefresh(cookieValueMap, response, reqUserId);
            }
        } else if (cookieValueMap.containsKey("refresh_token")) {
            tryPerfromRefresh(cookieValueMap, response, reqUserId);
        } else {
            throw new NoAuthException();
        }

    }

    /**
     * Try to perform token refresh from the 'refresh_token' (if present)
     * Flow:
     *      in case refresh token is valid, takes the id token (from the refresh result)
     *      and tries to validate it.
     *      if the id token is valid - sets the response cookies to the refresh result tokens.
     *      otherwise - throws the exception
     *
     * @param cookieValueMap
     * @param response
     * @param reqUserId the userId from the path
     *
     * @throws NoAuthException when not authorized
     */
    private void tryPerfromRefresh(Map<String, String> cookieValueMap, HttpServletResponse response, String reqUserId) {
        if (cookieValueMap.containsKey("refresh_token")) {
            String refreshToken = cookieValueMap.get("refresh_token");
            try {

                AuthenticationResultType resultType = cognitoService.performRefresh(refreshToken);

                try {
                    tryValidateIdToken(resultType.getIdToken(), reqUserId);
                    registrationService.setIdAndAccessCookies(response, resultType);
                } catch (InvalidToken invalidToken) {
                    throw new NoAuthException();
                }

            } catch (AWSCognitoIdentityProviderException notAuthEx) {
                throw new NoAuthException(notAuthEx.getMessage());
            }
        } else {
            throw new NoAuthException();
        }
    }


    /**
     * Validates the idToken by checking it's a valid jwt,
     * and that the 'custom:id' matches the userId from the path
     *
     * @param idToken
     * @param reqUserId the userId from the path
     */
    private void tryValidateIdToken(String idToken, String reqUserId) {
        JWTClaimsSet claimsSet = jwtValidator.validateIdToken(idToken);
        if (!((String) claimsSet.getClaim("custom:id")).equals(reqUserId)) {
            throw new NoAuthException("id requested and id in token didn't match");
        }
    }

}
