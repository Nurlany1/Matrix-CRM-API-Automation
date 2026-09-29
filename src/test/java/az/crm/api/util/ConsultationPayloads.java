package az.crm.api.util;

import java.util.HashMap;
import java.util.Map;

public final class ConsultationPayloads {

    private ConsultationPayloads() {
    }

    public static Map<String, Object> create(int leadId, int consultantId, String note) {
        Map<String, Object> body = new HashMap<>();
        body.put("leadId", leadId);
        body.put("consultantId", consultantId);
        body.put("scheduledAt", "2026-09-25T10:00:00Z");
        body.put("format", "Online");
        body.put("note", note);
        return body;
    }

    public static Map<String, Object> update(int id, int consultantId, String note) {
        Map<String, Object> body = new HashMap<>();
        body.put("id", id);
        body.put("consultantId", consultantId);
        body.put("scheduledAt", "2026-09-26T12:00:00Z");
        body.put("format", "Offline");
        body.put("note", note);
        return body;
    }

    public static Map<String, Object> statusUpdate(int consultationStatusId, String statusNote) {
        Map<String, Object> body = new HashMap<>();
        body.put("consultationStatusId", consultationStatusId);
        body.put("statusNote", statusNote);
        return body;
    }
}
