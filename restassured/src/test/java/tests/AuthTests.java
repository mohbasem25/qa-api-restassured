package tests;

import io.restassured.response.Response;
import models.LoginRequest;
import models.LoginResponse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static config.ApiConfig.responseTimeSlaMs;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Authentication endpoint coverage: successful and failed login,
 * exercising both the happy path and dummyjson.com's documented negative
 * cases where a valid username AND password are both required.
 * <p>
 * {@code emilys} / {@code emilyspass} is one of dummyjson's real seeded
 * test users, so the "success" scenarios below hit an actual account rather
 * than a mocked one.
 */
public class AuthTests extends BaseTest {

    @DataProvider(name = "invalidAuthPayloads")
    public Object[][] invalidAuthPayloads() {
        return new Object[][]{
                {new LoginRequest("emilys", null), "Missing password"},
                {new LoginRequest(null, "emilyspass"), "Missing username"},
                {new LoginRequest("", ""), "Missing username and password"},
        };
    }

    @Test(description = "POST /auth/login with valid credentials returns 200 and a non-empty access token")
    public void successfulLoginReturnsToken() {
        LoginRequest payload = new LoginRequest("emilys", "emilyspass");

        Response response = given()
                .body(payload)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(200)
                .time(lessThan(responseTimeSlaMs()))
                .body("accessToken", notNullValue())
                .body("username", equalTo("emilys"))
                .extract().response();

        LoginResponse loginResponse = response.as(LoginResponse.class);
        assertNotNull(loginResponse.getAccessToken(), "Access token should be present on successful login");
        assertTrue(loginResponse.getAccessToken().length() > 0, "Access token should not be empty");
    }

    @Test(description = "POST /auth/login with a wrong password returns 400 with an 'Invalid credentials' message")
    public void loginWithWrongPasswordReturns400() {
        LoginRequest payload = new LoginRequest("emilys", "wrongpassword");

        given()
                .body(payload)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(400)
                .time(lessThan(responseTimeSlaMs()))
                .body("message", equalTo("Invalid credentials"));
    }

    @Test(description = "POST /auth/login without a password returns 400 requiring both username and password")
    public void loginWithoutPasswordReturns400() {
        given()
                .body("{ \"username\": \"emilys\" }")
        .when()
                .post("/auth/login")
        .then()
                .statusCode(400)
                .time(lessThan(responseTimeSlaMs()))
                .body("message", equalTo("Username and password required"));
    }

    @Test(description = "POST /auth/login and negative payload variants reject invalid/incomplete credentials with 400",
            dataProvider = "invalidAuthPayloads")
    public void authEndpointRejectsInvalidPayloads(LoginRequest payload, String scenario) {
        given()
                .body(payload)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(400)
                .body("message", notNullValue());
    }
}
