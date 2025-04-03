package com.sahibinden.tests;

import com.sahibinden.base.BaseTest;
import com.sahibinden.pages.YepyNavigationPage;
import com.sahibinden.pages.YepyFilterPage;
import com.sahibinden.pages.YepyProductPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Yepy Desktop Tests")
@Tag("desktop")
public class YepyTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(YepyTest.class);

    @Autowired
    @Qualifier("webDriver")
    private WebDriver webDriver;
    
    @Autowired
    private YepyNavigationPage navigationPage;

    @Autowired
    private YepyFilterPage filterPage;

    @Autowired
    private YepyProductPage productPage;

    @BeforeEach
    protected void setUp() {
        logger.info("Setting up YepyTest");
        driver = webDriver;
        super.setUp();
        navigationPage.navigateToHomePage();
        navigationPage.acceptCookies();
        logger.info("YepyTest setup completed");
    }

    @Test
    @Order(1)
    @DisplayName("Should navigate to Yepy Refurbished Phones page")
    public void shouldNavigateToYepyRefurbishedPhones() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("desktop"), 
            "Failed to navigate to Refurbished Phones section");
        takeScreenshot("refurbished_phones_page");
    }

    @Test
    @Order(2)
    @DisplayName("Should sort phones by price high to low")
    public void shouldSortPhonesByPriceHighToLow() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("desktop"), 
            "Failed to navigate to Refurbished Phones section");
        
        assertTrue(filterPage.sortByPrice(YepyFilterPage.SortOrder.HIGH_TO_LOW), 
            "Failed to sort by price high to low");
        
        assertTrue(filterPage.verifyPriceSorting(YepyFilterPage.SortOrder.HIGH_TO_LOW), 
            "Prices are not sorted high to low");
        takeScreenshot("sort_high_to_low");
    }

    @Test
    @Order(3)
    @DisplayName("Should sort phones by price low to high")
    public void shouldSortPhonesByPriceLowToHigh() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("desktop"), 
            "Failed to navigate to Refurbished Phones section");
        
        assertTrue(filterPage.sortByPrice(YepyFilterPage.SortOrder.LOW_TO_HIGH), 
            "Failed to sort by price low to high");
        
        assertTrue(filterPage.verifyPriceSorting(YepyFilterPage.SortOrder.LOW_TO_HIGH), 
            "Prices are not sorted low to high");
        takeScreenshot("sort_low_to_high");
    }

    @Test
    @Order(4)
    @DisplayName("Should filter phones by maximum price")
    public void shouldFilterPhonesByMaximumPrice() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("desktop"), 
            "Failed to navigate to Refurbished Phones section");
        
        assertTrue(filterPage.setMaximumPriceFilter("10000"), 
            "Failed to set maximum price filter");
        
        assertTrue(filterPage.verifyAllPhonesAreBelowPrice(10000), 
            "Not all phones are below the maximum price");
        takeScreenshot("max_price_filter");
    }

    @Test
    @Order(5)
    @DisplayName("Should filter and verify Apple phones")
    public void shouldFilterAndVerifyApplePhones() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("desktop"), 
            "Failed to navigate to Refurbished Phones section");
        
        assertTrue(filterPage.clickApplePhoneFilter(), 
            "Failed to apply Apple phone filter");
        
        assertTrue(productPage.verifyAllPhonesAreApple(), 
            "Not all phones are Apple products");
        takeScreenshot("apple_filter");
    }

    @Test
    @Order(6)
    @DisplayName("Should verify price consistency between listing and detail pages")
    public void shouldVerifyPriceConsistency() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("desktop"), 
            "Failed to navigate to Refurbished Phones section");
        
        assertTrue(productPage.verifyPriceConsistency(), 
            "Prices are not consistent between listing and detail pages");
        takeScreenshot("price_consistency");
    }

    @Test
    @Order(7)
    @DisplayName("Should filter phones by fair condition")
    public void shouldFilterPhonesByFairCondition() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("desktop"), 
            "Failed to navigate to Refurbished Phones section");
        
        assertTrue(filterPage.filterFairConditionPhones(), 
            "Failed to filter phones by fair condition");
        
        assertTrue(filterPage.verifyFairConditionFilterApplied(), 
            "Fair condition filter label is not visible");
        
        takeScreenshot("fair_condition_filter");
    }
} 