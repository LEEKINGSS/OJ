package com.binzc.oj.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                // 允许的前端域名，多个域名可以使用 .allowedOrigins("http://localhost:8080", "http://otherdomain.com")
                .allowedOrigins("http://localhost:8080","http://localhost:8081", "http://10.135.5.186:8081","http://localhost:80","http://121.40.216.152:8080","https://121.40.216.152:8080","https://121.40.216.152:80")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}
