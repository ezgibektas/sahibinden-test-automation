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
import java.util.Set;

@Component
public abstract class BasePage {
    private static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    private static final int RESPONSIVE_BREAKPOINT = 767;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;
    protected JavascriptExecutor js;
    protected boolean isResponsiveMode;

    @Value("${test.url}")
    protected String baseUrl;

    @FindBy(xpath = "//button[@id='onetrust-accept-btn-handler']")
    private WebElement acceptCookiesButton;

    @FindBy(css = "textarea[name='q']")
    protected WebElement googleSearchInput;
    
    @FindBy(css = "iframe[title='Cloudflare Security']")
    protected WebElement cloudflareFrame;
    
    @FindBy(css = "form[action='/giris']")
    protected WebElement loginForm;
    
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

    protected void setResponsiveMode(boolean isResponsive) {
        this.isResponsiveMode = isResponsive;
        logInfo("Responsive mode set to: {}", isResponsive ? "enabled" : "disabled");
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

    public void acceptCookies() {
        try {
            logger.info("Checking for cookie consent banner");
            if (isElementDisplayed(acceptCookiesButton)) {
                logger.info("Cookie consent banner found, accepting cookies");
                clickElement(acceptCookiesButton);
                waitForElementInvisible(acceptCookiesButton);
                logger.info("Cookies accepted successfully");
            } else {
                logger.info("No cookie consent banner found");
            }
        } catch (Exception e) {
            logger.error("Failed to accept cookies: {}", e.getMessage());
        }
    }

    public boolean refreshPage(long waitTimeMillis) {
        try {
            logger.info("Sayfa yenileniyor");
            driver.navigate().refresh();
            try {
                waitForPageLoad();
                if (waitTimeMillis > 0) {
                    Thread.sleep(waitTimeMillis);
                }
                
            } catch (InterruptedException e) {
                logger.error("Bekleme sırasında hata: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
            
            logger.info("Sayfa başarıyla yenilendi");
            return true;
        } catch (Exception e) {
            logger.error("Sayfa yenilenirken hata oluştu: {}", e.getMessage());
            return false;
        }
    }

    public boolean refreshPage() {
        return refreshPage(500); // Varsayılan olarak 500ms bekle
    }

    public void navigateTo() {
        try {
            logger.info("Navigating to URL: {}", baseUrl);
            driver.get(baseUrl);
            waitForPageLoad();
            handleAuthentication();
            handlePopups();
            
            logger.info("Successfully navigated to page");
        } catch (Exception e) {
            logger.error("Failed to navigate to page: {}", e.getMessage());
            throw e;
        }
    }


    protected void handleLoginIfNeeded() {
        try {
            if (isElementDisplayed(loginForm)) {
                logger.info("Login form detected, performing login");
                // TODO: Implement login functionality using LoginPage
                logger.info("Login completed");
            } else {
                logger.info("No login form detected or already logged in");
            }
        } catch (Exception e) {
            logger.error("Error handling login: {}", e.getMessage());
        }
    }

    protected void handleLocationPopup() {
        try {
            if (isElementDisplayed(locationDismissButton)) {
                logger.info("Location permission popup detected, dismissing it");
                clickElement(locationDismissButton);
                logger.info("Location permission popup dismissed");
            }
        } catch (Exception e) {
            logger.info("No location permission popup found or already handled");
        }
    }

    protected void handleAuthentication() {
        handleLoginIfNeeded();
    }

    protected void handlePopups() {
        handleLocationPopup();
    }


    @Deprecated
    protected void navigateToBaseUrl() {
        navigateTo();
    }

    protected void waitForElementVisible(WebElement element) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
        } catch (Exception e) {
            logger.error("Element is not visible: {}", element);
            throw e;
        }
    }

    protected void waitForElementClickable(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
        } catch (Exception e) {
            logger.error("Element is not clickable: {}", element);
            throw e;
        }
    }

    protected void waitForElementInvisible(WebElement element) {
        try {
            wait.until(ExpectedConditions.invisibilityOf(element));
        } catch (TimeoutException e) {
            logger.error("Element is still visible: {}", element);
            throw e;
        }
    }

