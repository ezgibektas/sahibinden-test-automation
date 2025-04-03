package com.sahibinden.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ApplicationConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Test
    public void contextLoads() {
        assertNotNull(applicationContext, "Application context should not be null");
    }

    @Test
    public void testEnvironmentConfiguration() {
        assertNotNull(environment, "Environment should not be null");
        assertEquals("test", environment.getActiveProfiles()[0], "Active profile should be 'test'");
    }

    @Test
    public void testWebDriverConfiguration() {
        assertTrue(applicationContext.containsBean("webDriver"), "WebDriver bean should be present");
        assertTrue(applicationContext.containsBean("webDriverWait"), "WebDriverWait bean should be present");
    }

    @Test
    public void testPropertyValues() {
        assertNotNull(environment.getProperty("test.base.url"), "test.base.url should be configured");
        assertNotNull(environment.getProperty("test.timeout"), "test.timeout should be configured");
    }
} 