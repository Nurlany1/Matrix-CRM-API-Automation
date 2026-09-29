package az.crm.api.tests.leads;

import az.crm.api.base.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@DisplayName("Leads - Delete")
class LeadsDeleteTests extends BaseTest {

    @Test
    @DisplayName("Deleting a non-existent lead should return 404 but returns 405 Method Not Allowed")
    void deleteLead_nonExistentId_returns404() {
        given()
                .spec(authSpec())
        .when()
                .delete("/api/Leads/{id}", 999999)
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Deleting an existing lead should return 200 but returns 405 Method Not Allowed")
    void deleteLead_existingId_returns200() {
        given()
                .spec(authSpec())
        .when()
                .delete("/api/Leads/{id}", 20)
        .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("The 405 block happens even without auth")
    void deleteLead_withoutAuth_isBlockedBeforeAuthCheck() {
        given()
                .spec(anonymousSpec)
        .when()
                .delete("/api/Leads/{id}", 999999)
        .then()
                .statusCode(405)
                .header("Allow", containsString("OPTIONS"));
    }
}
