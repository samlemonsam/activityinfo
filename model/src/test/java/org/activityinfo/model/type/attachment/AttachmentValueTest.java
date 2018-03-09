/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    @Test
    public void parseNewMultiple() {
        String json = "[{\"mimeType\":\"application/octet-stream\",\"width\":0,\"height\":0," +
                "\"filename\":\"Maharashtra Budget 2017-18 Overview english.docx\",\"blobId\":\"cj11ps9053\"}," +
                "{\"mimeType\":\"application/octet-stream\",\"width\":0,\"height\":0,\"filename\":\"Health.docx\",\"blobId\":\"cj11ptcya4\"}," +
                "{\"mimeType\":\"application/octet-stream\",\"width\":0,\"height\":0,\"filename\":\"ABCD English 2017-18.docx\",\"blobId\":\"cj11ptjqe5\"}," +
                "{\"mimeType\":\"application/msword\",\"width\":0,\"height\":0,\"filename\":\"School Education 20017-18.doc\",\"blobId\":\"cj11ptt5w6\"}," +
                "{\"mimeType\":\"application/octet-stream\",\"width\":0,\"height\":0,\"filename\":\"Water supply and water resources English.docx\",\"blobId\":\"cj11ptzpo7\"}," +
                "{\"mimeType\":\"application/msword\",\"width\":0,\"height\":0,\"filename\":\"Food security english for  17-18 march 22 (1).doc\",\"blobId\":\"cj11pu5hl8\"}," +
                "{\"mimeType\":\"application/octet-stream\",\"width\":0,\"height\":0,\"filename\":\"Housing.docx\",\"blobId\":\"cj11pumvf9\"}]";

        AttachmentValue fieldValue = AttachmentValue.fromJson(json);
        assertThat(fieldValue.getValues(), Matchers.hasSize(7));

    }

}