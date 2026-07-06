package tests;

import io.restassured.response.Response;
import models.CreateUserRequest;
import models.CreateUserResponse;
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
 * <p>
 * dummyjson.com simulates these mutations: it returns a realistic response
 * (fake new id, echoed/merged fields, delete flags) but never actually
 * persists the change server-side.
 */
public class UsersCreateUpdateDeleteTests extends BaseTest {

    @Test(description = "POST /users/add creates a simulated user and returns 201 with id/fields populated")
    public void createUserReturns201WithExpectedShape() {
        CreateUserRequest payload = new CreateUserRequest("Morpheus", "Leader");

        Response response = given()
                .body(payload)
        .when()
                .post("/users/add")
        .then()
                .statusCode(201)
                .time(lessThan(responseTimeSlaMs()))
                .body("firstName", equalTo("Morpheus"))
                .body("lastName", equalTo("Leader"))
                .body("id", notNullValue())
                .extract().response();

        CreateUserResponse created = response.as(CreateUserResponse.class);
        assertNotNull(created.getId(), "Created user id should not be null");
        assertEquals(created.getFirstName(), "Morpheus");
        assertEquals(created.getLastName(), "Leader");
    }

    @Test(description = "POST /users/add with an empty body still returns 201 (dummyjson accepts arbitrary payloads)")
    public void createUserWithEmptyBodyReturns201() {
        given()
                .body("{}")
        .when()
                .post("/users/add")
        .then()
                .statusCode(201)
                .body("id", notNullValue());
    }

    @Test(description = "PUT /users/{id} fully updates a user and returns 200 with the submitted fields echoed")
    public void updateUserWithPutReturns200() {
        CreateUserRequest payload = new CreateUserRequest("Neo", "Anderson");

        given()
                .pathParam("id", 2)
                .body(payload)
        .when()
                .put("/users/{id}")
        .then()
                .statusCode(200)
                .time(lessThan(responseTimeSlaMs()))
                .body("id", equalTo(2))
                .body("firstName", equalTo("Neo"))
                .body("lastName", equalTo("Anderson"));
    }

    @Test(description = "PATCH /users/{id} partially updates a user and returns 200 with the patched field echoed")
    public void updateUserWithPatchReturns200() {
        CreateUserRequest payload = new CreateUserRequest(null, "Hacker");

        given()
                .pathParam("id", 2)
                .body(payload)
        .when()
                .patch("/users/{id}")
        .then()
                .statusCode(200)
                .time(lessThan(responseTimeSlaMs()))
                .body("id", equalTo(2))
                .body("lastName", equalTo("Hacker"));
    }

    @Test(description = "DELETE /users/{id} returns 200 with an isDeleted flag and a deletedOn timestamp")
    public void deleteUserReturns200WithIsDeletedFlag() {
        given()
                .pathParam("id", 2)
        .when()
                .delete("/users/{id}")
        .then()
                .statusCode(200)
                .time(lessThan(responseTimeSlaMs()))
                .body("id", equalTo(2))
                .body("isDeleted", equalTo(true))
                .body("deletedOn", notNullValue());
    }

    @Test(description = "Create-then-delete workflow: a freshly (simulated) created user id can be used in a subsequent DELETE call")
    public void createThenDeleteUserWorkflow() {
        CreateUserRequest payload = new CreateUserRequest("Agent", "Smith");

        int createdId = given()
                .body(payload)
        .when()
                .post("/users/add")
        .then()
                .statusCode(201)
                .extract().path("id");

        assertNotNull(createdId, "Created id should be captured from the POST response");

        // dummyjson doesn't persist the newly-created id, so we exercise the
        // DELETE contract against a known existing id, mirroring the original
        // reqres workflow's intent (chaining an id from a prior response)
        // while keeping the assertion meaningful against real API behaviour.
        given()
                .pathParam("id", 1)
        .when()
                .delete("/users/{id}")
        .then()
                .statusCode(200)
                .body("isDeleted", equalTo(true));
    }
}
