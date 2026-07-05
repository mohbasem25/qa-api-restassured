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

    @DataProvider(name = "validPageNumbers")
    public Object[][] validPageNumbers() {
        return new Object[][]{{1}, {2}};
    }

    @DataProvider(name = "existingUserIds")
    public Object[][] existingUserIds() {
        return new Object[][]{{1}, {2}, {3}, {7}, {12}};
    }

    @Test(description = "GET /users?page= returns 200 with the requested page and correct pagination metadata",
            dataProvider = "validPageNumbers")
    public void listUsersReturnsRequestedPage(int page) {
        given()
                .queryParam("page", page)
        .when()
                .get("/users")
        .then()
                .statusCode(200)
                .contentType("application/json")
                .time(lessThan(responseTimeSlaMs()))
                .body("page", equalTo(page))
                .body("data", Matchers.not(Matchers.empty()))
                .body("data.size()", greaterThan(0))
                .body("total_pages", greaterThan(0))
                .body("per_page", greaterThan(0));
    }

    @Test(description = "GET /users?page= default per_page is honoured and every user has the expected fields")
    public void listUsersEachRecordHasExpectedFields() {
        given()
                .queryParam("page", 1)
        .when()
                .get("/users")
        .then()
                .statusCode(200)
                .body("data.id", Matchers.everyItem(Matchers.notNullValue()))
                .body("data.email", Matchers.everyItem(Matchers.containsString("@")))
                .body("data.first_name", Matchers.everyItem(Matchers.notNullValue()))
                .body("data.last_name", Matchers.everyItem(Matchers.notNullValue()))
                .body("data.avatar", Matchers.everyItem(Matchers.containsString("https://")));
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
                .body("data.id", equalTo(id))
                .extract().response();

        User user = response.jsonPath().getObject("data", User.class);
        assertNotNull(user, "Deserialized user should not be null");
        assertEquals(user.getId(), id);
        assertTrue(user.getEmail().contains("@"), "Email should contain '@'");
        assertNotNull(user.getFirst_name(), "first_name should be present");
        assertNotNull(user.getLast_name(), "last_name should be present");
    }

    @Test(description = "GET /users/{id} for a non-existent user returns 404 with an empty body")
    public void getNonExistentUserReturns404() {
        given()
                .pathParam("id", 23)
        .when()
                .get("/users/{id}")
        .then()
                .statusCode(404)
                .time(lessThan(responseTimeSlaMs()))
                .body(equalTo("{}"));
    }

    @Test(description = "GET /unknown (colors resource) returns 200 with a non-empty data list")
    public void listColorsReturns200() {
        given()
        .when()
                .get("/unknown")
        .then()
                .statusCode(200)
                .body("data", Matchers.not(Matchers.empty()))
                .body("data[0].id", Matchers.notNullValue())
                .body("data[0].name", Matchers.notNullValue());
    }

    @Test(description = "GET /unknown/{id} for a non-existent color returns 404")
    public void getNonExistentColorReturns404() {
        given()
                .pathParam("id", 99)
        .when()
                .get("/unknown/{id}")
        .then()
                .statusCode(404);
    }

    @Test(description = "Response headers include a JSON content type on successful GET requests")
    public void responseHasExpectedContentTypeHeader() {
        given()
                .queryParam("page", 1)
        .when()
                .get("/users")
        .then()
                .statusCode(200)
                .header("Content-Type", Matchers.containsString("application/json"));
    }
}
