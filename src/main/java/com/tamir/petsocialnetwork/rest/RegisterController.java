package com.tamir.petsocialnetwork.rest;

import com.tamir.petsocialnetwork.dto.SignupRequestDTO;
import com.tamir.petsocialnetwork.dto.AuthResultDTO;
import com.tamir.petsocialnetwork.services.RegistrationService;
import com.tamir.petsocialnetwork.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("registration")
public class RegisterController {

    @Autowired
    UserService userService;

    @Autowired
    RegistrationService registrationService;

    @PostMapping(value = "/signup", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public AuthResultDTO signupNewUser(@RequestBody SignupRequestDTO signupReq) {
        registrationService.signup(signupReq);
        AuthResultDTO authResultDTO = registrationService.signIn(signupReq.getUserName(), signupReq.getPassword());
        return authResultDTO;
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

    @GetMapping(value = "/signin", produces = "application/json")
    @ResponseBody
    public AuthResultDTO signinUser(@RequestParam String username, @RequestParam String password) {
        AuthResultDTO authResultDTO = registrationService.signIn(username, password);
        return authResultDTO;
    }

    //TODO: confirm signup endpoint - using the verification code sent to email

}
