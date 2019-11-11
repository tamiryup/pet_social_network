package com.tamir.followear.rest;

import com.nimbusds.jwt.JWTClaimsSet;
import com.tamir.followear.dto.AuthResultDTO;
import com.tamir.followear.dto.LoginDTO;
import com.tamir.followear.dto.NewPassowrdDTO;
import com.tamir.followear.dto.SignupRequestDTO;
import com.tamir.followear.services.AuthService;
import com.tamir.followear.services.CsrfService;
import com.tamir.followear.services.RegistrationService;
import com.tamir.followear.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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
                                    @RequestBody LoginDTO loginReq) {
        AuthResultDTO authResultDTO = registrationService.signIn(response, loginReq.getUsername(),
                loginReq.getPassword());
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

    @GetMapping("/logout")
    @ResponseBody
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        registrationService.logout(request, response);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/reset-password")
    public void resetPassword(@RequestParam String username) {
        registrationService.resetPassword(username);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/set-new-password")
    public void setNewPassword(@RequestBody NewPassowrdDTO newPasswordReq) {
        registrationService.setNewPassword(newPasswordReq.getUsername(),
                newPasswordReq.getNewPassword(), newPasswordReq.getCode());
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

}
