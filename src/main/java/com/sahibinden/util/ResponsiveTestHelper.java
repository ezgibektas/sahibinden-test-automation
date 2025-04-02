package com.sahibinden.util;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResponsiveTestHelper {
    private static final Logger logger = LoggerFactory.getLogger(ResponsiveTestHelper.class);

    @Value("${test.responsive.mode:false}")
    private boolean responsiveMode;

    @Value("${test.responsive.width:400}")
    private int responsiveWidth;

    @Value("${test.responsive.height:674}")
    private int responsiveHeight;

    public void configureResponsiveMode(WebDriver driver) {
        if (responsiveMode) {
            logger.info("Setting browser to responsive mode: {}x{}", responsiveWidth, responsiveHeight);
            driver.manage().window().setSize(new Dimension(responsiveWidth, responsiveHeight));
        } else {
            logger.info("Using default browser window size (maximized)");
            driver.manage().window().maximize();
        }
    }

    public boolean isResponsiveMode() {
        return responsiveMode;
    }

    public Map<String, Integer> getResponsiveDimensions() {
        Map<String, Integer> dimensions = new HashMap<>();
        dimensions.put("width", responsiveWidth);
        dimensions.put("height", responsiveHeight);
        return dimensions;
    }
} 