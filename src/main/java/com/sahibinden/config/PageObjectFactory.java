package com.sahibinden.config;

import com.sahibinden.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class PageObjectFactory {
    private static final Logger logger = LoggerFactory.getLogger(PageObjectFactory.class);
    
    private final WebDriverManager driverManager;
    
   
    @Autowired
    public PageObjectFactory(WebDriverManager driverManager) {
        this.driverManager = driverManager;
        logger.info("PageObjectFactory initialized with WebDriverManager");
    }
    
   
    public <T extends BasePage> T createPage(Class<T> pageClass) {
        WebDriver driver = driverManager.getDriver();
        logger.info("Creating page: {}", pageClass.getSimpleName());
        
        try {
            T page = pageClass.getConstructor(WebDriver.class).newInstance(driver);
            logger.info("Successfully created page: {}", pageClass.getSimpleName());
            return page;
        } catch (Exception e) {
            logger.error("Failed to create page {}: {}", pageClass.getSimpleName(), e.getMessage());
            throw new RuntimeException("Could not create page: " + pageClass.getName(), e);
        }
    }
} 