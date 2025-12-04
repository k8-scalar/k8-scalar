package com.example.mt_api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.mt_api.filter.LoggingInterceptor;

// @Configuration
// public class WebConfig implements WebMvcConfigurer {
//     @Autowired
//     private LoggingInterceptor loggingInterceptor;

//     @Override
//     public void addInterceptors(InterceptorRegistry registry) {
//         registry.addInterceptor(loggingInterceptor);
//     }
// }
