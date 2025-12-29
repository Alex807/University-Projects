# API Testing with RestAssured and Cucumber

### Installing required dependencies

In order to test API endpoints in the BDD style, we'll first need to add the required dependencies for the [RestAssured](https://rest-assured.io/) library, including [jackson-databind](https://github.com/FasterXML/jackson-databind) for proper JSON object mapping.

````xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.5.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.18.1</version>
</dependency>
````

[Cucumber](https://cucumber.io/docs/cucumber/) requires the following dependencies:

````xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>7.20.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit</artifactId>
    <version>7.20.1</version>
</dependency>
````
We also need to add a dependency for the JUnit API, which provides the `Assertions` class:
````xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.11.3</version>
    <scope>test</scope>
</dependency>
````


### Behavior-driven development with Cucumber

Behavior-driven development implies writing tests based on requirements written in english (or english-like) language. Usually, the tests are written in three steps: `Given`, `When` and `Then`.

In the examples below, we will test a public API, available at https://fakerestapi.azurewebsites.net/index.html, and will interact with the **Activity** endpoint.

#### The feature file

A `*.feature` file needs to be created in the `src/test/resources` directory. The file will contain the customer requirements using [Gherkin](https://cucumber.io/docs/gherkin/) syntax.

````gherkin
Feature: API testing of a Fake API
  Swagger URL: https://fakerestapi.azurewebsites.net/index.html

  Scenario: When user requests for an existing activity id, the activity should be returned
    Given A list of activities is available
    When The user requests for an activity with a specific id
    Then The requested activity should be returned
````

The `Given`, `When` and `Then` steps will be implemented in the step definition class. `Feature` and `Scenario` are simply used to provide additional information to the user, and are completely ignored by Cucumber. In the above example, `Feature` is used to describe the API that will be tested, while `Scenario` describes a single test case. Multiple scenarios (with groups of `Given`, `When`, `Then`) describe multiple test cases.

#### The step definition class

The scenarios defined in the previous file need to be specified in the corresponding annotations, inside the step definition class. This class will also contain the test logic:

````java
public class StepDefinitions {
    @Given("A list of activities is available")
    public void aListOfActivitiesIsAvailabie() {
      // ToDo: Implement logic
    }

    @When("The user requests for an activity with a specific id")
    public void theUserRequestsForAnActivityWithASpecificId() {
      // ToDo: Implement logic
    }

    @Then("The requested activity should be returned")
    public void theRequestedActivityShouldBeReturned() {
      // ToDo: Implement logic
    }
}
````

The step definition will contain all of the API requests, together with required assertions.

#### The runner class

The step definition class is **not** runnable. Cucumber requires a special annotated class to run the step definitions. 

> [!NOTE]
The class will remain empty and will only be used for running the tests.

````java
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue="ro.upt.test"
)
public class TestRunner {
}
````
`features` specifies the directory where the feature files reside, while `glue` specifies the package of the step definition file.

### Sending API requests with RestAssured

We will make use of the Swagger documentation of the provided API, in order to get a grasp of the available endpoints and how we can access them.

#### 1. Sending simple GET requests for one item

![image](https://github.com/user-attachments/assets/f851ecf6-9150-4d8b-95f6-4624fe2fe08a)

````java
String BASE_URL = "https://fakerestapi.azurewebsites.net";

RestAssured.baseURI = BASE_URL;
RequestSpecification requestSpecification = RestAssured.given();
Response response = requestSpecification.get("/api/v1/Activities/1");

Assertions.assertEquals(200, response.getStatusCode());
Assertions.assertEquals(1, response.jsonPath().getInt("id"));
Assertions.assertEquals("Activity 1", response.jsonPath().getString("title"));
Assertions.assertEquals(false, response.jsonPath().getBoolean("completed"));
````

#### 2. Sending GET requests for multiple items (checking the size of the list)

![image](https://github.com/user-attachments/assets/55d18db9-3b3c-4705-b1f0-0a638ba3c7d1)

````java
String BASE_URL = "https://fakerestapi.azurewebsites.net";

RestAssured.baseURI = BASE_URL;
RequestSpecification requestSpecification = RestAssured.given();
Response response = requestSpecification.get("/api/v1/Activities");

Assertions.assertEquals(200, response.getStatusCode());
// Take the list returned by the GET request, and check its size (in our case, the API returns a list with 200 elements)
Assertions.assertEquals(200, response.jsonPath().getList("").size());
````


#### 3. Sending POST requests (with body)

![image](https://github.com/user-attachments/assets/0979f177-dcd4-43e4-b0e3-9c580318efd5)

````java
String BASE_URL = "https://fakerestapi.azurewebsites.net";

RestAssured.baseURI = BASE_URL;
RequestSpecification requestSpecification = RestAssured.given();

JsonObject requestBody = new JsonObject();
requestBody.add("id", 16);
requestBody.add("idBook", 32);
requestBody.add("firstName", "John");
requestBody.add("lastName", "Doe");

Response response = requestSpecification
        .contentType("application/json")
        .body(requestBody.toString())
        .post("/api/v1/Authors");

// Check the status code
Assertions.assertEquals(200, response.getStatusCode());

// POST also returns the created object, so check for the returned fields to match your object
Assertions.assertEquals(16, response.jsonPath().getInt("id"));
Assertions.assertEquals(32, response.jsonPath().getInt("idBook"));
Assertions.assertEquals("John", response.jsonPath().getString("firstName"));
Assertions.assertEquals("Doe", response.jsonPath().getString("lastName"));
````

> [!NOTE]
`PUT` requests can be sent similarly by just replacing the final method call. 

#### 4. Sending POST requests (with Authorization header and JWT token)

````java
String BASE_URL = "BASE URL GOES HERE";

RestAssured.baseURI = BASE_URL;
RequestSpecification requestSpecification = RestAssured.given();

Response response = requestSpecification
        .contentType("application/json")
        .header(new Header("Authorization", "Bearer TOKEN")) // requires a JWT token
        .body("BODY GOES HERE")
        .post("URL GOES HERE");

// Check for the status code
Assertions.assertEquals(200, response.getStatusCode());

// Additionally check for response body fields
````

### Accessing complex data using JSONPath ###

#### 1. Getting values from nested data ####

For data with a similar format:
````json
{
    "data": {
        "user1": {
            "username": "john",
            "email": "johndoe53@gmail.com"
        }
    }
}
````

Nested data can be accessed like this:

````java
Assertions.assertEquals("john", response.jsonPath().getString("data.user1.username");
Assertions.assertEquals("johndoe53@gmail.com", response.jsonPath().getString("data.user1.email");
````

#### 2. Retrieving properties from array elements ####

For data containing arrays:

````json
{
    "books": [
        {
            "title": "The Jungle Book",
            "author": "Rudyard Kipling"
        },
        {
            "title": "Harry Potter and the Philosopher's Stone",
            "author": "J.K. Rowling"
        }
    ]
}
````

The properties of the first book can be accessed like this:

````java
Assertions.assertEquals("The Jungle Book", response.jsonPath().getString("books[0].title");
Assertions.assertEquals("Rudyard Kipling", response.jsonPath().getString("books[0].author");
````
