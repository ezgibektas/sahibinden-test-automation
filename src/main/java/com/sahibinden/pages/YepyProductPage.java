package com.sahibinden.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.List;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import org.openqa.selenium.By;

@Component
public class YepyProductPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(YepyProductPage.class);
    private static final int DEFAULT_TIMEOUT = 10;

    // Desktop view elements
    @FindBy(xpath = "//div[@class='refurbishment-content-box']")
    private WebElement firstPhone;

    @FindBy(xpath = "(//div[@class='sui-color-emphasis-high sui-global-surface-body-lead-semibold searchResultsTagAttributeValue'])[1]")
    private WebElement firstPhoneTitle;

    @FindBy(xpath = "(//div[@class='sui-color-primary-default sui-global-surface-body-lead-bold searchResultsPriceValue'])[1]")
    private WebElement firstPhonePrice;

    @FindBy(xpath = "//div[@class='refurb-section-infos']//h1[contains(@class,'sui-global-surface-page-title refurb-section-infos-title sui-color-emphasis-high')]")
    private WebElement firstPhoneDetailTitle;

    @FindBy(css = ".sui-global-surface-page-title.sui-color-primary-default.refurb-section-infos-price")
    private WebElement firstPhoneDetailPrice;

    @FindBy(xpath = "//h1[@class='sui-global-surface-page-title']")
    private WebElement refurbishedAppleTitle;

    @FindBy(css = "div[class='sui-color-emphasis-high sui-global-surface-body-lead-semibold searchResultsTagAttributeValue']")
    private List<WebElement> applePhoneList;

    // Mobile view elements
    @FindBy(xpath = "//h2[@class='refurbishment-item-content-title sui-global-surface-body-semibold sui-color-emphasis-high']")
    private List<WebElement> mobileApplePhoneList;

    @FindBy(xpath = "//div[@class='refurbishment-item-content-price sui-global-surface-body-bold sui-color-primary-default']")
    private List<WebElement> mobileApplePriceList;

    private boolean isResponsiveMode = false;

    @Autowired
    public YepyProductPage(@Qualifier("webDriver") WebDriver driver) {
        super(driver);
    }

    public void setResponsiveMode(boolean isResponsive) {
        this.isResponsiveMode = isResponsive;
        logger.info("Responsive mode set to: {}", isResponsive ? "enabled" : "disabled");
    }

    public boolean detectResponsiveMode() {
        int width = driver.manage().window().getSize().getWidth();
        boolean isResponsive = width <= 767;
        setResponsiveMode(isResponsive);
        logger.info("Browser width: {}px, Responsive mode: {}", width, isResponsive);
        return isResponsive;
    }

    public boolean clickFirstPhone() {
        try {
            logger.info("Clicking on first phone listing");
            waitForElementVisible(firstPhone);

            String title = getFirstPhoneTitle();
            String price = getFirstPhonePrice();
            logger.info("First phone title: {}, price: {}", title, price);

            clickElement(firstPhone);
            
            // Detay sayfası başlığının görünür olmasını bekle
            WebElement detailTitle = waitForElementToBeVisible(By.xpath("//div[@class='refurb-section-infos']//h1[contains(@class,'sui-global-surface-page-title refurb-section-infos-title sui-color-emphasis-high')]"));
            if (detailTitle != null) {
                logger.info("Successfully navigated to phone detail page");
                waitForPageLoad();
                return true;
            } else {
                logger.error("Detail page title element not found after navigation");
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to click on first phone: {}", e.getMessage());
            return false;
        }
    }

    private WebElement waitForElementToBeVisible(By locator) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
            logger.warn("Element not visible: {}", locator);
            return null;
        }
    }

    public String getFirstPhoneTitle() {
        return getElementText(firstPhoneTitle);
    }

    public String getFirstPhonePrice() {
        return getElementText(firstPhonePrice);
    }

    public String getDetailPageTitle() {
        return getElementText(firstPhoneDetailTitle);
    }

    public String getDetailPagePrice() {
        return getElementText(firstPhoneDetailPrice);
    }

    public boolean waitForRefurbishedAppleTitle(int timeoutInSeconds) {
        try {
            logger.info("Apple title visibility wait started");
            detectResponsiveMode();
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            
            if (isResponsiveMode) {
                wait.until(ExpectedConditions.visibilityOfAllElements(mobileApplePhoneList));
                logger.info("Mobile view Apple phones list visible");
            } else {
                wait.until(ExpectedConditions.visibilityOf(refurbishedAppleTitle));
                String title = getElementText(refurbishedAppleTitle);
                logger.info("Refurbished Apple title visible: {}", title);
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Apple title not visible within {} seconds: {}", 
                    timeoutInSeconds, e.getMessage());
            return false;
        }
    }

    public boolean verifyAllPhonesAreApple() {
        try {
            logger.info("Checking all phones are Apple");
            detectResponsiveMode();
            
            List<WebElement> phoneElements = getPhoneElements();
            if (phoneElements.isEmpty()) {
                logger.warn("No phone found");
                return false;
            }
            
            return verifyPhoneBrands(phoneElements);
        } catch (Exception e) {
            logger.error("Error checking Apple phones: {}", e.getMessage());
            return false;
        }
    }

    private List<WebElement> getPhoneElements() {
        if (isResponsiveMode) {
            waitForElementVisible(mobileApplePhoneList.get(0));
            logger.info("Mobile view {} phone found", mobileApplePhoneList.size());
            return mobileApplePhoneList;
        } else {
            waitForElementVisible(applePhoneList.get(0));
            logger.info("Desktop view {} phone found", applePhoneList.size());
            return applePhoneList;
        }
    }

    private boolean verifyPhoneBrands(List<WebElement> phoneElements) {
        boolean allAreApple = true;
        int count = 0;
        
        for (WebElement phone : phoneElements) {
            String phoneTitle = getElementText(phone).toLowerCase();
            count++;
            
            if (!isApplePhone(phoneTitle)) {
                logger.error("Non-Apple phone found: {}", phoneTitle);
                allAreApple = false;
            } else {
                logger.info("Apple phone verified: {}", phoneTitle);
            }
        }
        
        logVerificationResults(count, allAreApple);
        return allAreApple;
    }

    private boolean isApplePhone(String phoneTitle) {
        return phoneTitle.contains("iphone") || phoneTitle.contains("apple");
    }

    private void logVerificationResults(int count, boolean allAreApple) {
        logger.info("Total {} phones checked, {} Apple phones verified", 
                count, allAreApple ? count : (count - 1));
    }

    public String getElementText(WebElement element) {
        try {
            waitForElementVisible(element);
            return element.getText().trim();
        } catch (Exception e) {
            logger.error("Failed to get element text: {}", e.getMessage());
            return "";
        }
    }

    public boolean verifyPriceConsistency() {
        try {
            logger.info("Verifying price consistency between listing and detail pages");
            
            // Liste sayfasındaki fiyatı al
            String listingPrice = getFirstPhonePrice();
            logger.info("Listing page price: {}", listingPrice);
            
            // İlk telefonu tıkla
            if (!clickFirstPhone()) {
                logger.error("Failed to navigate to detail page");
                return false;
            }
            
            // Detay sayfasındaki fiyatı al
            String detailPrice = getDetailPagePrice();
            logger.info("Detail page price: {}", detailPrice);
            
            // Fiyatları temizle ve karşılaştır
            String cleanListingPrice = listingPrice.replaceAll("[^0-9]", "");
            String cleanDetailPrice = detailPrice.replaceAll("[^0-9]", "");
            
            if (cleanListingPrice.equals(cleanDetailPrice)) {
                logger.info("Price consistency verified successfully");
                return true;
            } else {
                logger.error("Price mismatch: listing '{}' vs detail '{}'", listingPrice, detailPrice);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error verifying price consistency: {}", e.getMessage());
            return false;
        }
    }
} 