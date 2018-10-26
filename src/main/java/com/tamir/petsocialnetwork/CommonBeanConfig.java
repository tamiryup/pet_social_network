package com.tamir.petsocialnetwork;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CommonBeanConfig {

    private final static int numPostsPerFeedRequest = 40;

    private final static String defaultProfileImageAddr = "images/profile-images/default.jpg";

    private final static String defaultUserDescription = "";

    private final static int readFollowersRequestLimit = 15;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static int getNumPostsPerFeedRequest() {
        return numPostsPerFeedRequest;
    }

    public static String getDefaultProfileImageAddr() {
        return defaultProfileImageAddr;
    }

    public static String getDefaultUserDescription() {
        return defaultUserDescription;
    }

    public static int getReadFollowersRequestLimit() {
        return readFollowersRequestLimit;
    }
}
