package com.tamir.petsocialnetwork.services;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.tamir.petsocialnetwork.AWS.s3.S3Service;
import com.tamir.petsocialnetwork.entities.User;
import com.tamir.petsocialnetwork.enums.ImageType;
import com.tamir.petsocialnetwork.exceptions.InvalidUserException;
import com.tamir.petsocialnetwork.helpers.FileHelper;
import com.tamir.petsocialnetwork.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    UserRepository userRepo;

    @Autowired
    S3Service s3Service;

    public boolean existsByEmail(String email){
        return userRepo.existsByEmail(email);
    }

    public boolean existsByUsername(String username){
        return userRepo.existsByUsername(username);
    }

    public boolean existsById(long id){
        return userRepo.existsById(id);
    }

    public User findByEmail(String email){
        return userRepo.findByEmail(email);
    }

    public User findByUsername(String username){
        return userRepo.findByUsername(username);
    }

    public User create(User user){
        return userRepo.save(user);
    }

    public User update(User user){
        return userRepo.save(user);
    }

    public User findById(long id){
        Optional<User> user = userRepo.findById(id);
        if(!user.isPresent()){
            return null;
        }
        return user.get();
    }

    public List<User> findAllById(List<Long> ids){
        List<User> userList = Lists.newArrayList(userRepo.findAllById(ids));
        List<User> userListPlusDuplicates = new ArrayList<>();
        for(long id : ids){
            User userById = Iterables.tryFind(userList, user -> id==user.getId()).orNull();
            if(userById!=null) //if null than id doesn't exist in database therefore do nothing
                userListPlusDuplicates.add(userById);
        }
        return userListPlusDuplicates;
    }

    public String updateProfilePictureAddrById(long id, String profilePictureAddr){
        User user = findById(id);
        if(user==null){
            throw new InvalidUserException();
        }
        String lastAddr = user.getProfileImageAddr();
        user.setProfileImageAddr(profilePictureAddr);
        update(user);
        return lastAddr;
    }

    public String updateProfileImage(long id, MultipartFile image) throws IOException {
        if(!existsById(id))
            throw new InvalidUserException();
        ImageType imageType = ImageType.ProfileImage;
        String extension = FileHelper.getMultipartFileExtension(image);
        String addr = s3Service.uploadImage(imageType, image, extension);
        String lastAddr = updateProfilePictureAddrById(id, addr);
        s3Service.deleteByKey(lastAddr);
        return addr;
    }

    public void updateDescriptionById(long id, String description){
        if(!existsById(id))
            throw new InvalidUserException();
        userRepo.updateDescriptionById(id, description);
    }

}
