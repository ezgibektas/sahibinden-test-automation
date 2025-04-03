package com.sahibinden.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Component
public class WebDriverManager {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverManager.class);
    private static final int DEFAULT_TIMEOUT = 30;
    
    private final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    
    @Value("${browser.type:chrome}")
    private String browserType;
    
    @Value("${browser.headless:false}")
    private boolean headless;
    
    @Value("${browser.implicit.wait:20}")
    private int implicitWait;
    
    @Value("${browser.page.load.timeout:30}")
    private int pageLoadTimeout;
    
    @Value("${browser.script.timeout:30}")
    private int scriptTimeout;
    
    @Value("${selenium.grid.url:}")
    private String gridUrl;

    @Value("${selenium.use.grid:false}")
    private boolean useGrid;
    
    @Autowired
    private Environment environment;
    

    @Bean(destroyMethod = "quit")
    @Qualifier("webDriver")
    public WebDriver webDriver() {
        boolean isResponsive = environment.getActiveProfiles().length > 0 && 
                             environment.getActiveProfiles()[0].equals("responsive-test");
        logger.info("Creating WebDriver bean with responsive mode: {}", isResponsive);
        
        WebDriver driver = createDriver(isResponsive);
        driverThreadLocal.set(driver);
        return driver;
    }
    

    @Bean
    @Qualifier("webDriverWait")
    public WebDriverWait webDriverWait(WebDriver webDriver) {
        logger.info("Creating WebDriverWait bean with timeout: {}s", DEFAULT_TIMEOUT);
        return new WebDriverWait(webDriver, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }
    

    @PostConstruct
    public void init() {
        logger.info("Initializing WebDriverManager with browser: {}, headless: {}", browserType, headless);
    }
    

    public WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        
        if (driver == null) {
            boolean isResponsive = environment.getActiveProfiles().length > 0 && 
                                 environment.getActiveProfiles()[0].equals("responsive-test");
            driver = createDriver(isResponsive);
            driverThreadLocal.set(driver);

            logger.info("Thread ID: {}, Created new driver: {}", 
                Thread.currentThread().getId(), driver.hashCode());
        }
        
        return driver;
    }

    private WebDriver createDriver(boolean isMobile) {
        try {
            logger.info("Creating {} driver with browser: {}, Grid Enabled: {}", 
                isMobile ? "mobile" : "desktop", browserType, useGrid);
            
            WebDriver driver;
            
            if (useGrid && gridUrl != null && !gridUrl.trim().isEmpty()) {
                driver = createRemoteDriver(isMobile);
                logger.info("Created remote driver with Grid URL: {}", gridUrl);
            } else {
                driver = createLocalDriver(isMobile);
                logger.info("Created local driver");
            }
            
            configureDriver(driver, isMobile);
            return driver;
        } catch (Exception e) {
            logger.error("Failed to create WebDriver: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }

    private WebDriver createRemoteDriver(boolean isMobile) {
        try {
            logger.info("Creating {} remote driver using Selenium Grid at {}", 
                isMobile ? "mobile" : "desktop", gridUrl);
            
            ChromeOptions options = createChromeOptions(isMobile);
            options.setCapability("se:name", "Thread-" + Thread.currentThread().getId());
            
            WebDriver remoteDriver = new RemoteWebDriver(new URL(gridUrl), options);
            logger.info("Remote driver created successfully: {}", remoteDriver);
            return remoteDriver;
        } catch (Exception e) {
            logger.warn("Failed to connect to Grid, falling back to local driver: {}", e.getMessage());
            return createLocalDriver(isMobile);
        }
    }
    

    private WebDriver createLocalDriver(boolean isMobile) {
        logger.info("Creating local {} driver with browser: {}", 
            isMobile ? "mobile" : "desktop", browserType);
            
        return switch (browserType.toLowerCase()) {
            default -> new ChromeDriver(createChromeOptions(isMobile));
        };
    }

    private ChromeOptions createChromeOptions(boolean isMobile) {
        ChromeOptions options = new ChromeOptions();

        options.addArguments(
            "--disable-notifications",
            "--disable-popup-blocking",
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--remote-allow-origins=*",
            "--disable-extensions",
            "--disable-infobars"
        );
        
        if (headless) {
            options.addArguments("--headless=new");
        }
        
        if (isMobile) {
            Map<String, Object> deviceMetrics = new HashMap<>();
            deviceMetrics.put("width", 375);
            deviceMetrics.put("height", 812);
            deviceMetrics.put("pixelRatio", 3.0);
            
            Map<String, Object> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceMetrics", deviceMetrics);
            mobileEmulation.put("userAgent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");
            
            options.setExperimentalOption("mobileEmulation", mobileEmulation);
            options.addArguments("--window-size=375,812");
        }
        
        return options;
    }

    private void configureDriver(WebDriver driver, boolean isMobile) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(scriptTimeout));
        
        if (!isMobile && !headless) {
            driver.manage().window().maximize();
        }
        
        logger.info("WebDriver configured with timeouts - implicit: {}s, pageLoad: {}s, script: {}s", 
            implicitWait, pageLoadTimeout, scriptTimeout);
    }

    public void closeDriver() {
        WebDriver driver = driverThreadLocal.get();
        
        if (driver != null) {
            try {
                driver.quit();
                logger.info("WebDriver instance closed successfully");
            } catch (Exception e) {
                logger.error("Error closing WebDriver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        closeDriver();
        logger.info("WebDriverManager cleanup completed");
    }
} 