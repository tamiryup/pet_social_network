package com.tamir.followear.rest;

import com.tamir.followear.dto.ChangePasswordDTO;
import com.tamir.followear.dto.UserInfoDTO;
import com.tamir.followear.entities.User;
import com.tamir.followear.exceptions.InvalidUserException;
import com.tamir.followear.services.UserDeviceService;
import com.tamir.followear.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("settings/{id}")
public class UserSettingsController {

    private final Logger logger = LoggerFactory.getLogger(UserSettingsController.class);

    @Autowired
    UserService userService;

    @Autowired
    UserDeviceService userDeviceService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/current-settings")
    @ResponseBody
    public UserInfoDTO getUserById(@PathVariable long id){
        logger.info("starting getCurrentSetting input userId: {}", id);

        User user = userService.findById(id);
        if(user==null)
            throw new InvalidUserException();
        UserInfoDTO ret = new UserInfoDTO(user.getId(), user.getUsername(), user.getFullName(),
                user.getProfileImageAddr(), user.getDescription(), user.getEmail(), user.getBirthDate());
        return ret;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/update-profile-image")
    @ResponseBody
    public String updateProfileImage(@PathVariable("id") long id, @RequestParam("image") MultipartFile image)
            throws IOException {
        logger.info("starting updateProfileImage input userId: {}", id);
        String addr = userService.updateProfileImage(id, image);
        return addr;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/update-description")
    public void updateDescription(@PathVariable long id, @RequestParam("description") String description) {
        logger.info("starting updateDescription input userId: {}, description: {}", id, description);
        userService.updateDescriptionById(id, description);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/update-full-name")
    public void updateFullName(@PathVariable long id, @RequestParam("fullName") String fullName) {
        logger.info("starting updateFullName input userId: {}, fullName: {}", id, fullName);
        userService.updateFullNameById(id, fullName);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/update-email")
    public void updateEmail(@PathVariable long id, @RequestParam("email") String email) {
        logger.info("starting updateEmail input userId: {}, email: {}", id, email);
        userService.updateEmailById(id, email);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/update-username")
    public void updateUsername(@PathVariable long id, @RequestParam("username") String username) {
        logger.info("stating updateUsername input userId: {}, username: {}", id, username);
        userService.updateUsernameById(id, username);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/change-password")
    public void changePassword(@PathVariable long id, @RequestBody ChangePasswordDTO changePasswordDTO,
                               HttpServletRequest servletRequest) {
        logger.info("starting changePassword input userId: {}, changePasswordReq: {}", id, changePasswordDTO);
        userService.changePassword(id, changePasswordDTO, servletRequest);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/register-device")
    public void registerDevice(@PathVariable long id, @RequestParam String registrationToken) {
        logger.info("starting registerDevice input userId: {}, registrationToken: {}", id, registrationToken);
        userDeviceService.addUserDevice(id, registrationToken);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/unregister-device")
    public void unregisterDevice(@PathVariable long id, @RequestParam String registrationToken) {
        logger.info("starting unregisterDevice input userId: {}, registrationToken: {}", id, registrationToken);
        userDeviceService.removeUserDevice(id, registrationToken);
    }

}
