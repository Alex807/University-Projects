package ro.upt.test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class StepDefinitions {
    private static final String BASE_URL = "https://fakestoreapi.com";
    private static final RequestSpecification requestSpecification;
    private static Response response;

    static {
        RestAssured.baseURI = BASE_URL;
        // ToDo: Uncomment if you run the tests from the lab environment
//        RestAssured.proxy("proxy.intranet.cs.upt.ro", 3128);
        requestSpecification = RestAssured.given();
    }
}
