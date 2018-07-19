package org.activityinfo.server.mail;

import org.activityinfo.json.JsonValue;

public class DeliveryReport {

    private String recordType;
    private String messageId;
    private String recipient;
    private String tag;
    private String deliveredAt;
    private String details;

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(String deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public static DeliveryReport fromJson(JsonValue object) {
        DeliveryReport deliveryReport = new DeliveryReport();

        if (!object.hasKey("RecordType")) {
            throw new IllegalArgumentException("No RecordType defined");
        }
        if (object.get("RecordType").isJsonNull() || !object.get("RecordType").asString().equals("Delivery")) {
            throw new IllegalArgumentException("RecordType is not Bounce");
        }

        deliveryReport.setRecordType(object.get("RecordType").asString());
        deliveryReport.setMessageId(object.get("MessageID").asString());
        deliveryReport.setRecipient(object.get("Recipient").asString());
        deliveryReport.setTag(object.get("Tag").asString());
        deliveryReport.setDeliveredAt(object.get("DeliveredAt").asString());
        deliveryReport.setDetails(object.get("Details").asString());

        return deliveryReport;
    }
}
