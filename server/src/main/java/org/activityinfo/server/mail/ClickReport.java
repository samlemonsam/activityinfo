package org.activityinfo.server.mail;

import org.activityinfo.json.JsonValue;

public class ClickReport {

    private String recordType;
    private String clickLocation;
    private UserClient client;
    private UserClient os;
    private String platform;
    private String UserAgent;
    private String originalLink;
    private String messageId;
    private String receivedAt;
    private String tag;
    private String recipient;

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getClickLocation() {
        return clickLocation;
    }

    public void setClickLocation(String clickLocation) {
        this.clickLocation = clickLocation;
    }

    public UserClient getClient() {
        return client;
    }

    public void setClient(UserClient client) {
        this.client = client;
    }

    public UserClient getOs() {
        return os;
    }

    public void setOs(UserClient os) {
        this.os = os;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUserAgent() {
        return UserAgent;
    }

    public void setUserAgent(String userAgent) {
        UserAgent = userAgent;
    }

    public String getOriginalLink() {
        return originalLink;
    }

    public void setOriginalLink(String originalLink) {
        this.originalLink = originalLink;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public static ClickReport fromJson(JsonValue object) {
        ClickReport clickReport = new ClickReport();

        if (!object.hasKey("RecordType")) {
            throw new IllegalArgumentException("No RecordType defined");
        }
        if (object.get("RecordType").isJsonNull() || !object.get("RecordType").asString().equals("Click")) {
            throw new IllegalArgumentException("RecordType is not Bounce");
        }

        clickReport.setRecordType(object.get("RecordType").asString());
        clickReport.setClickLocation(object.get("ClickLocation").asString());

        UserClient client = UserClient.fromJson(object.get("Client"));
        clickReport.setClient(client);

        UserClient os = UserClient.fromJson(object.get("OS"));
        clickReport.setOs(os);

        clickReport.setPlatform(object.get("Platform").asString());
        clickReport.setUserAgent(object.get("UserAgent").asString());
        clickReport.setOriginalLink(object.get("OriginalLink").asString());
        clickReport.setMessageId(object.get("MessageID").asString());
        clickReport.setReceivedAt(object.get("ReceivedAt").asString());
        clickReport.setTag(object.get("Tag").asString());
        clickReport.setRecipient(object.get("Recipient").asString());

        return clickReport;
    }
}
