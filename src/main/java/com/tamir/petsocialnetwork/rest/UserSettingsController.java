package com.tamir.petsocialnetwork.rest;

import com.tamir.petsocialnetwork.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("settings/{id}")
public class UserSettingsController {

    @Autowired
    UserService userService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/update-profile-image")
    @ResponseBody
    public String updateProfileImage(@PathVariable("id") long id, @RequestParam("image") MultipartFile image)
            throws IOException {
        String addr = userService.updateProfileImage(id, image);
        return addr;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/update-description")
    public void updateDescription(@PathVariable long id, @RequestParam("description") String description) {
        userService.updateDescriptionById(id, description);
    }

}
