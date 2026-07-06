package tests;

import io.restassured.response.Response;
import models.User;
import org.hamcrest.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static config.ApiConfig.responseTimeSlaMs;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Read-only ({@code GET}) coverage for the /users resource:
 * paginated listing, single user lookup, and the not-found edge case.
 */
public class UsersGetTests extends BaseTest {

    @DataProvider(name = "validSkipValues")
    public Object[][] validSkipValues() {
        return new Object[][]{{0}, {10}};
    }

    @DataProvider(name = "existingUserIds")
    public Object[][] existingUserIds() {
        return new Object[][]{{1}, {2}, {3}, {7}, {12}};
    }

    @Test(description = "GET /users?limit=&skip= returns 200 with the requested page and correct pagination metadata",
            dataProvider = "validSkipValues")
    public void listUsersReturnsRequestedPage(int skip) {
        given()
                .queryParam("limit", 10)
                .queryParam("skip", skip)
        .when()
                .get("/users")
        .then()
                .statusCode(200)
                .contentType("application/json")
                .time(lessThan(responseTimeSlaMs()))
                .body("skip", equalTo(skip))
                .body("limit", equalTo(10))
                .body("users", Matchers.not(Matchers.empty()))
                .body("users.size()", greaterThan(0))
                .body("total", greaterThan(0));
    }

    @Test(description = "GET /users?limit=&skip= every returned record has the expected fields")
    public void listUsersEachRecordHasExpectedFields() {
        given()
                .queryParam("limit", 10)
                .queryParam("skip", 0)
        .when()
                .get("/users")
        .then()
                .statusCode(200)
                .body("users.id", Matchers.everyItem(Matchers.notNullValue()))
                .body("users.email", Matchers.everyItem(Matchers.containsString("@")))
                .body("users.firstName", Matchers.everyItem(Matchers.notNullValue()))
                .body("users.lastName", Matchers.everyItem(Matchers.notNullValue()))
                .body("users.image", Matchers.everyItem(Matchers.containsString("https://")));
    }

    @Test(description = "GET /users/{id} returns 200 and deserializes into the User model correctly",
            dataProvider = "existingUserIds")
    public void getSingleUserReturns200AndValidBody(int id) {
        Response response = given()
                .pathParam("id", id)
        .when()
                .get("/users/{id}")
        .then()
                .statusCode(200)
                .time(lessThan(responseTimeSlaMs()))
                .body("id", equalTo(id))
                .extract().response();

        User user = response.as(User.class);
        assertNotNull(user, "Deserialized user should not be null");
        assertEquals(user.getId(), id);
        assertTrue(user.getEmail().contains("@"), "Email should contain '@'");
        assertNotNull(user.getFirstName(), "firstName should be present");
        assertNotNull(user.getLastName(), "lastName should be present");
    }

    @Test(description = "GET /users/{id} for a non-existent user returns 404 with a descriptive message")
    public void getNonExistentUserReturns404() {
        given()
                .pathParam("id", 999)
        .when()
                .get("/users/{id}")
        .then()
                .statusCode(404)
                .time(lessThan(responseTimeSlaMs()))
                .body("message", equalTo("User with id '999' not found"));
    }

    @Test(description = "GET /products (used in place of reqres.in's 'colors' resource) returns 200 with a non-empty list")
    public void listProductsReturns200() {
        given()
                .queryParam("limit", 10)
        .when()
                .get("/products")
        .then()
                .statusCode(200)
                .body("products", Matchers.not(Matchers.empty()))
                .body("products[0].id", Matchers.notNullValue())
                .body("products[0].title", Matchers.notNullValue());
    }

    @Test(description = "GET /products/{id} for a non-existent product returns 404")
    public void getNonExistentProductReturns404() {
        given()
                .pathParam("id", 9999)
        .when()
                .get("/products/{id}")
        .then()
                .statusCode(404)
                .body("message", equalTo("Product with id '9999' not found"));
    }

    @Test(description = "Response headers include a JSON content type on successful GET requests")
    public void responseHasExpectedContentTypeHeader() {
        given()
                .queryParam("limit", 10)
        .when()
                .get("/users")
        .then()
                .statusCode(200)
                .header("Content-Type", Matchers.containsString("application/json"));
    }
}
