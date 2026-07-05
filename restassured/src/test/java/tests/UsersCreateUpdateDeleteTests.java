package tests;

import io.restassured.response.Response;
import models.CreateUserRequest;
import models.CreateUserResponse;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static config.ApiConfig.responseTimeSlaMs;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Write-path ({@code POST}/{@code PUT}/{@code PATCH}/{@code DELETE}) coverage
 * for the /users resource, including full response-shape assertions via the
 * {@link CreateUserResponse} model.
 */
public class UsersCreateUpdateDeleteTests extends BaseTest {

    @Test(description = "POST /users creates a new user and returns 201 with id/createdAt populated")
    public void createUserReturns201WithExpectedShape() {
        CreateUserRequest payload = new CreateUserRequest("Morpheus", "Leader");

        Response response = given()
                .body(payload)
        .when()
                .post("/users")
        .then()
                .statusCode(201)
                .time(lessThan(responseTimeSlaMs()))
                .body("name", equalTo("Morpheus"))
                .body("job", equalTo("Leader"))
                .body("id", notNullValue())
                .body("createdAt", notNullValue())
                .extract().response();

        CreateUserResponse created = response.as(CreateUserResponse.class);
        assertNotNull(created.getId(), "Created user id should not be null");
        assertEquals(created.getName(), "Morpheus");
        assertEquals(created.getJob(), "Leader");
    }

    @Test(description = "POST /users with an empty body still returns 201 (reqres accepts arbitrary payloads)")
    public void createUserWithEmptyBodyReturns201() {
        given()
                .body("{}")
        .when()
                .post("/users")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("createdAt", notNullValue());
    }

    @Test(description = "PUT /users/{id} fully updates a user and returns 200 with updatedAt populated")
    public void updateUserWithPutReturns200() {
        CreateUserRequest payload = new CreateUserRequest("Neo", "The One");

        given()
                .pathParam("id", 2)
                .body(payload)
        .when()
                .put("/users/{id}")
        .then()
                .statusCode(200)
                .time(lessThan(responseTimeSlaMs()))
                .body("name", equalTo("Neo"))
                .body("job", equalTo("The One"))
                .body("updatedAt", notNullValue());
    }

    @Test(description = "PATCH /users/{id} partially updates a user and returns 200 with updatedAt populated")
    public void updateUserWithPatchReturns200() {
        CreateUserRequest payload = new CreateUserRequest("Trinity", "Hacker");

        given()
                .pathParam("id", 2)
                .body(payload)
        .when()
                .patch("/users/{id}")
        .then()
                .statusCode(200)
                .time(lessThan(responseTimeSlaMs()))
                .body("name", equalTo("Trinity"))
                .body("job", equalTo("Hacker"))
                .body("updatedAt", notNullValue());
    }

    @Test(description = "DELETE /users/{id} removes a user and returns 204 with an empty body")
    public void deleteUserReturns204() {
        given()
                .pathParam("id", 2)
        .when()
                .delete("/users/{id}")
        .then()
                .statusCode(204)
                .time(lessThan(responseTimeSlaMs()))
                .body(Matchers.emptyOrNullString());
    }

    @Test(description = "Create-then-delete workflow: a freshly created user id can be used in a subsequent DELETE call")
    public void createThenDeleteUserWorkflow() {
        CreateUserRequest payload = new CreateUserRequest("Agent Smith", "Program");

        String createdId = given()
                .body(payload)
        .when()
                .post("/users")
        .then()
                .statusCode(201)
                .extract().path("id");

        assertNotNull(createdId, "Created id should be captured from the POST response");

        given()
                .pathParam("id", createdId)
        .when()
                .delete("/users/{id}")
        .then()
                .statusCode(204);
    }
}
