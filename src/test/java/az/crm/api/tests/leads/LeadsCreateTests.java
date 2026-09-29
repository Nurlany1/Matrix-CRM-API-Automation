package az.crm.api.tests.leads;

import az.crm.api.base.BaseTest;
import az.crm.api.util.LeadPayloads;
import az.crm.api.util.ReferenceData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Leads - Create")
class LeadsCreateTests extends BaseTest {

    @Test
    @DisplayName("A fully valid payload should return 200 but currently returns 500")
    void createLead_withValidPayload_returns200() {
        given()
                .spec(authSpec())
                .body(LeadPayloads.validCreate("Automation", "Valid-" + System.currentTimeMillis()))
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("data.id", notNullValue())
                .body("data.status.name", notNullValue());
    }

    @Test
    @DisplayName("Empty contacts array is rejected with 400 ContactsRequired")
    void createLead_withEmptyContacts_returns400() {
        given()
                .spec(authSpec())
                .body(LeadPayloads.missingContacts("Automation", "NoContacts"))
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false))
                .body("error.errors", hasItem("ContactsRequired"));
    }

    @Test
    @DisplayName("Empty sourceIds array is rejected with 400 SourcesRequired")
    void createLead_withEmptySources_returns400() {
        given()
                .spec(authSpec())
                .body(LeadPayloads.missingSources("Automation", "NoSources"))
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false))
                .body("error.errors", hasItem("SourcesRequired"));
    }

    @Test
    @DisplayName("Neither trainingIds nor otherTrainingName is rejected with 400 TrainingOrOtherRequired")
    void createLead_withNoTrainingAndNoOtherTrainingName_returns400() {
        var body = LeadPayloads.validCreate("Automation", "NoTraining");
        body.put("trainingIds", List.of());
        body.put("otherTrainingName", null);

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false))
                .body("error.errors", hasItem("TrainingOrOtherRequired"));
    }

    @Test
    @DisplayName("contacts field entirely omitted is rejected with a 4xx")
    void createLead_withMissingContactsKey_returns400() {
        var body = LeadPayloads.validCreate("Automation", "MissingKey");
        body.remove("contacts");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("No Authorization header returns 401")
    void createLead_withoutAuth_returns401() {
        given()
                .spec(anonymousSpec)
                .body(LeadPayloads.validCreate("NoAuth", "User"))
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("firstName entirely omitted is rejected with 400")
    void createLead_withoutFirstName_returns400() {
        var body = LeadPayloads.validCreate("Automation", "NoFirstName");
        body.remove("firstName");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("firstName as an empty string is rejected")
    void createLead_withBlankFirstName_returns400() {
        var body = LeadPayloads.validCreate("", "BlankFirstName");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("firstName made up of only whitespace is trimmed and rejected as empty")
    void createLead_withWhitespaceOnlyFirstName_returns400() {
        var body = LeadPayloads.validCreate("   ", "WhitespaceFirstName");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("applicationDate entirely omitted is rejected")
    void createLead_withoutApplicationDate_returns400() {
        var body = LeadPayloads.validCreate("Automation", "NoAppDate");
        body.remove("applicationDate");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("A non-parseable applicationDate string is rejected with 400")
    void createLead_withInvalidDateFormat_returns400() {
        var body = LeadPayloads.validCreate("Automation", "BadDate");
        body.put("applicationDate", "not-a-date");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("lastName omitted should return 400 like firstName does, but returns 500")
    void createLead_withoutLastName_returns400() {
        var body = LeadPayloads.validCreate("Automation", "placeholder");
        body.remove("lastName");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("lastName as an empty string should return 400 like firstName does, but returns 500")
    void createLead_withBlankLastName_returns400() {
        var body = LeadPayloads.validCreate("Automation", "");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("An unknown extra JSON property should not crash the request with 500")
    void createLead_withUnknownExtraProperty_shouldNotReturn500() {
        var body = LeadPayloads.validCreate("Automation", "ExtraProp");
        body.put("hackerField", "unexpected");

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("A non-existent contactTypeId should return 400/404, not crash with 500")
    void createLead_withNonExistentContactTypeId_shouldNotReturn500() {
        var body = LeadPayloads.validCreate("Automation", "BadContactType");
        body.put("contacts", List.of(LeadPayloads.contact(999_999, "+994501112233")));

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("A non-existent trainingId should return 400/404, not crash with 500")
    void createLead_withNonExistentTrainingId_shouldNotReturn500() {
        var body = LeadPayloads.validCreate("Automation", "BadTraining");
        body.put("trainingIds", List.of(999_999));

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("A non-existent sourceId should return 400/404, not crash with 500")
    void createLead_withNonExistentSourceId_shouldNotReturn500() {
        var body = LeadPayloads.validCreate("Automation", "BadSource");
        body.put("sourceIds", List.of(999_999));

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("A negative sourceId should return 400/404, not crash with 500")
    void createLead_withNegativeSourceId_shouldNotReturn500() {
        var body = LeadPayloads.validCreate("Automation", "NegativeSource");
        body.put("sourceIds", List.of(-1));

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Duplicate values inside sourceIds should be handled gracefully, not crash with 500")
    void createLead_withDuplicateSourceIds_shouldNotReturn500() {
        int sourceId = ReferenceData.anySourceId();
        var body = LeadPayloads.validCreate("Automation", "DuplicateSources");
        body.put("sourceIds", List.of(sourceId, sourceId));

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Duplicate values inside trainingIds should be handled gracefully, not crash with 500")
    void createLead_withDuplicateTrainingIds_shouldNotReturn500() {
        int trainingId = ReferenceData.anyTrainingId();
        var body = LeadPayloads.validCreate("Automation", "DuplicateTrainings");
        body.put("trainingIds", List.of(trainingId, trainingId));

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Two valid contacts of different types should not crash the request with 500")
    void createLead_withTwoContacts_shouldNotReturn500() {
        var body = LeadPayloads.validCreate("Automation", "TwoContacts");
        body.put("contacts", List.of(
                LeadPayloads.contact(ReferenceData.anyContactTypeId(), "+994501112233"),
                LeadPayloads.contact(ReferenceData.anotherContactTypeId(), "automation@example.com")));

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("An empty contact value is rejected with a clear validation message")
    void createLead_withEmptyContactValue_returns400() {
        var body = LeadPayloads.validCreate("Automation", "EmptyContactValue");
        body.put("contacts", List.of(LeadPayloads.contact(ReferenceData.anyContactTypeId(), "")));

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("additionalNote longer than the documented limit is rejected")
    void createLead_withOverlongAdditionalNote_returns400() {
        var body = LeadPayloads.validCreate("Automation", "LongNote");
        body.put("additionalNote", "a".repeat(2000));

        given()
                .spec(authSpec())
                .body(body)
        .when()
                .post("/api/Leads")
        .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }
}
