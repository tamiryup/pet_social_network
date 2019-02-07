package com.tamir.petsocialnetwork;

import com.tamir.petsocialnetwork.filters.AuthenticationFilter;
import com.tamir.petsocialnetwork.services.AuthService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Autowired
    @Qualifier("filterAuthService")
    private AuthService filterAuthService;

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);

        CorsFilter corsFilter = new CorsFilter(source);
        FilterRegistrationBean<CorsFilter> registrationBean =
                new FilterRegistrationBean<>(corsFilter);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authFilter(
            @Value("${ps.should-authenticate}") boolean shouldAuth) {
        AuthenticationFilter authFilter = new AuthenticationFilter(filterAuthService);
        FilterRegistrationBean<AuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(authFilter);
        registrationBean.addUrlPatterns("/user-info/*");
        registrationBean.addUrlPatterns("/social/*");
        registrationBean.addUrlPatterns("/settings/*");

        registrationBean.setEnabled(shouldAuth);

        return registrationBean;
    }
}
