package com.github.metcox.apodeixis.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.awt.image.BufferedImage;

@Configuration
public class WebConfiguration {

    /**
     * @return {@link HttpMessageConverter} for {@link BufferedImage}. This format is output by the QrCode generation.
     */
    @Bean
    public HttpMessageConverter<BufferedImage> imageHttpMessageConverter() {
        return new BufferedImageHttpMessageConverter();
    }

     @Configuration
    public class ApodeixisResourcesConfigurer implements WebMvcConfigurer {
         @Override
         public void addResourceHandlers(ResourceHandlerRegistry registry) {
             // TODO use application args instead of 'sample'
             registry.addResourceHandler("/js/**").addResourceLocations("file:sample/.apodeixis/js/");
             registry.addResourceHandler("/fonts/**").addResourceLocations("file:sample/.apodeixis/fonts/");
             registry.addResourceHandler("/images/**").addResourceLocations("file:sample/.apodeixis/images/");
             registry.addResourceHandler("/media/**").addResourceLocations("file:sample/.apodeixis/media/");
             registry.addResourceHandler("/css/**").addResourceLocations("file:sample/.apodeixis/css/");
             registry.addResourceHandler("/favicon.ico").addResourceLocations("file:sample/.apodeixis/favicon.ico");
         }
     }

}
