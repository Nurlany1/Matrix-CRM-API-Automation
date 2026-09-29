package az.crm.api.tests.consultations;

import az.crm.api.base.BaseTest;
import az.crm.api.util.ConsultationPayloads;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Consultations - Create")
class ConsultationsCreateTests extends BaseTest {

    private static final int EXISTING_LEAD_ID = 20;

    @Test
    @DisplayName("Creating a consultation for an existing lead should return 200 but returns 404 - no consultant data seeded")
    void createConsultation_withValidLeadAndConsultant_returns200() {
        given()
                .spec(authSpec())
                .body(ConsultationPayloads.create(EXISTING_LEAD_ID, 1, "created by REST Assured automation"))
        .when()
                .post("/api/Consultations")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    @DisplayName("A non-existent consultantId returns 404")
    void createConsultation_withNonExistentConsultant_returns404() {
        given()
                .spec(authSpec())
                .body(ConsultationPayloads.create(EXISTING_LEAD_ID, 999999, "probe"))
        .when()
                .post("/api/Consultations")
        .then()
                .statusCode(404)
                .body("isSuccess", equalTo(false))
                .body("error.errors", Matchers.hasItem("Konsultant tapılmadı."));
    }

    @Test
    @DisplayName("consultantId=0 is treated the same as not selected and returns 400")
    void createConsultation_withZeroConsultantId_returns400() {
        given()
                .spec(authSpec())
                .body(ConsultationPayloads.create(EXISTING_LEAD_ID, 0, "probe"))
        .when()
                .post("/api/Consultations")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("A non-existent leadId returns 404")
    void createConsultation_withNonExistentLead_returns404() {
        given()
                .spec(authSpec())
                .body(ConsultationPayloads.create(999999, 1, "probe"))
        .when()
                .post("/api/Consultations")
        .then()
                .statusCode(404)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("No Authorization header returns 401")
    void createConsultation_withoutAuth_returns401() {
        given()
                .spec(anonymousSpec)
                .body(ConsultationPayloads.create(EXISTING_LEAD_ID, 1, "probe"))
        .when()
                .post("/api/Consultations")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("leadId omitted is rejected")
    void createConsultation_withoutLeadId_returns400() {
        var body = ConsultationPayloads.create(EXISTING_LEAD_ID, 1, "probe");
        body.remove("leadId");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Consultations")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false))
                .body("error.errors", Matchers.hasItem("Lead seçilməlidir."));
    }

    @Test
    @DisplayName("consultantId omitted is rejected")
    void createConsultation_withoutConsultantId_returns400() {
        var body = ConsultationPayloads.create(EXISTING_LEAD_ID, 1, "probe");
        body.remove("consultantId");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Consultations")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false))
                .body("error.errors", Matchers.hasItem("Konsultant seçilməlidir."));
    }

    @Test
    @DisplayName("scheduledAt omitted is rejected")
    void createConsultation_withoutScheduledAt_returns400() {
        var body = ConsultationPayloads.create(EXISTING_LEAD_ID, 1, "probe");
        body.remove("scheduledAt");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Consultations")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("A scheduledAt in the past is rejected")
    void createConsultation_withPastScheduledAt_returns400() {
        var body = ConsultationPayloads.create(EXISTING_LEAD_ID, 1, "probe");
        body.put("scheduledAt", "2020-01-01T00:00:00Z");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Consultations")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("A leadId of zero or negative is rejected with 400 rather than a server error")
    void createConsultation_withNegativeLeadId_returns400() {
        var body = ConsultationPayloads.create(-5, 1, "probe");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Consultations")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("An invalid format enum value is rejected with 400, not a server error")
    void createConsultation_withInvalidFormatEnum_returns400() {
        var body = ConsultationPayloads.create(EXISTING_LEAD_ID, 1, "probe");
        body.put("format", "Hybrid");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Consultations")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("The format field entirely omitted is still validated against the lead/consultant first")
    void createConsultation_withoutFormat_returnsConsultantLookupError() {
        var body = ConsultationPayloads.create(EXISTING_LEAD_ID, 1, "probe");
        body.remove("format");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Consultations")
        .then()
                .statusCode(404);
    }
}
