package com.tcs.model;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Loads properties from a file called ${APP_ENV}.properties
 * or default.properties if APP_ENV is not set.
 */
@Configuration
@PropertySource("classpath:${APP_ENV:app}.config")
public class PropertyPlaceholderConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
