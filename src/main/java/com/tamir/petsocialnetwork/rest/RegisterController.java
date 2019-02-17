package com.tamir.petsocialnetwork.rest;

import com.nimbusds.jwt.JWTClaimsSet;
import com.tamir.petsocialnetwork.dto.AuthResultDTO;
import com.tamir.petsocialnetwork.dto.SignupRequestDTO;
import com.tamir.petsocialnetwork.services.AuthService;
import com.tamir.petsocialnetwork.services.CsrfService;
import com.tamir.petsocialnetwork.services.RegistrationService;
import com.tamir.petsocialnetwork.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("registration")
public class RegisterController {

    @Autowired
    UserService userService;

    @Autowired
    RegistrationService registrationService;

    @Autowired
    CsrfService csrfService;

    @Autowired
    @Qualifier("defaultAuthService")
    AuthService authService;

    @PostMapping(value = "/signup", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public AuthResultDTO signupNewUser(HttpServletResponse response, @RequestBody SignupRequestDTO signupReq) {
        registrationService.signup(signupReq);
        AuthResultDTO authResultDTO = registrationService.signIn(response, signupReq.getUserName(), signupReq.getPassword());
        return authResultDTO;
    }

    @GetMapping(value = "/signin", produces = "application/json")
    @ResponseBody
    public AuthResultDTO signinUser(HttpServletResponse response,
                                    @RequestParam String username, @RequestParam String password) {
        AuthResultDTO authResultDTO = registrationService.signIn(response, username, password);
        return authResultDTO;
    }

    @GetMapping(value = "/auto-login")
    @ResponseBody
    public AuthResultDTO autoLogin(HttpServletRequest request, HttpServletResponse response) {
        JWTClaimsSet claimsSet = authService.authenticateRequest(request, response);
        csrfService.setCsrfCookie(response);

        AuthResultDTO authRes = new AuthResultDTO(
                Long.parseLong((String) claimsSet.getClaim("custom:id")),
                (String) claimsSet.getClaim("cognito:username"));

        return authRes;
    }

    @GetMapping(value = "/check-username-exists")
    @ResponseBody
    public boolean checkUsernameExists(@RequestParam String username) {
        return userService.existsByUsername(username);
    }

    @GetMapping(value = "/check-email-exists")
    @ResponseBody
    public boolean checkEmailExists(@RequestParam String email) {
        return userService.existsByEmail(email);
    }


    //TODO: confirm signup endpoint - using the verification code sent to email

}
