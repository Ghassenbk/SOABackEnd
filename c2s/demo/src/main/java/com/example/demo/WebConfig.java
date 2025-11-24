package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // THIS IS THE ONLY ONE THAT WORKS 100% IN DEV + PROD
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:src/main/resources/static/images/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/pdfs/**")
                .addResourceLocations("file:src/main/resources/static/images/")  // Wait — NO!
                .addResourceLocations("file:src/main/resources/static/pdfs/")
                .setCachePeriod(3600);
    }
}