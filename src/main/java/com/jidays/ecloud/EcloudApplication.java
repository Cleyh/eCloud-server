package com.jidays.ecloud;

//import com.jidays.ecloud.util.JwtFilter;

import com.jidays.ecloud.util.JwtFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class EcloudApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcloudApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilter() {
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtFilter());
        registrationBean.addUrlPatterns("/userFile/*", "/socialize/*", "/admin/*"); // Protect /secure/* endpoints
        return registrationBean;
    }

}
