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

    @Test(description = "GET /users?page= response matches the list-users JSON schema")
    public void listUsersResponseMatchesSchema() {
        given()
                .queryParam("page", 1)
        .when()
                .get("/users")
        .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/list-users-schema.json"));
    }

    @Test(description = "POST /users response matches the create-user JSON schema")
    public void createUserResponseMatchesSchema() {
        CreateUserRequest payload = new CreateUserRequest("Morpheus", "Leader");

        given()
                .body(payload)
        .when()
                .post("/users")
        .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/create-user-schema.json"));
    }

    @Test(description = "POST /register response matches the register JSON schema")
    public void registerResponseMatchesSchema() {
        LoginRequest payload = new LoginRequest("eve.holt@reqres.in", "pistol");

        given()
                .body(payload)
        .when()
                .post("/register")
        .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/register-schema.json"));
    }
}
