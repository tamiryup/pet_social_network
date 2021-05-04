package com.tamir.followear;

import com.tamir.followear.filters.AuthenticationFilter;
import com.tamir.followear.filters.CsrfFilter;
import com.tamir.followear.services.AuthService;
import com.tamir.followear.services.CsrfService;
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
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Configuration
public class CommonBeanConfig {

    @Getter
    private final static int numPostsPerFeedRequest = 40;

    @Getter
    private final static int streamFeedRequestsLimit = 10;

    @Getter
    private final static int maxStreamActivitiesPerFeedRequest = 100;

    @Getter
    private final static String defaultProfileImageAddr = "images/profile-images/default.jpg";

    @Getter
    private final static String defaultUserDescription = "";

    @Getter
    private final static int readFollowersRequestLimit = 15;

    @Getter
    private final static String terminaLinkSuffix = "?utm_source=IG&utm_medium=Followear%20platform&utm_campaign=amitca";

    @Autowired
    @Qualifier("filterAuthService")
    private AuthService filterAuthService;

    @Autowired
    private CsrfService csrfService;

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter(
            @Value("${fw.should-allow-cors}") boolean shouldAllowCors){
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

        registrationBean.setEnabled(shouldAllowCors);

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authFilter(
            @Value("${fw.should-authenticate}") boolean shouldAuth) {
        AuthenticationFilter authFilter = new AuthenticationFilter(filterAuthService);
        FilterRegistrationBean<AuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(authFilter);
        registrationBean.addUrlPatterns("/social/*");
        registrationBean.addUrlPatterns("/settings/*");

        registrationBean.setEnabled(shouldAuth);

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<CsrfFilter> csrfFilter(
            @Value("${fw.should-csrf}") boolean shouldCsrf) {
        CsrfFilter csrfFilter = new CsrfFilter(csrfService);
        FilterRegistrationBean<CsrfFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(csrfFilter);
        registrationBean.addUrlPatterns("/user-info/*");
        registrationBean.addUrlPatterns("/social/*");
        registrationBean.addUrlPatterns("/settings/*");

        registrationBean.setEnabled(shouldCsrf);

        return registrationBean;
    }

    /*
     * Define multipartFile maximum size to 10Mb
     */
    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver
                = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(10485760);
        return multipartResolver;
    }

}
