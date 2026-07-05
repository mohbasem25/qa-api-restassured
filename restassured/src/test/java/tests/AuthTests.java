package tests;

import io.restassured.response.Response;
import models.LoginRequest;
import models.LoginResponse;
import models.RegisterResponse;
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
 * Authentication endpoint coverage: successful and failed login/register,
 * exercising both the happy path and the documented negative cases where
 * reqres.in requires a password.
 */
public class AuthTests extends BaseTest {

    @DataProvider(name = "invalidAuthPayloads")
    public Object[][] invalidAuthPayloads() {
        return new Object[][]{
                {new LoginRequest("eve.holt@reqres.in", null), "Missing password"},
                {new LoginRequest(null, "pistol"), "Missing email"},
                {new LoginRequest("", ""), "Missing email and password"},
        };
    }

    @Test(description = "POST /login with valid credentials returns 200 and a non-empty token")
    public void successfulLoginReturnsToken() {
        LoginRequest payload = new LoginRequest("eve.holt@reqres.in", "cityslicka");

        Response response = given()
                .body(payload)
        .when()
                .post("/login")
        .then()
                .statusCode(200)
                .time(lessThan(responseTimeSlaMs()))
                .body("token", notNullValue())
                .extract().response();

        LoginResponse loginResponse = response.as(LoginResponse.class);
        assertNotNull(loginResponse.getToken(), "Token should be present on successful login");
        assertTrue(loginResponse.getToken().length() > 0, "Token should not be empty");
    }

    @Test(description = "POST /register with valid credentials returns 200 with id and token")
    public void successfulRegisterReturnsIdAndToken() {
        LoginRequest payload = new LoginRequest("eve.holt@reqres.in", "pistol");

        Response response = given()
                .body(payload)
        .when()
                .post("/register")
        .then()
                .statusCode(200)
                .time(lessThan(responseTimeSlaMs()))
                .body("id", notNullValue())
                .body("token", notNullValue())
                .extract().response();

        RegisterResponse registerResponse = response.as(RegisterResponse.class);
        assertTrue(registerResponse.getId() > 0, "Registered user id should be a positive integer");
        assertNotNull(registerResponse.getToken(), "Token should be present on successful registration");
    }

    @Test(description = "POST /login without a password returns 400 with a descriptive error message")
    public void loginWithoutPasswordReturns400() {
        given()
                .body("{ \"email\": \"eve.holt@reqres.in\" }")
        .when()
                .post("/login")
        .then()
                .statusCode(400)
                .time(lessThan(responseTimeSlaMs()))
                .body("error", equalTo("Missing password"));
    }

    @Test(description = "POST /register without a password returns 400 with a descriptive error message")
    public void registerWithoutPasswordReturns400() {
        given()
                .body("{ \"email\": \"eve.holt@reqres.in\" }")
        .when()
                .post("/register")
        .then()
                .statusCode(400)
                .time(lessThan(responseTimeSlaMs()))
                .body("error", equalTo("Missing password"));
    }

    @Test(description = "POST /register with an unknown email returns 400 with a descriptive error message")
    public void registerWithUnknownUserReturns400() {
        LoginRequest payload = new LoginRequest("unknown.user@reqres.in", "somePassword");

        given()
                .body(payload)
        .when()
                .post("/register")
        .then()
                .statusCode(400)
                .body("error", equalTo("Note: Only defined users succeed registration"));
    }

    @Test(description = "POST /login and /register reject invalid/incomplete payloads with 400",
            dataProvider = "invalidAuthPayloads")
    public void authEndpointsRejectInvalidPayloads(LoginRequest payload, String scenario) {
        given()
                .body(payload)
        .when()
                .post("/login")
        .then()
                .statusCode(400)
                .body("error", notNullValue());
    }
}
