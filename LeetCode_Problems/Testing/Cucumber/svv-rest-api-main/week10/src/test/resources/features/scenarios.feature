Feature: API testing of a Fake API
  Documentation URL: https://fakestoreapi.com/products

  Scenario: A product with a specific id can be retrieved
    Given A list of products exists in the database
    When Retrieving a product by a specific id
    Then The product's information should be returned

  Scenario: Changing the title of a product works
    Given A product with a certain id exists
    When The title of the product is updated
    Then A successful response after the update should be returned

  Scenario: A product can be created
    Given A product with a certain id does not exist (get the entire list and assert that its size is less than your id)
    When The product is created
    Then A successful response after the creation should be returned

  Scenario: A product can be deleted
    Given A product with a certain id already exists
    When The product is deleted
    Then A successful response after the deletion should be returned
