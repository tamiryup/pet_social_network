package com.tamir.petsocialnetwork;

import com.tamir.petsocialnetwork.filters.AuthenticationFilter;
import lombok.Getter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CommonBeanConfig {

    @Getter
    private final static int numPostsPerFeedRequest = 40;

    @Getter
    private final static String defaultProfileImageAddr = "images/profile-images/default.jpg";

    @Getter
    private final static String defaultUserDescription = "";

    @Getter
    private final static int readFollowersRequestLimit = 15;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsFilter corsFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authFilter() {
        FilterRegistrationBean<AuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new AuthenticationFilter());
        registrationBean.addUrlPatterns("/user-info/*");
        registrationBean.addUrlPatterns("/social/*");
        registrationBean.addUrlPatterns("/settings/*");

        return registrationBean;
    }
}
