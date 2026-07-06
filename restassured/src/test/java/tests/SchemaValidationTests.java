package tests;

import models.CreateUserRequest;
import models.LoginRequest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Contract/schema-level tests: verify that response payloads conform to
 * their published JSON Schema, independent of the field-level assertions
 * exercised elsewhere. This protects consumers from breaking/undocumented
 * structural changes (missing fields, type changes, etc.).
 */
public class SchemaValidationTests extends BaseTest {

    @Test(description = "GET /users/{id} response matches the single-user JSON schema")
    public void singleUserResponseMatchesSchema() {
        given()
                .pathParam("id", 2)
        .when()
                .get("/users/{id}")
        .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"));
    }

    @Test(description = "GET /users?limit=&skip= response matches the list-users JSON schema")
    public void listUsersResponseMatchesSchema() {
        given()
                .queryParam("limit", 10)
                .queryParam("skip", 0)
        .when()
                .get("/users")
        .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/list-users-schema.json"));
    }

    @Test(description = "POST /users/add response matches the create-user JSON schema")
    public void createUserResponseMatchesSchema() {
        CreateUserRequest payload = new CreateUserRequest("Morpheus", "Leader");

        given()
                .body(payload)
        .when()
                .post("/users/add")
        .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/create-user-schema.json"));
    }

    @Test(description = "POST /auth/login response matches the login JSON schema")
    public void loginResponseMatchesSchema() {
        LoginRequest payload = new LoginRequest("emilys", "emilyspass");

        given()
                .body(payload)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/login-schema.json"));
    }
}
