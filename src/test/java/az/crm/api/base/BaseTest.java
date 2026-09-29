package az.crm.api.base;

import az.crm.api.config.ConfigProvider;
import az.crm.api.util.TokenManager;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseTest {

    protected static RequestSpecification anonymousSpec;

    @BeforeAll
    static void baseSetUp() {
        RestAssured.baseURI = ConfigProvider.baseUrl();

        anonymousSpec = new RequestSpecBuilder()
                .setContentType("application/json")
                .log(LogDetail.METHOD)
                .build();
    }

    protected static RequestSpecification authSpec() {
        return new RequestSpecBuilder()
                .setContentType("application/json")
                .addHeader("Authorization", "Bearer " + TokenManager.getAccessToken())
                .log(LogDetail.METHOD)
                .build();
    }
}