    protected void waitForPageLoad() {
        try {
            wait.until(webDriver -> ((org.openqa.selenium.JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState")
                    .equals("complete"));
        } catch (Exception e) {
            logger.error("Page load timeout: {}", e.getMessage());
            throw e;
        }
    }

    protected void clickElement(WebElement element) {
        try {
            waitForElementClickable(element);
            element.click();
            logger.info("Clicked element: {}", element);
        } catch (Exception e) {
            logger.error("Failed to click element: {}", element);
            throw e;
        }
    }

    protected void clickElementWithJS(WebElement element) {
        try {
            waitForElementVisible(element);
            js.executeScript("arguments[0].click();", element);
            logger.info("Clicked element with JS: {}", element);
        } catch (Exception e) {
            logger.error("Failed to click element with JS: {}", element);
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

    protected void selectByVisibleText(WebElement element, String text) {
        try {
            waitForElementVisible(element);
            org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(element);
            select.selectByVisibleText(text);
            logger.info("Selected text from element: {}, text: {}", element, text);
        } catch (Exception e) {
            logger.error("Failed to select text from element: {}", element);
            throw e;
        }
    }

    protected void scrollToElement(WebElement element) {
        try {
            waitForElementVisible(element);
            js.executeScript("arguments[0].scrollIntoView(true);", element);
            logger.info("Scrolled to element: {}", element);
        } catch (Exception e) {
            logger.error("Failed to scroll to element: {}", element);
            throw e;
        }
    }

    protected void scrollToBottom() {
        try {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            logger.info("Scrolled to bottom of page");
        } catch (Exception e) {
            logger.error("Failed to scroll to bottom");
            throw e;
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

    protected boolean isElementEnabled(WebElement element) {
        try {
            waitForElementVisible(element);
            return element.isEnabled();
        } catch (Exception e) {
            logger.error("Failed to check if element is enabled: {}", element);
            return false;
        }
    }

    protected boolean isElementSelected(WebElement element) {
        try {
            waitForElementVisible(element);
            return element.isSelected();
        } catch (Exception e) {
            logger.error("Failed to check if element is selected: {}", element);
            return false;
        }
    }

    protected void acceptAlert() {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
            logger.info("Accepted alert");
        } catch (Exception e) {
            logger.error("Failed to accept alert");
            throw e;
        }
    }

    protected void dismissAlert() {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().dismiss();
            logger.info("Dismissed alert");
        } catch (Exception e) {
            logger.error("Failed to dismiss alert");
            throw e;
        }
    }

    protected void switchToFrame(WebElement frame) {
        try {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
            logger.info("Switched to frame: {}", frame);
        } catch (Exception e) {
            logger.error("Failed to switch to frame: {}", frame);
            throw e;
        }
    }

    protected void switchToDefaultContent() {
        try {
            driver.switchTo().defaultContent();
            logger.info("Switched to default content");
        } catch (Exception e) {
            logger.error("Failed to switch to default content");
            throw e;
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

    public String getCurrentWindowHandle() {
        return driver.getWindowHandle();
    }

    public Set<String> getWindowHandles() {
        return driver.getWindowHandles();
    }

    public String getCurrentUrl() {
        try {
            String currentUrl = driver.getCurrentUrl();
            logger.info("Current URL: {}", currentUrl);
            return currentUrl;
        } catch (Exception e) {
            logger.error("Failed to get current URL: {}", e.getMessage());
            throw e;
        }
    }

    public boolean switchToNewWindow(String currentWindowHandle) {
        try {
            logger.info("Switching to new window");
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windowHandles = driver.getWindowHandles();
            String newWindowHandle = null;
            for (String handle : windowHandles) {
                if (!handle.equals(currentWindowHandle)) {
                    newWindowHandle = handle;
                    break;
                }
            }
            if (newWindowHandle != null) {
                return switchToWindow(newWindowHandle);
            } else {
                logger.error("Failed to find new window handle");
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to switch to new window: {}", e.getMessage());
            return false;
        }
    }

    protected <T> T retry(Function<WebDriver, T> function, int maxAttempts) {
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                return function.apply(driver);
            } catch (Exception e) {
                attempts++;
                if (attempts == maxAttempts) throw e;
                waitForPageLoad();
            }
        }
        throw new RuntimeException("Max retry attempts reached");
    }

    protected boolean isElementPresent(WebElement element) {
        try {
            return element.isDisplayed() && element.isEnabled();
        } catch (StaleElementReferenceException | NoSuchElementException e) {
            return false;
        }
    }

    public void navigateViaGoogle(String searchTerm) {
        try {
            logger.info("Attempting to navigate via Google search for: {}", searchTerm);
            driver.get("https://www.google.com");
            waitForPageLoad();

            retry(d -> {
                waitForElementVisible(googleSearchInput);
                sendKeys(googleSearchInput, searchTerm);
                googleSearchInput.sendKeys(Keys.ENTER);
                return null;
            }, 3);

            waitForPageLoad();

            WebElement searchResult = null;
            try {
                searchResult = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("h3.LC20lb")
                ));
                logger.info("Found result using h3 title selector");
            } catch (Exception e1) {
                try {
                    searchResult = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//h3[contains(@class, 'LC20lb')]/ancestor::a")
                    ));
                    logger.info("Found result using h3 ancestor selector");
                } catch (Exception e2) {
                    try {
                        searchResult = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(@href, '" + searchTerm + "')]")
                        ));
                        logger.info("Found result using href selector");
                    } catch (Exception e3) {
                        logger.info("Using fallback selector");
                        searchResult = driver.findElement(By.tagName("a"));
                    }
                }
            }
            
            if (searchResult != null) {
                logger.info("Clicking on search result: {}", 
                    searchResult.getAttribute("href") != null ? searchResult.getAttribute("href") : "Unknown URL");
                if (searchResult.getTagName().equalsIgnoreCase("h3")) {
                    logger.info("Found h3 element, finding parent link");
                    js.executeScript("arguments[0].closest('a').click();", searchResult);
                } else {
                    clickElement(searchResult);
                }
            } else {
                throw new RuntimeException("Could not find any suitable search result");
            }

            waitForPageLoad();
            handleAuthentication();
            handlePopups();
            
            logger.info("Successfully navigated via Google search");
        } catch (Exception e) {
            logger.error("Navigation via Google failed: {}", e.getMessage());
            throw new RuntimeException("Failed to navigate via Google", e);
        }
    }
}