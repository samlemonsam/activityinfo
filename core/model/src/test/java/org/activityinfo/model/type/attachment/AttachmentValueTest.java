package org.activityinfo.model.type.attachment;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class AttachmentValueTest {

    @Test
    public void testParse() {
        String json = "{\"@type\":\"ATTACHMENT\", \"values\":[{\"mimeType\":\"image/jpeg\", \"width\":0, \"height\":0, \"filename\":\"12.jpg\", \"blobId\":\"XYZ123\"}]}";

        AttachmentValue fieldValue = AttachmentValue.fromJson(json);
        assertThat(fieldValue.getValues(), Matchers.hasSize(1));

        Attachment attachment = fieldValue.getValues().get(0);
        assertThat(attachment.getFilename(), equalTo("12.jpg"));
        assertThat(attachment.getMimeType(), equalTo("image/jpeg"));
        assertThat(attachment.getBlobId(), equalTo("XYZ123"));
        assertThat(attachment.getWidth(), equalTo(0));
        assertThat(attachment.getHeight(), equalTo(0));
    }

    @Test
    public void parseMultiple() {
        String json = " {\"@type\":\"ATTACHMENT\", \"values\":[" +
                "{\"mimeType\":\"image/jpeg\", \"width\":0, \"height\":0, \"filename\":\"Scan_20160712_132842_003.jpg\", \"blobId\":\"XYZ1\"}," +
                "{\"mimeType\":\"image/jpeg\", \"width\":0, \"height\":0, \"filename\":\"Scan_20160712_132842_002.jpg\", \"blobId\":\"XYZ2\"}," +
                "{\"mimeType\":\"image/jpeg\", \"width\":0, \"height\":0, \"filename\":\"Scan_20160712_132842_001.jpg\", \"blobId\":\"XYZ3\"}," +
                "{\"mimeType\":\"image/jpeg\", \"width\":0, \"height\":0, \"filename\":\"Scan_20160712_132842.jpg\", \"blobId\":\"XYZ4\"}]}";

        AttachmentValue fieldValue = AttachmentValue.fromJson(json);
        assertThat(fieldValue.getValues(), Matchers.hasSize(4));

        assertThat(fieldValue.getValues().get(0).getBlobId(), equalTo("XYZ1"));
        assertThat(fieldValue.getValues().get(1).getBlobId(), equalTo("XYZ2"));
        assertThat(fieldValue.getValues().get(2).getBlobId(), equalTo("XYZ3"));
        assertThat(fieldValue.getValues().get(3).getBlobId(), equalTo("XYZ4"));

    }

}