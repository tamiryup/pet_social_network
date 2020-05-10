package com.tamir.followear.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class FirebaseMessagingService implements NotificationService {

    private FirebaseApp firebaseApp;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserDeviceService userDeviceService;

    @PostConstruct
    private void init() throws IOException {
        InputStream serviceAccount = FirebaseMessagingService.class.
                getResourceAsStream("/credentials/followear-70ac3-firebase-adminsdk-gpdix-0706e2e63f.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://followear-70ac3.firebaseio.com")
                .build();

        firebaseApp = FirebaseApp.initializeApp(options);
    }

    private MulticastMessage.Builder buildNotificationMessage(long userId, String message) {
        List<String> registrationTokens = userDeviceService.getUserRegistrationTokens(userId);
        if(registrationTokens.isEmpty()) {
            return null;
        }

        Notification notification = Notification.builder()
                .setTitle("")
                .setBody(message)
                .build();

        return MulticastMessage.builder()
                .setNotification(notification)
                .addAllTokens(registrationTokens);
    }

    private void sendMulticastMessage(MulticastMessage message) {
        FirebaseMessaging.getInstance(firebaseApp).sendMulticastAsync(message);
    }

    @Override
    public void sendFollowNotification(long masterId, long slaveId) {
        String slaveUsername = userService.getUsernameById(slaveId);
        String body = slaveUsername + " started following you";

        MulticastMessage.Builder messageBuilder = buildNotificationMessage(masterId, body);
        if(messageBuilder == null) {
            return;
        }

        MulticastMessage message = messageBuilder.build();
        sendMulticastMessage(message);
    }

    @Override
    public void sendLikeNotification(long likingUserId, long likedPostId) {
        String likingUserUsername = userService.getUsernameById(likingUserId);
        long likedUserId = postService.getPostUserId(likedPostId);
        String body = likingUserUsername + " liked an item you uploaded";

        MulticastMessage.Builder messageBuilder = buildNotificationMessage(likedUserId, body);
        if(messageBuilder == null) {
            return;
        }

        MulticastMessage message = messageBuilder.build();
        sendMulticastMessage(message);
    }
}
