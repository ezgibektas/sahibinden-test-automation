package com.sahibinden.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebDriverConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebDriverConfig.class);

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
        return createDriver(isResponsive);
    }

    private WebDriver createDriver(boolean isMobile) {
        try {
            ChromeOptions options = createChromeOptions(isMobile);
            WebDriver driver;

            if (useGrid && gridUrl != null && !gridUrl.trim().isEmpty()) {
                try {
                    logger.info("Creating {} driver using Selenium Grid at {}", 
                        isMobile ? "mobile" : "desktop", gridUrl);
                    driver = new RemoteWebDriver(new URL(gridUrl), options);
                } catch (Exception e) {
                    logger.warn("Failed to connect to Grid, falling back to local driver: {}", e.getMessage());
                    driver = createLocalDriver(options);
                }
            } else {
                logger.info("Creating {} driver using local ChromeDriver", 
                    isMobile ? "mobile" : "desktop");
                driver = createLocalDriver(options);
            }

            configureDriver(driver, isMobile);
            return driver;
        } catch (Exception e) {
            logger.error("Failed to create WebDriver: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }

    private WebDriver createLocalDriver(ChromeOptions options) {
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(options);
    }

    private ChromeOptions createChromeOptions(boolean isMobile) {
        ChromeOptions options = new ChromeOptions();
        
        // Temel Chrome ayarları
        options.addArguments(
            "--start-maximized",
            "--disable-notifications",
            "--disable-popup-blocking",
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-blink-features=AutomationControlled",
            "--disable-extensions",
            "--disable-infobars"
        );

        // Mobile emülasyon ayarları
        if (isMobile) {
            Map<String, Object> deviceMetrics = new HashMap<>();
            deviceMetrics.put("width", 375);
            deviceMetrics.put("height", 812);
            deviceMetrics.put("pixelRatio", 3.0);
            deviceMetrics.put("userAgent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");
            
            Map<String, Object> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceMetrics", deviceMetrics);
            
            options.setExperimentalOption("mobileEmulation", mobileEmulation);
            options.addArguments("--window-size=375,812");
        }

        return options;
    }

    private void configureDriver(WebDriver driver, boolean isMobile) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
        
        if (!isMobile) {
            driver.manage().window().maximize();
        }
    }
} 