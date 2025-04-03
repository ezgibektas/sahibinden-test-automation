package com.sahibinden.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

@Component
public class YepyNavigationPage extends BasePage {
    private static final Logger logger = LoggerFactory.getLogger(YepyNavigationPage.class);
    private static final String HOME_PAGE_URL = "https://www.sahibinden.com";
    private static final By COOKIE_ACCEPT_BUTTON = By.id("onetrust-accept-btn-handler");
    private static final By REFURBISHED_PHONES_BUTTON = By.xpath("(//a[@class='sui-button banner-section-buttons'])[1]");

    // Desktop view elements
    @FindBy(xpath = "//a[@id='yepy-link-primary']")
    private WebElement yepyLink;

    @FindBy(xpath = "//a[@title='Yenilenmiş telefon al']")
    private WebElement refurbishedPhonesLink;

    @FindBy(xpath = "(//a[@class='sui-button'])[1]")
    private WebElement refurbishedPhonesButton;

    // Mobile view elements
    @FindBy(css = ".sui-navbar__responsive-menu-toggle")
    private WebElement mobileMenuToggle;

    @FindBy(xpath = "//a[@class='refurbishment-home-category mui-btn category-id-0']")
    private WebElement mobileYepyLink;

    @FindBy(xpath = "(//a[@class='sui-button'])[1]")
    private WebElement mobileRefurbishedPhonesLink;

    private boolean isResponsiveMode = false;

    @Autowired
    public YepyNavigationPage(@Qualifier("webDriver") WebDriver driver) {
        super(driver);
    }

    public boolean setResponsiveMode(boolean isResponsive) {
        try {
            this.isResponsiveMode = isResponsive;
            logger.info("Responsive mode set to: {}", isResponsive ? "enabled" : "disabled");
            return true;
        } catch (Exception e) {
            logger.error("Failed to set responsive mode: {}", e.getMessage());
            return false;
        }
    }

    public boolean detectResponsiveMode() {
        int width = driver.manage().window().getSize().getWidth();
        boolean isResponsive = width <= 767;
        setResponsiveMode(isResponsive);
        logger.info("Browser width: {}px, Responsive mode: {}", width, isResponsive);
        return isResponsive;
    }

    public boolean clickLink(String linkType) {
        try {
            logger.info("Clicking on {} link", linkType);
            detectResponsiveMode();
            
            if (isResponsiveMode) {
                clickMobileLink(linkType);
            } else {
                clickDesktopLink(linkType);
            }
            
            waitForPageLoad();
            logger.info("Successfully clicked on {} link", linkType);
            return true;
        } catch (Exception e) {
            logger.error("Error occurred while clicking the {} link: {}", linkType, e.getMessage());
            return false;
        }
    }

    private void clickMobileLink(String linkType) {
        if ("YEPY".equals(linkType)) {
            clickMobileYepyLink();
        } else if ("REFURBISHED_PHONES".equals(linkType)) {
            clickMobileRefurbishedPhonesLink();
        }
    }

    private void clickDesktopLink(String linkType) {
        if ("YEPY".equals(linkType)) {
            clickDesktopYepyLink();
        } else if ("REFURBISHED_PHONES".equals(linkType)) {
            clickDesktopRefurbishedPhonesLink();
        }
    }

    private void clickMobileYepyLink() {
        waitForElementVisible(mobileMenuToggle);
        clickElement(mobileMenuToggle);
        logger.info("Mobile menu opened");
        
        waitForElementVisible(mobileYepyLink);
        clickElement(mobileYepyLink);
        logger.info("Clicked on the mobile Yepy link");
    }

    private void clickMobileRefurbishedPhonesLink() {
        waitForElementVisible(mobileRefurbishedPhonesLink);
        clickElement(mobileRefurbishedPhonesLink);
        logger.info("Clicked on the mobile Refurbished Phones link");
    }

    private void clickDesktopYepyLink() {
        waitForElementVisible(yepyLink);
        clickElement(yepyLink);
        logger.info("Clicked on the desktop Yepy link");
    }

    private void clickDesktopRefurbishedPhonesLink() {
        waitForElementVisible(refurbishedPhonesLink);
        clickElement(refurbishedPhonesLink);
        logger.info("Clicked on the desktop Refurbished Phones link");
    }

