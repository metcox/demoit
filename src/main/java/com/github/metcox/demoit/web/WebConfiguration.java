package com.github.metcox.demoit.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.awt.image.BufferedImage;

@Configuration
public class WebConfiguration {

    @Bean
    public HttpMessageConverter<BufferedImage> imageHttpMessageConverter() {
        return new BufferedImageHttpMessageConverter();
    }

     @Configuration
    public class ApodeixisResourcesConfigurer implements WebMvcConfigurer {
         @Override
         public void addResourceHandlers(ResourceHandlerRegistry registry) {
             // TODO use application args instead of 'sample'
             registry.addResourceHandler("/js/**").addResourceLocations("file:sample/.demoit/js/");
             registry.addResourceHandler("/fonts/**").addResourceLocations("file:sample/.demoit/fonts/");
             registry.addResourceHandler("/images/**").addResourceLocations("file:sample/.demoit/images/");
             registry.addResourceHandler("/media/**").addResourceLocations("file:sample/.demoit/media/");
             registry.addResourceHandler("/style.css").addResourceLocations("file:sample/.demoit/style.css");
             registry.addResourceHandler("/favicon.ico").addResourceLocations("file:sample/.demoit/favicon.ico");
         }
     }

}
