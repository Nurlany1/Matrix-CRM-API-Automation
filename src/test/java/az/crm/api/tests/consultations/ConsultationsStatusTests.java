package az.crm.api.tests.consultations;

import az.crm.api.base.BaseTest;
import az.crm.api.util.ConsultationPayloads;
import az.crm.api.util.ReferenceData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Consultations - Status transitions")
class ConsultationsStatusTests extends BaseTest {

    @Test
    @DisplayName("Updating the status of a non-existent consultation returns 404")
    void updateStatus_nonExistentConsultation_returns404() {
        int anyStatusId = ReferenceData.consultationStatusIdWithoutBehavior("NoteRequired");

        given()
                .spec(authSpec())
                .pathParam("id", 999999)
                .body(ConsultationPayloads.statusUpdate(anyStatusId, null))
        .when()
                .patch("/api/Consultations/{id}/status")
        .then()
                .statusCode(404)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("Without auth returns 401")
    void updateStatus_withoutAuth_returns401() {
        given()
                .spec(anonymousSpec)
                .pathParam("id", 999999)
                .body(ConsultationPayloads.statusUpdate(ReferenceData.consultationStatusIdWithoutBehavior("NoteRequired"), null))
        .when()
                .patch("/api/Consultations/{id}/status")
        .then()
                .statusCode(401);
    }
}
