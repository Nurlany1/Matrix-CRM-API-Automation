package az.crm.api.tests.consultations;

import az.crm.api.base.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@DisplayName("Consultations - Delete")
class ConsultationsDeleteTests extends BaseTest {

    @Test
    @DisplayName("Deleting a non-existent consultation should return 404 but returns 405 Method Not Allowed")
    void deleteConsultation_nonExistentId_returns404() {
        given()
                .spec(authSpec())
        .when()
                .delete("/api/Consultations/{id}", 999999)
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Blocked at 405 regardless of the id requested")
    void deleteConsultation_anyId_isBlockedAt405() {
        given()
                .spec(authSpec())
        .when()
                .delete("/api/Consultations/{id}", 1)
        .then()
                .statusCode(405);
    }

    @Test
    @DisplayName("The 405 block happens even without auth")
    void deleteConsultation_withoutAuth_isBlockedBeforeAuthCheck() {
        given()
                .spec(anonymousSpec)
        .when()
                .delete("/api/Consultations/{id}", 999999)
        .then()
                .statusCode(405);
    }
}
