package com.tamir.petsocialnetwork.AWS.cognito;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.sun.javafx.tools.packager.PackagerException;
import com.tamir.petsocialnetwork.exceptions.InvalidToken;
import com.tamir.petsocialnetwork.exceptions.UrlException;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

@Component
public class JWTValidator {

    @Value("${ps.cognito.client-id}")
    private String audience;

    @Value("${ps.cognito.issuer}")
    private String issuer;

    public void validateIdToken(String token) throws ParseException {
        JWTClaimsSet claimsSet = validateToken(token);
        String tokenUse = claimsSet.getStringClaim("token_use");
        if(!tokenUse.equals("id")){
            throw new InvalidToken("Wrong token use");
        }
    }

    public void validateAccessToken(String token) throws ParseException {
        JWTClaimsSet claimsSet = validateToken(token);
        String tokenUse = claimsSet.getStringClaim("token_use");
        if(!tokenUse.equals("access")) {
            throw new InvalidToken("Wrong token use");
        }
    }

    public JWTClaimsSet validateToken(String token) {
        JWTClaimsSet claimsSet = verifySignature(token);

        //verify audience
        if(!claimsSet.getAudience().equals(audience)) {
            throw new InvalidToken("Wrong audience");
        }

        //verify issuer
        if(!claimsSet.getIssuer().equals(issuer)) {
            throw new InvalidToken("Wrong issuer");
        }

        return claimsSet;
    }

    public JWTClaimsSet verifySignature(String token) {
        ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
        JWKSource keySource;
        JWTClaimsSet claimsSet;

        try {
            keySource = new RemoteJWKSet(
                    new URL("https://cognito-idp.us-east-1.amazonaws.com/us-east-1_aJXHzmpO6/.well-known/jwks.json"));
        } catch (MalformedURLException e) {
            throw new UrlException(e.getMessage());
        }

        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;

        JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);

        SecurityContext ctx = null; // optional context parameter, not required here
        try {
            claimsSet = jwtProcessor.process(token, ctx);
        } catch (Exception e) {
            throw new InvalidToken("Signature didn't pass");
        }
        return claimsSet;
    }
}
