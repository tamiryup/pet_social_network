package com.tamir.petsocialnetwork.rest;

import com.tamir.petsocialnetwork.dto.SignupRequestDTO;
import com.tamir.petsocialnetwork.dto.RegisterResponseDTO;
import com.tamir.petsocialnetwork.entities.User;
import com.tamir.petsocialnetwork.exceptions.UserCollisionException;
import com.tamir.petsocialnetwork.repositories.UserRepository;
import com.tamir.petsocialnetwork.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("registration")
public class RegisterController {

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping(value = "/signup", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public RegisterResponseDTO signupNewUser(@RequestBody SignupRequestDTO signupReq) {
        if (userService.existsByEmail(signupReq.getEmail()))
            throw new UserCollisionException("email already exists");
        if (userService.existsByUsername(signupReq.getUserName()))
            throw new UserCollisionException("username already exists");
        String hashedPassword = passwordEncoder.encode(signupReq.getPassword());
        User user = new User(signupReq.getEmail(), hashedPassword, signupReq.getUserName(),
                signupReq.getFullName(), signupReq.getBirthDate());
        user = userService.create(user);
        return new RegisterResponseDTO(user.getId(), user.getUsername());
    }

    @GetMapping(value = "/check-username-exists")
    @ResponseBody
    public boolean checkUsernameExists(@RequestParam String username) {
        return userService.existsByUsername(username);
    }

    @GetMapping(value = "/check-email-exists")
    @ResponseBody
    public boolean checkEmailExists(@RequestParam String email){
        return userService.existsByEmail(email);
    }

    @GetMapping(value = "/signin", produces = "application/json")
    @ResponseBody
    public RegisterResponseDTO signinUser(@RequestParam String email, @RequestParam String password) {
        //TODO: implement this function properly.
        User user = userService.findByEmail(email);
        return null;
    }


}
