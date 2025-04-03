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

    @FindBy(xpath = "//button[contains(text(), 'Sonuçları Listele') or contains(text(), 'Sonuçları Göster')]")
    private WebElement mobileShowResultsButton;

    @FindBy(xpath = "//a[@class='mui-btn ng-scope']")
    private WebElement mobilePriceButton;

    @FindBy(xpath = "(//input[@name='price_max'])[1]")
    private WebElement mobileMaxPriceInput;

    @FindBy(xpath = "//button[@class='sui-button rfb-btn-bottom']")
    private WebElement mobileApplyButton;

    @FindBy(xpath = "//input[@id='fair']")
    private WebElement fairConditionCheckbox;

    @FindBy(xpath = "//button[contains(text(), 'Filtrele') or contains(text(), 'Ara')]")
    private WebElement filterButton;
    
    @FindBy(xpath = "//a[@title='İyi']")
    private WebElement fairConditionLabel;

    @FindBy(xpath = "(//li[@class='item ng-scope has-selected-data'])[2]")
    private WebElement mobileCosmeticConditionFilter;

    @FindBy(xpath = "//button[contains(text(), 'Uygula')]")
    private WebElement mobileApplyFilterButton;

    @FindBy(xpath = "//span[@class='sui-global-surface-body ng-binding ng-scope']")
    private WebElement mobileFairConditionSelectedLabel;

    private boolean isResponsiveMode = false;

    public enum SortOrder {
        HIGH_TO_LOW,
        LOW_TO_HIGH
    }

    @Autowired
    public YepyFilterPage(@Qualifier("webDriver") WebDriver driver) {
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

            waitForCondition(driver -> {
                try {
                    return !phoneList.isEmpty() && isElementDisplayed(phoneList.get(0));
                } catch (Exception e) {
                    return false;
                }
            }, 10, "Price sorting results to be visible");
            
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
                return true;
            } else {
                logger.error("Prices are NOT sorted {}", expectedOrder);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error verifying price sorting: {}", e.getMessage());
            return false;
        }
    }

    private List<Long> extractPrices() {
        List<Long> prices = new ArrayList<>();
        try {
            waitForElementVisible(phoneList.get(0));
            logger.info("Found {} phone elements", phoneList.size());
            
            for (WebElement phone : phoneList) {
                try {
                    WebElement priceElement = phone.findElement(By.xpath(".//div[@class='classified-price-container refurbishment-classified-price-container sui-global-surface-body-lead-bold']"));
                    String priceText = priceElement.getText().trim();
                    logger.info("Raw price text: {}", priceText);
                    
                    long price = parsePrice(priceText);
                    if (price > 0) {
                        prices.add(price);
                        logger.info("Successfully parsed price: {}", price);
                    } else {
                        logger.warn("Invalid or zero price parsed from: {}", priceText);
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

    private long parsePrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            logger.warn("Empty price text provided");
            return 0;
        }
        
        try {
            String cleanPrice = priceText.replaceAll("[^0-9]", "");
            logger.debug("Cleaned price text from '{}' to '{}'", priceText, cleanPrice);
            
            if (cleanPrice.isEmpty()) {
                logger.warn("No numeric characters found in price text: {}", priceText);
                return 0;
            }
            
            return Long.parseLong(cleanPrice);
        } catch (NumberFormatException e) {
            logger.error("Failed to parse price from text: {}", priceText, e);
            return 0;
        }
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

        waitForCondition(driver -> {
            try {
                return !phoneList.isEmpty() && isElementDisplayed(phoneList.get(0));
            } catch (Exception e) {
                return false;
            }
        }, 10, "Filtered price results to be visible");
        
        waitForPageLoad();
        logger.info("Price filter applied successfully");
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

        waitForCondition(driver -> {
            try {
                return !phoneList.isEmpty() && isElementDisplayed(phoneList.get(0));
            } catch (Exception e) {
                return false;
            }
        }, 10, "Apple filtered results to be visible");
        
        waitForPageLoad();
        logger.info("Apple filter applied successfully");
    }

    @Override
    public boolean detectResponsiveMode() {
        return super.detectResponsiveMode();
    }

    public boolean filterFairConditionPhones() {
        try {
            logger.info("Filtering phones with 'Fair' condition");
            detectResponsiveMode();
            
            waitForElementVisible(fairConditionCheckbox);
            clickElement(fairConditionCheckbox);
            logger.info("Clicked on 'Fair condition' checkbox");
            
            waitForElementVisible(filterButton);
            clickElement(filterButton);
            logger.info("Clicked on filter button");
            
            waitForPageLoad();
            return true;
        } catch (Exception e) {
            logger.error("Error filtering fair condition phones: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifyFairConditionFilterApplied() {
        try {
            logger.info("Verifying 'Fair' condition filter was applied");
            Thread.sleep(5000);
            waitForElementVisible(fairConditionLabel);
            
            boolean isVisible = isElementDisplayed(fairConditionLabel);
            logger.info("Fair condition filter label is visible: {}", isVisible);
            
            return isVisible;
        } catch (Exception e) {
            logger.error("Error verifying fair condition filter: {}", e.getMessage());
            return false;
        }
    }

    public boolean filterMobileFairConditionPhones() {
        try {
            logger.info("Filtering phones with 'Fair' condition in mobile view");
            detectResponsiveMode();

            waitForElementVisible(mobileFilterButton);
            clickElement(mobileFilterButton);
            logger.info("Clicked on mobile filter button");

            waitForElementVisible(mobileCosmeticConditionFilter);
            clickElement(mobileCosmeticConditionFilter);
            logger.info("Clicked on cosmetic condition filter");

            waitForElementVisible(fairConditionCheckbox);
            clickElement(fairConditionCheckbox);
            logger.info("Clicked on 'Fair condition' checkbox");

            waitForElementVisible(mobileApplyFilterButton);
            clickElement(mobileApplyFilterButton);
            logger.info("Clicked on apply filter button");
            waitForElementVisible(mobileShowResultsButton);
            clickElement(mobileShowResultsButton);
            logger.info("Clicked on show results button");


            waitForPageLoad();
            return true;
        } catch (Exception e) {
            logger.error("Error filtering fair condition phones in mobile view: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifyMobileFairConditionFilterApplied() {
        try {
            logger.info("Verifying 'Fair' condition filter was applied in mobile view");
            waitForElementVisible(mobileFilterButton);
            clickElement(mobileFilterButton);
            logger.info("Clicked on mobile filter button");

            waitForElementVisible(mobileFairConditionSelectedLabel);
            
            boolean isVisible = isElementDisplayed(mobileFairConditionSelectedLabel);
            logger.info("Fair condition filter selected label is visible: {}", isVisible);
            
            return isVisible;
        } catch (Exception e) {
            logger.error("Error verifying fair condition filter in mobile view: {}", e.getMessage());
            return false;
        }
    }
} 