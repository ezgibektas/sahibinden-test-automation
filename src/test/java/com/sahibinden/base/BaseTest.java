package com.sahibinden.base;

import com.sahibinden.util.ResponsiveTestHelper;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.time.Duration;

@SpringBootTest
public abstract class BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    @Autowired(required = false)
    private ResponsiveTestHelper responsiveTestHelper;

    @Value("${test.timeout:30}")
    protected int timeout;

    protected WebDriver driver;

    @BeforeEach
    protected void setUp() {
        try {
            logger.info("Setting up test");
            
            if (driver == null) {
                logger.error("Driver is not initialized. Please inject the appropriate driver in the test class.");
                throw new IllegalStateException("Driver must be initialized before setUp");
            }

            // Configure timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeout));
            
            // Configure window size based on responsive mode
            if (responsiveTestHelper != null && responsiveTestHelper.isResponsiveMode()) {
                logger.info("Configuring responsive mode with dimensions: {}", 
                        responsiveTestHelper.getResponsiveDimensions());
                responsiveTestHelper.configureResponsiveMode(driver);
            } else {
                driver.manage().window().maximize();
            }
            
            logger.info("Browser setup completed");
        } catch (Exception e) {
            logger.error("Error in setup: {}", e.getMessage());
            takeScreenshot("setup_error");
            throw e;
        }
    }

    @AfterEach
    void tearDown() {
        try {
            if (driver != null) {
                driver.quit();
                driver = null;
                logger.info("Browser closed successfully");
            }
        } catch (Exception e) {
            logger.error("Error in tearDown: {}", e.getMessage());
            takeScreenshot("teardown_error");
            throw e;
        }
    }

    /**
     * Takes a screenshot and attaches it to the Allure report
     * @param name The name of the screenshot
     */
    protected void takeScreenshot(String name) {
        try {
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment(name, new ByteArrayInputStream(screenshot));
                logger.info("Screenshot taken: {}", name);
            }
        } catch (Exception e) {
            logger.error("Error taking screenshot: {}", e.getMessage());
        }
    }
} 