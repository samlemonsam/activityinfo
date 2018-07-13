package org.activityinfo.server.mail;

import org.activityinfo.json.JsonValue;

public class OpenReport {

    private String recordType;
    private boolean firstOpen;
    private UserClient client;
    private UserClient os;
    private String platform;
    private String UserAgent;
    private int readSeconds;
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

    public boolean isFirstOpen() {
        return firstOpen;
    }

    public void setFirstOpen(boolean firstOpen) {
        this.firstOpen = firstOpen;
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

    public int getReadSeconds() {
        return readSeconds;
    }

    public void setReadSeconds(int readSeconds) {
        this.readSeconds = readSeconds;
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

    public static OpenReport fromJson(JsonValue object) {
        OpenReport openReport = new OpenReport();

        if (!object.hasKey("RecordType")) {
            throw new IllegalArgumentException("No RecordType defined");
        }
        if (object.get("RecordType").isJsonNull() || !object.get("RecordType").asString().equals("Open")) {
            throw new IllegalArgumentException("RecordType is not Bounce");
        }

        openReport.setRecordType(object.get("RecordType").asString());
        openReport.setFirstOpen(object.get("FirstOpen").asBoolean());

        UserClient client = UserClient.fromJson(object.get("Client"));
        openReport.setClient(client);

        UserClient os = UserClient.fromJson(object.get("OS"));
        openReport.setOs(os);

        openReport.setPlatform(object.get("Platform").asString());
        openReport.setUserAgent(object.get("UserAgent").asString());
        openReport.setReadSeconds(object.get("ReadSeconds").asInt());
        openReport.setMessageId(object.get("MessageID").asString());
        openReport.setReceivedAt(object.get("ReceivedAt").asString());
        openReport.setTag(object.get("Tag").asString());
        openReport.setRecipient(object.get("Recipient").asString());

        return openReport;
    }
}
