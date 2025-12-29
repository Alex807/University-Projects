package ro.upt.test;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepDefinitions {
    private static final String BASE_URL = "https://fakerestapi.azurewebsites.net";
    private static final RequestSpecification requestSpecification;

    private static Response response;
    private static int createdActivityId;
    private static int createdAuthorId;
    private static int createdBookId;

    static {
        RestAssured.baseURI = BASE_URL;
        // ToDo: Uncomment if you run the tests from the lab environment
//        RestAssured.proxy("proxy.intranet.cs.upt.ro", 3128);
        requestSpecification = RestAssured.given();
    }

    @Given("A list of activities is available")
    public void aListOfActivitiesIsAvailabie() {
        response = requestSpecification.get("/api/v1/Activities");
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @When("The user requests for an activity with a specific id")
    public void theUserRequestsForAnActivityWithASpecificId() {
        response = requestSpecification
                .get("/api/v1/Activities/1");
    }

    @Then("The requested activity should be returned")
    public void theRequestedActivityShouldBeReturned() {
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.jsonPath().getInt("id"));
        Assertions.assertEquals("Activity 1", response.jsonPath().getString("title"));
        Assertions.assertFalse(response.jsonPath().getBoolean("completed"));
    }

    // =======================
    // SCENARIO 2 - Add Activity
    // =======================
    @Given("An activity id that does not exist")
    public void givenId() {
        createdActivityId = 777;
    }

    @When("The user creates a new activity with that id")
    public void theUserAddsANewActivity() {
        Map<String, Object> newActivity = new HashMap<>();
        newActivity.put("id", createdActivityId);
        newActivity.put("title", "Test Activity");
        newActivity.put("completed", false);

        response = requestSpecification
                .contentType(ContentType.JSON)
                .body(newActivity)
                .post("/api/v1/Activities");

    }

    @Then("The activity should be created successfully")
    public void theNewActivityShouldBeSaved() {
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("Test Activity", response.jsonPath().getString("title"));
        Assertions.assertFalse(response.jsonPath().getBoolean("completed"));

        Assertions.assertEquals(777, response.jsonPath().getInt("id"));
    }


    //=========     SCENARIO 3 =======

    @Given("An author id that already exists")
    public void anAuthorIdThatAlreadyExists() {
        Map<String, Object> newAuthor = new HashMap<>();
        newAuthor.put("id", 123);
        newAuthor.put("idBook", 90);

        response = requestSpecification
                    .contentType(ContentType.JSON)
                    .body(newAuthor)
                    .post("/api/v1/Authors");

        createdAuthorId = response.jsonPath().getInt("id");
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @When("The user deletes the author with the specific id")
    public void theUserDeletesTheAuthorWithTheSpecificId() {
        response = requestSpecification.delete("/api/v1/Authors/" + createdAuthorId);
    }

    @Then("The author should be successfully deleted")
    public void theAuthorShouldBeSuccessfullyDeleted() {
        Assertions.assertEquals(200, response.getStatusCode());

        Response verify = requestSpecification.get("/api/v1/Authors/" + createdAuthorId);
        Assertions.assertEquals(404, verify.getStatusCode());
    }

    @Given("A book with a certain id exists")
    public void aBookIsCreated() {
        Map<String, Object> newBook = new HashMap<>();
        newBook.put("id", 0);
        newBook.put("title", "Original Title");
        newBook.put("description", "Test book");
        newBook.put("pageCount", 150);
        newBook.put("excerpt", "excerpt");
        newBook.put("publishDate", "2025-01-01T00:00:00.000Z");

        response = requestSpecification
                .contentType(ContentType.JSON)
                .body(newBook)
                .post("/api/v1/Books");

        createdBookId = response.jsonPath().getInt("id");
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @When("The user updates the book")
    public void theUserUpdatesThatBook() {
        Map<String, Object> updatedBook = new HashMap<>();
        updatedBook.put("id", createdBookId);
        updatedBook.put("title", "Updated Title");
        updatedBook.put("description", "Updated description");
        updatedBook.put("pageCount", 200);
        updatedBook.put("excerpt", "Updated excerpt");
        updatedBook.put("publishDate", "2025-01-01T00:00:00.000Z");

        response = requestSpecification
                .contentType(ContentType.JSON)
                .body(updatedBook)
                .put("/api/v1/Books/" + createdBookId);
    }

    @Then("The book should be successfully updated")
    public void theBookShouldContainTheNewValues() {
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("Updated Title", response.jsonPath().getString("title"));
        Assertions.assertEquals(200, response.jsonPath().getInt("pageCount"));
    }
}