    public boolean navigateToHomePage() {
        try {
            logger.info("Starting navigation to home page: {}", HOME_PAGE_URL);

            if (driver == null) {
                logger.error("WebDriver is null");
                return false;
            }

            try {
                driver.manage().deleteAllCookies();
                if (!detectResponsiveMode()) {
                    driver.manage().window().maximize();
                }
                logger.info("Cleared cookies and configured window size");
            } catch (Exception e) {
                logger.warn("Failed to clear cookies or configure window: {}", e.getMessage());

            }

            try {
                driver.get(HOME_PAGE_URL);
                waitForPageLoad();
                
                String currentUrl = driver.getCurrentUrl();
                logger.info("Current URL after navigation: {}", currentUrl);
                
                if (!currentUrl.contains("sahibinden.com")) {
                    logger.error("Navigation failed, URL does not contain sahibinden.com");
                    return false;
                }
                
                logger.info("Successfully navigated to home page");
                return true;
            } catch (Exception e) {
                logger.error("Error during navigation: {}", e.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error during navigation: {}", e.getMessage());
            return false;
        }
    }

    public boolean navigateToRefurbishedPhones(String deviceType, Function<String, Void> takeScreenshot) {
        try {
            logger.info("{} - Starting navigation to Refurbished Phones page", deviceType);
            
            performInitialSetup(deviceType, takeScreenshot);
            navigateToRefurbishedPhonesPage(deviceType, takeScreenshot);
            
            logger.info("{} - Navigation to Refurbished Phones page completed", deviceType);
            return true;
        } catch (Exception e) {
            logger.error("{} - Error during navigation to Refurbished Phones page: {}", deviceType, e.getMessage());
            return false;
        }
    }

    private void performInitialSetup(String deviceType, Function<String, Void> takeScreenshot) {
        acceptCookies();
    }

    private void navigateToRefurbishedPhonesPage(String deviceType, Function<String, Void> takeScreenshot) {
        try {
            String currentWindow = driver.getWindowHandle();
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(currentWindow)) {
                    driver.switchTo().window(windowHandle);
                    logger.info("Switched to new window: {}", windowHandle);
                    break;
                }
            }

            waitForCondition(driver -> {
                return ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState")
                    .equals("complete");
            }, 10, "Page load after window switch");
            
            waitForPageLoad();

            WebElement refurbishedPhonesButton = waitForElementToBeClickable(REFURBISHED_PHONES_BUTTON);
            if (refurbishedPhonesButton != null) {
                clickElement(refurbishedPhonesButton);
                logger.info("Clicked on refurbished phones link");

                waitForCondition(driver -> {
                    String url = driver.getCurrentUrl();
                    return url.contains("/yepy/yenilenmis-telefonlar");
                }, 10, "Navigation to refurbished phones page");
                
                waitForPageLoad();
            } else {
                throw new IllegalStateException("Refurbished phones button not found");
            }

            waitForPageLoad();
            String currentUrl = driver.getCurrentUrl();
            boolean isCorrectUrl = currentUrl.contains("/yepy/yenilenmis-telefonlar");
            logger.info("Current URL: {}, Expected URL pattern: /yepy/yenilenmis-telefonlar, Match: {}",
                currentUrl, isCorrectUrl);

            if (!isCorrectUrl) {
                throw new IllegalStateException("URL mismatch. Expected URL to contain /yepy/yenilenmis-telefonlar, Found: " + currentUrl);
            }
            
            if (takeScreenshot != null) {
                takeScreenshot.apply(deviceType + "_3_refurbished_phones");
            }
        } catch (Exception e) {
            logger.error("Error navigating to refurbished phones page: {}", e.getMessage());
            throw e;
        }
    }

    protected WebElement waitForElementToBeClickable(By locator) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (Exception e) {
            logger.warn("Element not clickable: {}", locator);
            return null;
        }
    }

    public boolean acceptCookies() {
        try {
            WebElement cookieButton = waitForElementToBeClickable(COOKIE_ACCEPT_BUTTON);
            if (cookieButton != null) {
                cookieButton.click();
                logger.info("Cookies accepted");
            } else {
                logger.info("Cookie button not found or not clickable");
            }
            return true;
        } catch (Exception e) {
            logger.info("No cookie dialog found or already handled: {}", e.getMessage());
            return false;
        }
    }

    public boolean navigateToRefurbishedPhonesSection(String deviceType) {
        try {
            logger.info("Navigating to Refurbished Phones section in {} mode", deviceType);
            if (!clickLink("YEPY")) {
                logger.error("Failed to click Yepy link");
                return false;
            }
            if (!navigateToRefurbishedPhones(deviceType, null)) {
                logger.error("Failed to navigate to Refurbished Phones page");
                return false;
            }
            logger.info("Successfully navigated to Refurbished Phones section");
            return true;
        } catch (Exception e) {
            logger.error("Error navigating to Refurbished Phones section: {}", e.getMessage());
            return false;
        }
    }
} 