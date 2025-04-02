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
import java.util.ArrayList;
import org.openqa.selenium.By;

@Component
public class YepyFilterPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(YepyFilterPage.class);

    @Autowired
    @Qualifier("webDriver")
    private WebDriver driver;

    @FindBy(xpath = "//a[@id='advancedSorting']")
    private WebElement sortingDropdown;

    @FindBy(css = "a[title='Fiyat: Yüksekten düşüğe']")
    private WebElement priceHighToLowOption;

    @FindBy(css = "a[title='Fiyat: Düşükten yükseğe']")
    private WebElement priceLowToHighOption;

    @FindBy(xpath = "//div[@class='sui-color-primary-default sui-global-surface-body-lead-bold searchResultsPriceValue']")
    private List<WebElement> phoneList;

    @FindBy(css = "a[title='Apple'] h2[class='sui-global-component-value-sm']")
    private WebElement applePhone;

    @FindBy(xpath = "(//input[@class='js-manual-search-input sui-input'])[2]")
    private WebElement highestInputField;

    @FindBy(xpath = "//button[@sui-type='button']")
    private WebElement searchButton;

    @FindBy(xpath = "//select[@id='sorting']")
    private WebElement mobileSortingDropdown;

    @FindBy(xpath = "//option[@value='price_asc']")
    private WebElement mobilePriceHighToLowOption;

    @FindBy(xpath = "//option[@value='price_desc']")
    private WebElement mobilePriceLowToHighOption;

    @FindBy(xpath = "//a[@class='search-filter ']")
    private WebElement mobileFilterButton;

    @FindBy(xpath = "//div[contains(@class, 'filter-section')]//*[contains(text(), 'Fiyat')]")
    private WebElement mobilePriceFilterSection;

    @FindBy(xpath = "//a[@class='mui-btn']")
    private WebElement mobileModelButton;

    @FindBy(xpath = "(//span[@class='sui-global-surface-body-lead sui-color-emphasis-high ng-binding'])[1]")
    private WebElement mobileAppleModelFilter;

    @FindBy(xpath = "//div[@class='refurbishment-brand ng-scope']")
    private WebElement mobileAllFilterButton;

    @FindBy(xpath = "//button[@class='sui-button rfb-btn-bottom']")
    private WebElement mobileFilterApplyButton;

    @FindBy(xpath = "//button[contains(@class, 'sui-button') and contains(text(), 'Sonuçları Göster')]")
    private WebElement mobileShowResultsButton;

    @FindBy(xpath = "//a[@class='mui-btn ng-scope']")
    private WebElement mobilePriceButton;

    @FindBy(xpath = "(//input[@name='price_max'])[1]")
    private WebElement mobileMaxPriceInput;

    @FindBy(xpath = "//button[@class='sui-button rfb-btn-bottom']")
    private WebElement mobileApplyButton;

    private boolean isResponsiveMode = false;

    public enum SortOrder {
        HIGH_TO_LOW,
        LOW_TO_HIGH
    }

    @Autowired
    public YepyFilterPage(@Qualifier("webDriver") WebDriver driver) {
        super(driver);
    }

    public void setResponsiveMode(boolean isResponsive) {
        this.isResponsiveMode = isResponsive;
        logger.info("Responsive mode set to: {}", isResponsive ? "enabled" : "disabled");
    }

    public boolean sortByPrice(SortOrder order) {
        try {
            logger.info("Applying price filter: {}", order);
            detectResponsiveMode();
            
            if (isResponsiveMode) {
                waitForElementVisible(mobileSortingDropdown);
                clickElement(mobileSortingDropdown);
                
                WebElement option = (order == SortOrder.HIGH_TO_LOW) ? 
                    mobilePriceHighToLowOption : mobilePriceLowToHighOption;
                waitForElementVisible(option);
                clickElement(option);
            } else {
                waitForElementVisible(sortingDropdown);
                clickElement(sortingDropdown);
                
                WebElement option = (order == SortOrder.HIGH_TO_LOW) ? 
                    priceHighToLowOption : priceLowToHighOption;
                waitForElementVisible(option);
                clickElement(option);
            }
            
            // Sayfanın yüklenmesi için bekle
            Thread.sleep(3000);
            waitForPageLoad();
            
            logger.info("Price sorting {} applied", order);
            return true;
        } catch (Exception e) {
            logger.error("Failed to sort by price {}: {}", order, e.getMessage());
            return false;
        }
    }

    public boolean verifyPriceSorting(SortOrder expectedOrder) {
        try {
            logger.info("Verifying prices are sorted {}", expectedOrder);
            waitForElementVisible(phoneList.get(0));
            
            List<Long> prices = extractPrices();
            
            if (prices.size() < 2) {
                logger.warn("Not enough valid prices found to verify sorting: {}", prices.size());
                return true;
            }

            boolean sorted = verifyPriceOrder(prices, expectedOrder);
            
            if (sorted) {
                logger.info("Prices are correctly sorted {}", expectedOrder);
            } else {
                logger.error("Prices are NOT sorted {}", expectedOrder);
            }
            
            return sorted;
        } catch (Exception e) {
            logger.error("Error verifying price sorting: {}", e.getMessage());
            return false;
        }
    }

    private List<Long> extractPrices() {
        List<Long> prices = new ArrayList<>();
        try {
            // Önce telefon listesinin yüklenmesini bekle
            waitForElementVisible(phoneList.get(0));
            logger.info("Found {} phone elements", phoneList.size());
            
            for (WebElement phone : phoneList) {
                try {
                    // Fiyat elementini bul
                    WebElement priceElement = phone.findElement(By.xpath(".//div[@class='classified-price-container refurbishment-classified-price-container sui-global-surface-body-lead-bold']"));
                    String priceText = priceElement.getText().trim();
                    logger.info("Raw price text: {}", priceText);
                    
                    String cleanPrice = priceText.replaceAll("[^0-9]", "");
                    logger.info("Cleaned price text: {}", cleanPrice);
                    
                    if (!cleanPrice.isEmpty()) {
                        try {
                            long price = Long.parseLong(cleanPrice);
                            prices.add(price);
                            logger.info("Successfully parsed price: {}", price);
                        } catch (NumberFormatException e) {
                            logger.warn("Could not parse price: {}", priceText);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error processing phone element: {}", e.getMessage());
                }
            }
            
            logger.info("Total prices extracted: {}", prices.size());
        } catch (Exception e) {
            logger.error("Error extracting prices: {}", e.getMessage());
        }
        return prices;
    }

    private boolean verifyPriceOrder(List<Long> prices, SortOrder expectedOrder) {
        for (int i = 0; i < prices.size() - 1; i++) {
            if (expectedOrder == SortOrder.HIGH_TO_LOW && prices.get(i) < prices.get(i + 1)) {
                logger.error("Prices not sorted correctly. Found {} before {}", prices.get(i), prices.get(i + 1));
                return false;
            } else if (expectedOrder == SortOrder.LOW_TO_HIGH && prices.get(i) > prices.get(i + 1)) {
                logger.error("Prices not sorted correctly. Found {} before {}", prices.get(i), prices.get(i + 1));
                return false;
            }
        }
        return true;
    }

    public boolean setMaximumPriceFilter(String maxPrice) {
        try {
            logger.info("Setting maximum price filter to {}", maxPrice);
            detectResponsiveMode();
            
            if (isResponsiveMode) {
                applyMobilePriceFilter(maxPrice);
            } else {
                applyDesktopPriceFilter(maxPrice);
            }
            
            waitForPageLoad();
            logger.info("Successfully set maximum price filter");
            return true;
        } catch (Exception e) {
            logger.error("Error occurred while setting maximum price filter: {}", e.getMessage());
            return false;
        }
    }

    private void applyMobilePriceFilter(String maxPrice) {
        waitForElementVisible(mobileFilterButton);
        clickElement(mobileFilterButton);
        logger.info("Clicked on the mobile filter button");
        
        waitForElementVisible(mobilePriceButton);
        clickElement(mobilePriceButton);
        logger.info("Clicked on the price button");
        
        waitForElementVisible(mobileMaxPriceInput);
        sendKeys(mobileMaxPriceInput, maxPrice);
        logger.info("Entered maximum price: {}", maxPrice);
        
        waitForElementVisible(mobileApplyButton);
        clickElement(mobileApplyButton);
        logger.info("Applied price filter");
    }

    private void applyDesktopPriceFilter(String maxPrice) {
        waitForElementVisible(highestInputField);
        sendKeys(highestInputField, maxPrice);
        logger.info("Entered maximum price: {}", maxPrice);
        
        waitForElementVisible(searchButton);
        clickElement(searchButton);
        logger.info("Clicked on the search button");

        // Sayfanın yüklenmesi için bekle
        try {
            Thread.sleep(5000);
            waitForPageLoad();
            logger.info("Waited for page to load after applying price filter");
        } catch (InterruptedException e) {
            logger.error("Error while waiting for page load: {}", e.getMessage());
        }
    }

    public boolean verifyAllPhonesAreBelowPrice(long maxPrice) {
        try {
            logger.info("Verifying all phones are below the maximum price: {}", maxPrice);
            detectResponsiveMode();
            
            List<Long> prices = extractPrices();
            if (prices.isEmpty()) {
                logger.warn("No phone prices found to verify");
                return false;
            }
            
            boolean allBelowMaxPrice = verifyAllPricesBelowMax(prices, maxPrice);
            
            if (allBelowMaxPrice) {
                logger.info("All phones are below or equal to the maximum price");
            } else {
                logger.error("Some phones are above the maximum price");
            }
            
            return allBelowMaxPrice;
        } catch (Exception e) {
            logger.error("Error occurred while verifying phone prices: {}", e.getMessage());
            return false;
        }
    }

    private boolean verifyAllPricesBelowMax(List<Long> prices, long maxPrice) {
        for (Long price : prices) {
            if (price > maxPrice) {
                logger.error("Found phone above maximum price. Price: {}, Maximum: {}", price, maxPrice);
                return false;
            }
            logger.info("Phone price is below maximum: {}", price);
        }
        return true;
    }

    public boolean clickApplePhoneFilter() {
        try {
            logger.info("Applying Apple phone filter");
            detectResponsiveMode();
            
            if (isResponsiveMode) {
                applyMobileAppleFilter();
            } else {
                applyDesktopAppleFilter();
            }
            
            waitForPageLoad();
            logger.info("Apple phone filter applied successfully");
            return true;
        } catch (Exception e) {
            logger.error("Error occurred while applying Apple phone filter: {}", e.getMessage());
            return false;
        }
    }

    private void applyMobileAppleFilter() {
        waitForElementVisible(mobileFilterButton);
        clickElement(mobileFilterButton);
        logger.info("Clicked on the mobile filter button");
        
        waitForElementVisible(mobileModelButton);
        clickElement(mobileModelButton);
        logger.info("Clicked on the model button");
        
        waitForElementVisible(mobileAppleModelFilter);
        clickElement(mobileAppleModelFilter);
        logger.info("Selected Apple model");
        
        waitForElementVisible(mobileApplyButton);
        clickElement(mobileApplyButton);
        logger.info("Filter applied");
    }

    private void applyDesktopAppleFilter() {
        waitForElementVisible(applePhone);
        clickElement(applePhone);
        logger.info("Applied Apple filter in desktop view");
        
        // Sayfanın yüklenmesi için bekle
        try {
            Thread.sleep(3000);
            waitForPageLoad();
            logger.info("Waited for page to load after applying Apple filter");
        } catch (InterruptedException e) {
            logger.error("Error while waiting for page load: {}", e.getMessage());
        }
    }

    @Override
    public boolean detectResponsiveMode() {
        return super.detectResponsiveMode();
    }
} 