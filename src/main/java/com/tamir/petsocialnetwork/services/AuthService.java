package com.tamir.petsocialnetwork.services;

import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.nimbusds.jwt.JWTClaimsSet;
import com.tamir.petsocialnetwork.AWS.cognito.CognitoService;
import com.tamir.petsocialnetwork.AWS.cognito.JWTValidator;
import com.tamir.petsocialnetwork.exceptions.InvalidToken;
import com.tamir.petsocialnetwork.exceptions.NoAuthException;
import com.tamir.petsocialnetwork.helpers.HttpHelper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


public abstract class AuthService {

    @Autowired
    private CognitoService cognitoService;

    @Autowired
    private JWTValidator jwtValidator;

    /**
     * Authenticate the request from server - throws if not authorized,
     * otherwise does nothing (lets the regular flow continue)
     * Flow:
     * if 'id_token' exists and valid - continue
     * otherwise - try to refresh the tokens using the 'refresh_token'
     * if 'refresh_token' valid - try to validate the new id token,
     * if valid update token cookies and continue
     * otherwise throw the exception
     * if 'refresh_token' invalid - throw the exception
     *
     * @param request  Http request which is supposed to contain the 'id_token' and 'refresh_token'
     *                 in the cookies
     * @param response Http response in case we need to set new cookies
     * @return the id token claimSet (if successful)
     * @throws NoAuthException when not authorized
     */
    public JWTClaimsSet authenticateRequest(HttpServletRequest request, HttpServletResponse response) {

        Map<String, String> cookieValueMap = HttpHelper.getCookieValueMapFromRequest(request);

        if (cookieValueMap.containsKey("id_token")) {
            String idToken = cookieValueMap.get("id_token");
            try {
                return tryValidateIdToken(idToken, request);
            } catch (InvalidToken invalidTokenEx) {
                return tryPerfromRefresh(cookieValueMap, request, response);
            }
        } else if (cookieValueMap.containsKey("refresh_token")) {
            return tryPerfromRefresh(cookieValueMap, request, response);
        } else {
            throw new NoAuthException();
        }

    }

    /**
     * Try to perform token refresh from the 'refresh_token' (if present)
     * Flow:
     * in case refresh token is valid, takes the id token (from the refresh result)
     * and tries to validate it.
     * if the id token is valid - sets the response cookies to the refresh result tokens.
     * otherwise - throws the exception
     *
     * @param cookieValueMap
     * @param response
     * @return the id token claimSet (if successful)
     * @throws NoAuthException when not authorized
     */
    protected JWTClaimsSet tryPerfromRefresh(Map<String, String> cookieValueMap,
                                             HttpServletRequest request, HttpServletResponse response) {
        if (cookieValueMap.containsKey("refresh_token")) {
            JWTClaimsSet claimsSet;
            String refreshToken = cookieValueMap.get("refresh_token");
            try {

                AuthenticationResultType resultType = cognitoService.performRefresh(refreshToken);

                try {
                    claimsSet = tryValidateIdToken(resultType.getIdToken(), request);
                    HttpHelper.setIdAndAccessCookies(response, resultType);
                    return claimsSet;
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

    protected abstract JWTClaimsSet tryValidateIdToken(String idToken, HttpServletRequest request);


}
