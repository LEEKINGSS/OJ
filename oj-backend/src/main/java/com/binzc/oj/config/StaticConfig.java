package com.binzc.oj.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // http://localhost:8080/images/xxx.jpg 映射到 文件系统的 ../images/xxx.jpg
        registry.addResourceHandler("/static/images/**")
                .addResourceLocations("file:static/images/");
    }
}
