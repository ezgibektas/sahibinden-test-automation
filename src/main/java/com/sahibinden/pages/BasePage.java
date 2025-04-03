package com.sahibinden.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.openqa.selenium.support.FindBy;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public abstract class BasePage {
    private static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    private static final int RESPONSIVE_BREAKPOINT = 767;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;
    protected JavascriptExecutor js;
    protected boolean isResponsiveMode;

    @Value("${test.base.url}")
    protected String baseUrl;

    @FindBy(xpath = "//button[@id='onetrust-accept-btn-handler']")
    private WebElement acceptCookiesButton;
    
    @FindBy(css = "iframe[title='Cloudflare Security']")
    protected WebElement cloudflareFrame;
    
    @FindBy(css = "g-raised-button[jsaction='click:cOuCgd']")
    protected WebElement locationDismissButton;

    @Autowired
    public BasePage(@Qualifier("webDriver") WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.actions = new Actions(driver);
        this.js = (JavascriptExecutor) driver;
        PageFactory.initElements(driver, this);
        this.isResponsiveMode = false;
    }

    protected boolean setResponsiveMode(boolean isResponsive) {
        try {
            this.isResponsiveMode = isResponsive;
            logInfo("Responsive mode set to: {}", isResponsive ? "enabled" : "disabled");
            return true;
        } catch (Exception e) {
            logError("Failed to set responsive mode: {}", e.getMessage());
            return false;
        }
    }

    protected boolean detectResponsiveMode() {
        int width = driver.manage().window().getSize().getWidth();
        boolean isResponsive = width <= RESPONSIVE_BREAKPOINT;
        setResponsiveMode(isResponsive);
        logInfo("Browser width: {}px, Responsive mode: {}", width, isResponsive);
        return isResponsive;
    }

    protected void logInfo(String message, Object... args) {
        logger.info(message, args);
    }

    protected void logError(String message, Object... args) {
        logger.error(message, args);
    }

    protected void logWarning(String message, Object... args) {
        logger.warn(message, args);
    }

    public boolean acceptCookies() {
        try {
            logger.info("Attempting to accept cookies");
            WebElement cookieButton = waitForElementToBeClickable(By.id("onetrust-accept-btn-handler"));
            if (cookieButton != null) {
                cookieButton.click();
                logger.info("Successfully clicked cookie accept button");
            } else {
                logger.warn("Cookie accept button not found");
            }
            return true;
        } catch (Exception e) {
            logger.error("Error accepting cookies: {}", e.getMessage());
            return false;
        }
    }

    protected void waitForElementVisible(WebElement element) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
        } catch (Exception e) {
            logger.error("Element is not visible: {}", element);
            throw e;
        }
    }

    protected void waitForElementToBeClickable(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
        } catch (Exception e) {
            logger.error("Element is not clickable: {}", element);
            throw e;
        }
    }

    protected WebElement waitForElementToBeClickable(By locator) {
        try {
            logger.info("Waiting for element to be clickable: {}", locator);
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            logger.info("Element is now clickable: {}", locator);
            return element;
        } catch (Exception e) {
            logger.warn("Element is not clickable: {}", locator);
            return null;
        }
    }

    protected void waitForPageLoad() {
        try {

            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState")
                    .equals("complete"));

            Boolean jQueryReady = (Boolean) js.executeScript("return typeof jQuery != 'undefined' && jQuery.active == 0");
            if (jQueryReady != null && jQueryReady) {
                wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return jQuery.active == 0"));
            }

            waitForCondition(driver -> {
                Boolean networkIdle = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "return window.performance.getEntriesByType('resource').filter(r => !r.responseEnd).length === 0"
                );
                return networkIdle != null && networkIdle;
            }, 5, "Network idle");
            
            logger.info("Page load completed successfully");
        } catch (Exception e) {
            logger.error("Page load timeout: {}", e.getMessage());
            throw e;
        }
    }

    protected void clickElement(WebElement element) {
        try {
            waitForElementToBeClickable(element);
            element.click();
            logger.info("Clicked element successfully");
        } catch (Exception e) {
            logger.error("Failed to click element: {}", e.getMessage());
            throw e;
        }
    }

    protected void sendKeys(WebElement element, String text) {
        try {
            waitForElementVisible(element);
            element.clear();
            element.sendKeys(text);
            logger.info("Sent keys to element: {}, text: {}", element, text);
        } catch (Exception e) {
            logger.error("Failed to send keys to element: {}", element);
            throw e;
        }
    }

    protected String getElementText(WebElement element) {
        try {
            waitForElementVisible(element);
            String text = element.getText();
            if (text == null || text.isEmpty()) {
                text = element.getAttribute("value");
            }
            if (text == null || text.isEmpty()) {
                text = (String) js.executeScript("return arguments[0].textContent;", element);
            }
            return text != null ? text : "";
        } catch (Exception e) {
            logger.error("Failed to get text from element: {}", e.getMessage());
            return "";
        }
    }

    protected boolean isElementDisplayed(WebElement element) {
        try {
            boolean exists = (Boolean) js.executeScript(
                "return arguments[0] !== null && typeof(arguments[0]) != 'undefined' && " +
                "arguments[0].offsetParent !== null", element);
            
            if (!exists) {
                return false;
            }
            return element.isDisplayed();
        } catch (Exception e) {
            logger.error("Failed to check if element is displayed: {}", element);
            return false;
        }
    }

    public boolean switchToWindow(String windowHandle) {
        try {
            logger.info("Switching to window with handle: {}", windowHandle);
            driver.switchTo().window(windowHandle);
            waitForPageLoad();
            logger.info("Successfully switched to window with URL: {}", driver.getCurrentUrl());
            return true;
        } catch (Exception e) {
            logger.error("Failed to switch to window: {}", e.getMessage());
            return false;
        }
    }

    protected void waitForCondition(Function<WebDriver, Boolean> condition, int timeoutInSeconds, String message) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                .withMessage(message)
                .until(condition);
            logger.info("Condition met: {}", message);
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for condition: {}", message);
            throw e;
        }
    }

    protected void waitForElementVisible(WebElement element, String reason) {
        try {
            logger.info("Waiting for element to be visible: {}", reason);
            wait.until(ExpectedConditions.visibilityOf(element));
            logger.info("Element is now visible: {}", reason);
        } catch (Exception e) {
            logger.error("Element is not visible ({}): {}", reason, e.getMessage());
            throw e;
        }
    }

    protected <T> T execute(String actionName, Supplier<T> action) {
        try {
            logger.info("Starting: {}", actionName);
            T result = action.get();
            logger.info("Completed: {}", actionName);
            return result;
        } catch (Exception e) {
            logger.error("Failed to {}: {}", actionName, e.getMessage());
            throw new RuntimeException("Failed to " + actionName, e);
        }
    }

    protected void execute(String actionName, Runnable action) {
        try {
            logger.info("Starting: {}", actionName);
            action.run();
            logger.info("Completed: {}", actionName);
        } catch (Exception e) {
            logger.error("Failed to {}: {}", actionName, e.getMessage());
            throw new RuntimeException("Failed to " + actionName, e);
        }
    }
}