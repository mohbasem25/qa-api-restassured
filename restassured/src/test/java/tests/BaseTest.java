package tests;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import specs.RequestSpecs;

/**
 * Common test base: wires the default {@link RequestSpecification} into
 * every test class so individual tests can focus purely on business logic
 * and assertions.
 */
public abstract class BaseTest {

    protected RequestSpecification requestSpec;

    @BeforeClass(alwaysRun = true)
    public void setUpBaseSpec() {
        requestSpec = RequestSpecs.baseSpec();
        RestAssured.requestSpecification = requestSpec;
    }
}
