package ro.upt;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * It may seem a bit confusing to why I've decided to implement several testing methods using xPath, or arrays, but
 * it is 100% your choice on how to implement it. Most of the modifications I did to the code were because I wanted
 * to clean it up a bit. I posted this code just to show you how to use Selenium's syntax, but this doesn't mean
 * that you should always use these approaches for similar test cases. Try playing with the Inspect Element tool and
 * implement the rest of the methods on your own. There are still some methods left :)
 */
public class SeleniumTest {
    private WebDriver webDriver;
    private Actions actions;

    private static WebDriverManager webDriverManager;

    @BeforeAll
    static void beforeAll() {
        if (WebDriverManager.chromedriver().getBrowserPath().isPresent()) {
            webDriverManager = WebDriverManager.chromedriver();
        }
    }

    @BeforeEach
    public void setup() {
        Assertions.assertNotNull(webDriverManager);
        webDriver = new ChromeDriver();
        webDriver.manage().timeouts().implicitlyWait(Duration.of(5, ChronoUnit.SECONDS));
        actions = new Actions(webDriver);
    }

    @AfterEach
    void tearDown() {
        webDriver.quit();
    }

    /*
        Not a test case, but the login functionality is used in every test case, so I decided to store it in a separate
        method.
     */
    private void login(String username, String password) {
        WebElement usernameTextBox = webDriver.findElement(By.name("user-name"));
        actions.sendKeys(usernameTextBox, username);

        WebElement passwordTextBox = webDriver.findElement(By.name("password"));
        actions.sendKeys(passwordTextBox, password).perform();

        WebElement loginButton = webDriver.findElement(By.name("login-button"));
        loginButton.click();
    }

    @Test
    public void checkTitleIsCorrect() {
        webDriver.get("https://www.saucedemo.com/");
        String title = webDriver.getTitle();
        Assertions.assertEquals("Swag Labs", title);
    }

    @Test
    public void checkAuthenticationWorked() {
        webDriver.get("https://www.saucedemo.com/");

        login("standard_user", "secret_sauce");

        WebElement headingText = webDriver.findElement(By.className("title"));
        Assertions.assertEquals("Products", headingText.getText());
    }

    /*
        Enter invalid login data and check if the website responded properly.
     */
    @Test
    public void testAuthenticationFailed() {
        webDriver.get("https://www.saucedemo.com/");
        login("invalid_user", "invalid_password");

        WebElement errorText = webDriver.findElement(By.tagName("h3"));
        Assertions.assertNotNull(errorText);
        Assertions.assertEquals("Epic sadface: Username and password do not match any user in this service", errorText.getText());
    }

    /*
        After logging in, click on the filter and select "Price (low to high)", then write your assertion based
        on the first element that is shown
     */
    @Test
    public void testOrderProductsByPrice() {
        webDriver.get("https://www.saucedemo.com/");
        login("standard_user", "secret_sauce");

        /*
            We first want to check that the initial first item is a backpack
         */
        WebElement firstElementTitle = webDriver.findElement(By.className("inventory_item_name"));
        Assertions.assertNotNull(firstElementTitle);
        Assertions.assertEquals("Sauce Labs Backpack", firstElementTitle.getText());

        WebElement productSortSelector = webDriver.findElement(By.className("product_sort_container"));
        productSortSelector.click();

        /*
            I used xpath here because the option elements do not have a specific identifier (id, name, class, etc.)
            But you could as well have used findElements(By.tagName("option")), which returns a List, and get the item
            at index 2. Don't forget, XPath's index starts from 1, and the List index starts from 0!
         */
        WebElement productOption = webDriver.findElement(By.xpath("/html/body/div/div/div/div[1]/div[2]/div/span/select/option[3]"));
        Assertions.assertNotNull(productOption);
        productOption.click();

        /*
            After sorting, we now want to check that the NEW first item is a onesie
         */
        firstElementTitle = webDriver.findElement(By.className("inventory_item_name"));
        Assertions.assertNotNull(firstElementTitle);
        Assertions.assertEquals("Sauce Labs Onesie", firstElementTitle.getText());
    }

    /*
        After logging in, add one item to the cart and make sure that the button got transformed into a "Remove" button
     */
    @Test
    public void testAddToCartDisplaysRemoveButton() throws InterruptedException {
        webDriver.get("https://www.saucedemo.com/");
        login("standard_user", "secret_sauce");

        /*
            I will get all "Add to cart" buttons and click some of them. It would waste less code rows if I take them
            as an array and not using their id. But you could as well get the id of each of the buttons you want to
            press and separately call click() on them. It's your choice how you want to implement it.
         */
        List<WebElement> addToCartButtons = webDriver.findElements(By.className("btn_inventory"));
        Assertions.assertTrue(addToCartButtons.size() >= 6);

        addToCartButtons.get(0).click();
        addToCartButtons.get(2).click();
        addToCartButtons.get(5).click();

        /*
            I am making sure here that the circle indicating the number of products in the cart is equal to the actual
            number of selected products.
         */
        WebElement shoppingCartCounterCircle = webDriver.findElement(By.className("shopping_cart_badge"));
        Thread.sleep(2000);
        Assertions.assertNotNull(shoppingCartCounterCircle);
        Assertions.assertEquals("3", shoppingCartCounterCircle.getText());

        /*
            This is the test case where I check if I have three buttons that have changed (when an "Add to cart" button
            is clicked, it switches to a "Remove" button). You could as well only test one of the two things (counter
            or buttons changed). It's your choice.
         */
        List<WebElement> removeButtons = webDriver.findElements(By.className("btn_secondary")); // The red button has the class btn_secondary, and the normal button is btn_primary (this can be easily observed)
        Assertions.assertEquals(3, removeButtons.size()); // 3 available remove buttons
    }

    /*
        After logging in, add a product (or more) to the cart and go further with the checkout process by filling in
        all the required inputs. Then, make sure that the "Thank you for your order!" page is displayed.
     */
    @Test
    public void testEntireCheckoutProcess() {
        // ToDo: Implement
    }

    /*
        After logging in and going further with the checkout process (choose some items and click on the cart),
        make sure that when you're prompted to type your checkout data, when leaving the "First Name" textbox
        empty, you'll get an error message.
     */
    @Test
    public void testMissingFirstNameCheckout() {
        // ToDo: Implement
    }

    /*
        After logging in, log out and check if the user is properly redirected to the login page
     */
    @Test
    public void testLogoutWorks() {
        // ToDo: Implement
    }

    /*
        Try logging in with a locked out user, and make sure that it doesn't work, and that the proper message is
        displayed.
     */
    @Test
    public void testLockedOutUser() {
        // ToDo: Implement
    }

}
