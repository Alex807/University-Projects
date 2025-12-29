package ro.upt;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class ModelTest {

    private WebDriver webDriver;
    private Actions actions;

    private static WebDriverManager webDriverManager;

    @BeforeAll
    static void beforeAll() {
        if (WebDriverManager.chromedriver().getBrowserPath().isPresent()) {
            webDriverManager = WebDriverManager.chromedriver();
        }

//        if (WebDriverManager.firefoxdriver().getBrowserPath().isPresent()) {
//            webDriverManager = WebDriverManager.firefoxdriver();
//        }
    }

    @BeforeEach
    void setup() {
        Assertions.assertNotNull(webDriverManager);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-features=PasswordManagerEnabled");
        options.addArguments("--disable-features=PasswordLeakDetection");

        webDriver = new ChromeDriver(options); //or instantiate the Firefox
        webDriver.manage().timeouts().implicitlyWait(Duration.of(5, ChronoUnit.SECONDS));
        webDriver.manage().window().maximize();
        actions = new Actions(webDriver);

        webDriver.get("https://www.saucedemo.com/");
    }

    @AfterEach
    void tearDown() {
        webDriver.quit();
    }

    private void login(String username, String password) {
        WebElement usernameTextBox = webDriver.findElement(By.id("user-name"));
        actions.sendKeys(usernameTextBox, username);

        WebElement passwordBox = webDriver.findElement(By.id("password"));
        actions.sendKeys(passwordBox, password);

        actions.perform(); //very important to use the PERFORM just for ACTIONS class

        WebElement loginButton = webDriver.findElement(By.xpath("//*[@id=\"login-button\"]"));
        loginButton.click();
    }

    @Test
    void checkAuthenticationWorked() {
        login("standard_user", "secret_sauce");

        WebElement title = webDriver.findElement(By.xpath("//*[@id=\"header_container\"]/div[2]/span"));

        Assertions.assertEquals("Products", title.getText());
    }

    @Test
    void testAuthenticationFailed() {
        login("standard_user", "secret_sauce1");

        WebElement errorText = webDriver.findElement(By.tagName("h3"));

        Assertions.assertNotNull(errorText);
        Assertions.assertTrue(errorText.getText().contains("sadface"));
    }

    @Test
    void testOrderProductsByPrice() {
        login("standard_user", "secret_sauce");

        WebElement firstElementTitle = webDriver.findElement(By.className("inventory_item_name"));
        Assertions.assertNotNull(firstElementTitle);
        Assertions.assertTrue(firstElementTitle.getText().contains("Backpack"));

        WebElement productSortButton = webDriver.findElement(By.className("product_sort_container"));
        Assertions.assertNotNull(productSortButton);
        productSortButton.click();

        WebElement productOption = webDriver.findElement(By.xpath("//*[@id=\"header_container\"]/div[2]/div/span/select/option[3]"));
        Assertions.assertNotNull(productOption);
        productOption.click();

        WebElement firstElementAfterSorting = webDriver.findElement(By.className("inventory_item_name"));
        Assertions.assertNotNull(firstElementAfterSorting);
        Assertions.assertTrue(firstElementAfterSorting.getText().contains("Onesie"));
        Assertions.assertEquals("Sauce Labs Onesie", firstElementAfterSorting.getText());

    }

    @Test
    void testEntireCheckoutProcess() {
        login("standard_user", "secret_sauce");

        WebElement buyFirstItem = webDriver.findElement(By.xpath("//*[@id=\"add-to-cart-sauce-labs-onesie\"]"));
        Assertions.assertNotNull(buyFirstItem);
        buyFirstItem.click();

        WebElement secondBuyedElement = webDriver.findElement(By.xpath("//*[@id=\"add-to-cart-sauce-labs-bike-light\"]"));
        Assertions.assertNotNull(secondBuyedElement);
        secondBuyedElement.click();

        WebElement cartButton = webDriver.findElement(By.className("shopping_cart_link"));
        Assertions.assertNotNull(cartButton);
        cartButton.click();

        WebElement checkoutButton = webDriver.findElement(By.xpath("//*[@id=\"shopping_cart_container\"]/a"));
        Assertions.assertNotNull(checkoutButton);
        checkoutButton.click();

        WebElement firsNameBox = webDriver.findElement(By.id("first-name"));
        Assertions.assertNotNull(firsNameBox);
        actions.sendKeys(firsNameBox, "mammamam");

        WebElement secondNameBox = webDriver.findElement(By.xpath("//*[@id=\"last-name\"]"));
        Assertions.assertNotNull(secondNameBox);
        actions.sendKeys(secondNameBox, "mammamam");

        WebElement zipBox = webDriver.findElement(By.xpath("//*[@id=\"postal-code\"]"));
        Assertions.assertNotNull(zipBox);
        actions.sendKeys(zipBox, "mammamam");

        actions.perform(); //IMPORTANT

        WebElement continueButton = webDriver.findElement(By.id("continue"));
        Assertions.assertNotNull(continueButton);
        continueButton.click();

        WebElement finishButton = webDriver.findElement(By.id("finish"));
        Assertions.assertNotNull(finishButton);
        finishButton.click();

        WebElement finalMessage = webDriver.findElement(By.xpath("//*[@id=\"checkout_complete_container\"]/h2"));
        Assertions.assertNotNull(finalMessage);
        Assertions.assertTrue(finalMessage.getText().contains("Thank you"));
        Assertions.assertEquals("Thank you for your order!", finalMessage.getText());

    }
}
