package az.crm.api.tests.consultations;

import az.crm.api.base.BaseTest;
import az.crm.api.util.ConsultationPayloads;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@DisplayName("Consultations - Update")
class ConsultationsUpdateTests extends BaseTest {

    @Test
    @DisplayName("Updating a non-existent consultation should return 404 but returns 405 Method Not Allowed")
    void updateConsultation_nonExistentId_returns404() {
        given()
                .spec(authSpec())
                .body(ConsultationPayloads.update(999999, 1, "updated by automation"))
        .when()
                .put("/api/Consultations")
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("The 405 block happens even without auth")
    void updateConsultation_withoutAuth_isBlockedBeforeAuthCheck() {
        given()
                .spec(anonymousSpec)
                .body(ConsultationPayloads.update(1, 1, "no auth"))
        .when()
                .put("/api/Consultations")
        .then()
                .statusCode(405);
    }
}
