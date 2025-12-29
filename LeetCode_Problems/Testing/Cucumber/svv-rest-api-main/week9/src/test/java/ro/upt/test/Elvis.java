package ro.upt.test;

import io.cucumber.cienvironment.internal.com.eclipsesource.json.JsonObject;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;

public class Elvis {
    private static final String BASE_URL = "https://fakestoreapi.com";
    private static final RequestSpecification requestSpecification;
    private static Response response;

    static {
        RestAssured.baseURI = BASE_URL;
        RestAssured.proxy("proxy.intranet.cs.upt.ro", 3128);
        requestSpecification = RestAssured.given();
    }

    @Given("A list of products exists in the database")
    public void aListOfProductsExistsInTheDatabase() {
        response = requestSpecification.get("/products");
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @When("Retrieving a product by a specific id")
    public void retrievingAProductByASpecificId() {
        response = requestSpecification.get("/products/1");
    }

    @Then("The product's information should be returned")
    public void theProductSInformationShouldBeReturned() {
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.jsonPath().getInt("id"));
        Assertions.assertEquals("Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops", response.jsonPath().getString("title"));
    }

    @Given("A product with a certain id exists")
    public void aProductWithACertainIdExists() {
        response = requestSpecification.get("/products/1");
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.jsonPath().getInt("id"));
        Assertions.assertEquals("Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops", response.jsonPath().getString("title"));
    }

    @When("The title of the product is updated")
    public void theTitleOfTheProductIsUpdated() {
        JsonObject requestBody = new JsonObject();
        requestBody.add("id", 1);
        requestBody.add("title", "Title Updated");
        response = requestSpecification.contentType("application/json").body(requestBody.toString()).put("/products/1");
    }

    @Then("A successful response after the update should be returned")
    public void aSuccessfulResponseAfterTheUpdateShouldBeReturned() {
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.jsonPath().getInt("id"));
        Assertions.assertEquals("Title Updated", response.jsonPath().getString("title"));
    }

    @Given("A product with a certain id does not exist \\(get the entire list and assert that its size is less than your id)")
    public void aProductWithACertainIdDoesNotExistGetTheEntireListAndAssertThatItsSizeIsLessThanYourId() {
        int certainId = 200;
        response = requestSpecification.get("/products");
        int listSize = response.jsonPath().getList("").size();
        Assertions.assertTrue(listSize < certainId);
    }

    @When("The product is created")
    public void theProductIsCreated() {
        JsonObject requestBody = new JsonObject();
        requestBody.add("id", 200);
        requestBody.add("title", "Good title");
        requestBody.add("price", 200.200);
        requestBody.add("description", "Good product");
        requestBody.add("category", "Milk");
        requestBody.add("image", "http://example.com");
        response = requestSpecification.contentType("application/json")
                .body(requestBody.toString())
                .post("/products");
    }

    @Then("A successful response after the creation should be returned")
    public void aSuccessfulResponseAfterTheCreationShouldBeReturned() {
        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(1, response.jsonPath().getInt("id"));
    }
}

