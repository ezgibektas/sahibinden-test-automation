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
@ActiveProfiles("responsive-test")
@TestPropertySource(locations = "classpath:application-responsive-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Yepy Mobile Tests")
@Tag("mobile")
public class YepyResponsiveTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(YepyResponsiveTest.class);

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
        logger.info("Setting up YepyResponsiveTest");
        driver = webDriver;
        super.setUp();
        navigationPage.navigateToHomePage();
        navigationPage.acceptCookies();
        logger.info("YepyResponsiveTest setup completed");
    }

    @Test
    @Order(1)
    @DisplayName("Should navigate to Yepy Refurbished Phones page in mobile view")
    public void shouldNavigateToYepyRefurbishedPhones() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("mobile"), 
            "Failed to navigate to Refurbished Phones section in mobile view");
    }

    @Test
    @Order(2)
    @DisplayName("Should sort phones by price high to low in mobile view")
    public void shouldSortPhonesByPriceHighToLow() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("mobile"), 
            "Failed to navigate to Refurbished Phones section in mobile view");
        assertTrue(filterPage.sortByPrice(YepyFilterPage.SortOrder.HIGH_TO_LOW), 
            "Failed to sort by price high to low in mobile view");
        assertTrue(filterPage.verifyPriceSorting(YepyFilterPage.SortOrder.HIGH_TO_LOW), 
            "Prices are not sorted high to low in mobile view");
    }

    @Test
    @Order(3)
    @DisplayName("Should sort phones by price low to high in mobile view")
    public void shouldSortPhonesByPriceLowToHigh() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("mobile"), 
            "Failed to navigate to Refurbished Phones section in mobile view");
        assertTrue(filterPage.sortByPrice(YepyFilterPage.SortOrder.LOW_TO_HIGH), 
            "Failed to sort by price low to high in mobile view");
        assertTrue(filterPage.verifyPriceSorting(YepyFilterPage.SortOrder.LOW_TO_HIGH), 
            "Prices are not sorted low to high in mobile view");
    }

    @Test
    @Order(4)
    @DisplayName("Should filter phones by maximum price in mobile view")
    public void shouldFilterPhonesByMaximumPrice() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("mobile"), 
            "Failed to navigate to Refurbished Phones section in mobile view");
        assertTrue(filterPage.setMaximumPriceFilter("10000"), 
            "Failed to set maximum price filter in mobile view");
        assertTrue(filterPage.verifyAllPhonesAreBelowPrice(10000), 
            "Not all phones are below the maximum price in mobile view");
    }

    @Test
    @Order(5)
    @DisplayName("Should filter and verify Apple phones in mobile view")
    public void shouldFilterAndVerifyApplePhones() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("mobile"), 
            "Failed to navigate to Refurbished Phones section in mobile view");
        assertTrue(filterPage.clickApplePhoneFilter(), 
            "Failed to apply Apple phone filter in mobile view");
        assertTrue(productPage.verifyAllPhonesAreApple(), 
            "Not all phones are Apple products in mobile view");
    }

    @Test
    @Order(6)
    @DisplayName("Should verify price consistency between listing and detail pages in mobile view")
    public void shouldVerifyPriceConsistency() {
        assertTrue(navigationPage.navigateToRefurbishedPhonesSection("mobile"), 
            "Failed to navigate to Refurbished Phones section in mobile view");
        assertTrue(productPage.verifyPriceConsistency(), 
            "Prices are not consistent between listing and detail pages in mobile view");
    }
} 