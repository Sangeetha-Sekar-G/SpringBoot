package com.tcs.model;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Configuration
@ComponentScan("com.tcs")
@Import(PropertyPlaceholderConfig.class)
public class AppConfig {
    // comes from app.config
    public static String WALLIX_USERNAME;
    public static String WALLIX_PASSWORD;
    public static String MACHINE2USE;
    public static String TARGET_LOCAL_DIR;

    public static boolean CLEAR_PREV_LOGS = false;
    public static boolean DOWNLOAD_PREV_DAY_LOGS = false;
    public static String DATE_2_USE;
    public static boolean DOWNLOAD_WEEKEND_LOGS;
    public static boolean TODAY_FIRST_2_HOURS;


    @Bean
    public AppConfig appConfigBean(){
        this.initAppConfig();
        return this;
    }

    @PostConstruct
    public void initAppConfig() {
        System.out.println("--------------- App Properties Start ---------------");
        Properties prop = new Properties();
        String fileName = System.getProperty("user.dir")+ File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "app.config";
        try(FileInputStream fis = new FileInputStream(fileName))  {
            prop.load(fis);
        } catch(FileNotFoundException ex)        {
            System.out.println("No property file found!");
        } catch(IOException ex){
            System.out.println("IOException while reading property file!");
        }
        WALLIX_USERNAME = prop.getProperty("username");
        WALLIX_PASSWORD = prop.getProperty("password");
        MACHINE2USE = prop.getProperty("machine").toUpperCase();
        TARGET_LOCAL_DIR = prop.getProperty("targetdir");
        CLEAR_PREV_LOGS = "true".equalsIgnoreCase(prop.getProperty("clearPreviousLocalLogs"));
        DOWNLOAD_PREV_DAY_LOGS = "true".equalsIgnoreCase(prop.getProperty("downloadPreviousDayLog"));
        DATE_2_USE = prop.getProperty("dateToUse");
        DOWNLOAD_WEEKEND_LOGS = "true".equalsIgnoreCase(prop.getProperty("downloadWeekendLogs"));
        TODAY_FIRST_2_HOURS = "true".equalsIgnoreCase(prop.getProperty("todayFirst2Hours"));
        System.out.println("--------------- App Properties End ---------------");
    }


}
