package com.tamir.followear.services;

import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.GetUserResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.tamir.followear.AWS.cognito.CognitoService;
import com.tamir.followear.AWS.s3.S3Service;
import com.tamir.followear.CommonBeanConfig;
import com.tamir.followear.dto.ChangePasswordDTO;
import com.tamir.followear.dto.SearchDTO;
import com.tamir.followear.entities.User;
import com.tamir.followear.enums.ImageType;
import com.tamir.followear.exceptions.*;
import com.tamir.followear.helpers.AWSHelper;
import com.tamir.followear.helpers.FileHelper;
import com.tamir.followear.helpers.StringHelper;
import com.tamir.followear.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private CognitoService cognitoService;

    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        username = username.toLowerCase();
        return userRepo.existsByUsername(username);
    }

    public boolean existsById(long id) {
        return userRepo.existsById(id);
    }

    public User findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public User create(User user) {
        return userRepo.save(user);
    }

    public User update(User user) {
        return userRepo.save(user);
    }

    public void delete(User user) {
        userRepo.delete(user);
    }

    public User findById(long id) {
        Optional<User> user = userRepo.findById(id);
        if (!user.isPresent()) {
            return null;
        }
        return user.get();
    }

    public List<User> findAllById(List<Long> ids) {
        return Lists.newArrayList(userRepo.findAllById(ids));
    }

    /**
     * finds all users with ids from the list of ids (plus duplicates)
     *
     * @param ids A list of user ids to find in the database
     * @return A list of users with the ids specified including duplicates
     * in case there is a duplicate id. The list returns the users in the same order
     * as the ids received.
     */
    public List<User> findAllByIdWithDuplicates(List<Long> ids) {
        List<User> userList = Lists.newArrayList(userRepo.findAllById(ids));
        List<User> userListPlusDuplicates = new ArrayList<>();
        for (long id : ids) {
            User userById = Iterables.tryFind(userList, user -> id == user.getId()).orNull();
            if (userById != null) //if null than id doesn't exist in database therefore do nothing
                userListPlusDuplicates.add(userById);
        }
        return userListPlusDuplicates;
    }

    /**
     * Maps the user ids received to User objects
     *
     * @param ids User ids to map
     * @return A map which maps all ids received into the corresponding User object
     * (each id from the list appears in the map only once)
     */
    public Map<Long, User> makeMapFromIds(List<Long> ids) {
        List<User> userList = Lists.newArrayList(userRepo.findAllById(ids));

        Map<Long, User> userMap = userList.stream()
                .collect(Collectors.toMap(user -> user.getId(), user -> user));

        return userMap;
    }

    public String getUsernameById(long id) {
        User user = findById(id);
        if(user == null)
            return null;

        return user.getUsername();
    }

    public String updateProfilePictureAddrById(long id, String profilePictureAddr) {
        User user = findById(id);
        if (user == null) {
            throw new InvalidUserException();
        }

        String lastAddr = user.getProfileImageAddr();
        user.setProfileImageAddr(profilePictureAddr);
        update(user);

        if (!lastAddr.equals(CommonBeanConfig.getDefaultProfileImageAddr())) {
            s3Service.deleteByKey(lastAddr);
        }

        return lastAddr;
    }

    public String updateProfileImage(long id, MultipartFile image) throws IOException {
        if (!existsById(id)) {
            throw new InvalidUserException();
        }
        ImageType imageType = ImageType.ProfileImage;
        String extension = FileHelper.getMultipartFileExtension(image);
        String addr = s3Service.uploadImage(imageType, image, extension);
        updateProfilePictureAddrById(id, addr);
        return addr;
    }

    public String updateProfileImage(long id, String url) throws IOException {
        if(!existsById(id)) {
            throw new InvalidUserException();
        }
        ImageType imageType = ImageType.ProfileImage;
        String extension = "jpg";
        InputStream inputStream = FileHelper.urlToInputStream(url);
        String addr = s3Service.uploadImage(imageType, inputStream, extension);
        updateProfilePictureAddrById(id, addr);
        return addr;
    }

    public void updateDescriptionById(long id, String description) {
        if (!existsById(id)) {
            throw new InvalidUserException();
        }
        userRepo.updateDescriptionById(id, description);
    }

    public void updateFullNameById(long id, String fullName) {
        if (!existsById(id)) {
            throw new InvalidUserException();
        }
        userRepo.updateFullNameById(id, fullName);
    }

    public void updateEmailById(long id, String email) {
        User user = findById(id);
        if (user == null) {
            throw new InvalidUserException();
        } else if (existsByEmail(email)) {
            throw new UserCollisionException("email already exists");
        } else if (!StringHelper.isEmail(email)) {
            throw new InvalidEmailException();
        }

        try {
            cognitoService.updadeEmailAttribute(user.getUsername(), email);
        } catch (Exception e) {
            throw new CognitoException(e.getMessage());
        }

        userRepo.updateEmailById(id, email);
    }

    public void updateUsernameById(long id, String username) {
        User user = findById(id);
        if(user == null) {
            throw new InvalidUserException();
        } else if (existsByUsername(username)) {
            throw new UserCollisionException("username already exists");
        } else if (!StringHelper.isValidUsername(username)) {
            throw new InvalidUsernameException();
        }

        try {
            cognitoService.updatePreferredUsername(user.getUsername(), username);
        } catch (Exception e) {
            throw new CognitoException(e.getMessage());
        }

        userRepo.updateUsernameById(id, username);
    }

    public void changePassword(long id, ChangePasswordDTO changePasswordDTO, HttpServletRequest servletRequest) {
        if (!existsById(id)) {
            throw new InvalidUserException();
        }
        if (!cognitoService.isValidPassword(changePasswordDTO.getNewPassword())) {
            throw new InvalidPassword();
        }

        cognitoService.changePassword(changePasswordDTO.getOldPassword(), changePasswordDTO.getNewPassword(),
                servletRequest);
    }

    public List<SearchDTO> searchAutocomplete(String query) {
        List<SearchDTO> results = new ArrayList<>();

        List<User> users = userRepo.searchByQuery(query);
        for (User user : users) {
            SearchDTO dto =
                    new SearchDTO(user.getId(), user.getUsername(), user.getFullName(), user.getProfileImageAddr());
            results.add(dto);
        }

        return results;
    }

    /**
     * saves a new user to the database based on cognito attributes provided by facebook
     */
    public User createUserFromCognitoAttrFacebook(Map<String, String> attributesMap, String cogUsername) {

        if(attributesMap.containsKey("custom:id")) {
            return findById(Long.parseLong(attributesMap.get("custom:id")));
        }

        String username = cogUsername;
        String email = attributesMap.get("email");
        String fullName = attributesMap.get("name");
        Date birthDate = new Date(0);
        username = usernameFromFullName(fullName);

        User user = new User(email, username, fullName, birthDate);
        user = create(user);

        /* try {
            setProfilePictureFromFacebook(user.getId(), attributesMap.get("picture"));
        } catch (Exception e) {
            e.printStackTrace();
        } */

        return user;
    }

    /**
     * saves a new user to the database based on cognito attributes provided by apple
     */
    public User createUserFromCognitoAttrApple(Map<String, String> attributesMap, String cogUsername) {

        if(attributesMap.containsKey("custom:id")) {
            return findById(Long.parseLong(attributesMap.get("custom:id")));
        }

        String username = cogUsername;
        String email = attributesMap.get("email");
        username = usernameFromEmail(email);
        String fullName = username;
        Date birthDate = new Date(0);

        if(existsByEmail(email)) {
            cognitoService.deleteUser(cogUsername);
            throw new UserCollisionException("apple account email already exists");
        }

        User user = new User(email, username, fullName, birthDate);
        user = create(user);

        return user;
    }

    public String setProfilePictureFromFacebook(long id, String cognitoPictureString) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Map<String, String>> map = mapper.readValue(cognitoPictureString, Map.class);
        String profileImageUrl = map.get("data").get("url");
        String addr = updateProfileImage(id, profileImageUrl);
        return addr;
    }

    public String usernameFromFullName(String fullName) {
        String baseUsername = fullName.toLowerCase();
        baseUsername = baseUsername.replaceAll(" ", ".");
        String username = uniqueUsernameFromBase(baseUsername);
        return username;
    }

    public String usernameFromEmail(String email) {
        String baseUsername = email.substring(0, email.indexOf("@"));
        String username = uniqueUsernameFromBase(baseUsername);
        return username;
    }

    /**
     * create a unique username from a base username (using a number suffix)
     */
    public String uniqueUsernameFromBase(String baseUsername) {
        String username = baseUsername;
        int suffix = 0;
        while(existsByUsername(username)) {
            suffix++;
            username = baseUsername + suffix;
        }

        if(suffix >= 5) {
            logger.warn("Username suffix is bigger than 5! suffix value: " + suffix);
        }

        return username;
    }

}
