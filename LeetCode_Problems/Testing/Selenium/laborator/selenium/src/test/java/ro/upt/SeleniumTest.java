package ro.upt;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;

/**
 * The platform under test can be found at https://magento.softwaretestingboard.com/.
 */
public class SeleniumTest {
    private WebDriver webDriver;

    @BeforeEach
    public void setup() {
        FirefoxOptions options = new FirefoxOptions();

        // Set Firefox preferences to block popups and ads
        options.addPreference("dom.popup_maximum", 0); // Disable popups
        options.addPreference("privacy.trackingprotection.enabled", true); // Enable Tracking Protection
        options.addPreference("privacy.trackingprotection.pbmode.enabled", true); // Enable Tracking Protection in Private Mode
        options.addPreference("browser.contentblocking.category", "strict"); // Enable strict content blocking
        options.addPreference("browser.safebrowsing.phishing.enabled", true); // Enable phishing protection
        options.addPreference("browser.safebrowsing.malware.enabled", true); // Enable malware protection

        webDriver = new FirefoxDriver(options);
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {
        webDriver.quit();
    }

    /*
        VERY IMPORTANT: While implementing the tests, you may notice that the web page is slower than the one tested
        in the previous week. After interacting with elements that redirect to another page (such as buttons), please
        make sure you add Thread.sleep() calls, depending on how slow the page loads.

        e.g. Thread.sleep(1000) will make the test Thread wait for 1 second (1000 milliseconds), meaning that before
        you do an Assertion, you should wait until the new page fully loads all of the components.

        NOTICE: Selenium may still find elements even without Thread.sleep(), but it may find the elements from the old
        page, and show differences in your assertions. Make sure you properly add the statements if required.
     */

    /*
        Test the website's title
     */
    // ToDo: Implement testTitle()

    /*
        Test the search functionality (search box on the top-right corner)

        Hint: Search for a product that exists (navigate the website a little bit before) and make sure that it is
        displayed in the search results.
     */
    // ToDo: Implement testSearchBox()

    /*
        MANUALLY register on the website using fake data (create a test account) and test the login functionality
        with those credentials
     */
    // ToDo: Implement testLoginCorrect()

    /*
        Now test the login process with invalid data and make sure that a proper error is displayed
     */
    // ToDo: Implement testLoginInvalidData()

    /*
        After logging in on the website (with the credentials created manually), add to cart 3 (or more) items
        and make sure that the items in cart counter (top-right button) is updated correctly.

        IMPORTANT: If you cannot click the addToCart button, due to the "element could not be scrolled into view' error,
        try hovering over the box containing the button first (or even the image from the desired product, and then
        click on the button. The hover operation is done via actions.moveToElement().
     */
    // ToDo: Implement testAddItemsToCart()

    /*
        At the register process, try inserting different passwords inside the field and check if the password
        strength checker works accordingly. Assert first that "No password" is shown, then test for "Weak",
        "Medium", "Strong" and "Very Strong". Add all of the assertions in a single test method, so load the
        page only once, and use sendKeys() multiple times, starting from the weakest combination of characters
        and appending other characters to it. You'll need to test this manually first.

        You do not need to fill other registration fields, only the password field. You also do not need to go further
        with the registration process.
     */
    // ToDo: Implement testRegisterPasswordChecker()

    /*
        View all the products in a certain category and order them by their product name (not by position). Check
        if certain items have changed, compared to the initial order.
     */
    // ToDo: Implement testSortByProductName()

    /*
        Add two or more items to the comparison list (in the list of products, hover over each item and click the
        'chart' icon next to the 'heart' icon at the bottom of the box. Check if the "Compare Products" text has
        been updated in the header of the website (top-right). You should also check that the two items are displayed
        on the comparison page.
     */
    // ToDo: Implement testWorkingComparisonList()

    /*
        Login and then logout from the website, and make sure that you do not see your account name in the header
        anymore.
     */
    // ToDo: Implement testLoginLogoutWorking()
}
