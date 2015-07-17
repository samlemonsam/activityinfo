package org.activityinfo.server.endpoint.odk.xform;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.activityinfo.model.legacy.CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN;
import static org.activityinfo.model.legacy.CuidAdapter.COMMENT_FIELD;
import static org.activityinfo.model.legacy.CuidAdapter.END_DATE_FIELD;
import static org.activityinfo.model.legacy.CuidAdapter.GPS_FIELD;
import static org.activityinfo.model.legacy.CuidAdapter.INDICATOR_DOMAIN;
import static org.activityinfo.model.legacy.CuidAdapter.LOCATION_NAME_FIELD;
import static org.activityinfo.model.legacy.CuidAdapter.PARTNER_FIELD;
import static org.activityinfo.model.legacy.CuidAdapter.PROJECT_FIELD;
import static org.activityinfo.model.legacy.CuidAdapter.START_DATE_FIELD;
import static org.activityinfo.model.legacy.CuidAdapter.activityFormClass;
import static org.activityinfo.model.legacy.CuidAdapter.field;
import static org.activityinfo.model.legacy.CuidAdapter.getLegacyIdFromCuid;
import static org.activityinfo.server.endpoint.odk.OdkHelper.extractText;
import static org.activityinfo.server.endpoint.odk.xform.XFormInstanceImpl.findElement;

/**
 * This class exists to support the older XForm instances generated when using AI version 2.8. It can be removed as soon
 * as such support is no longer necessary.
 */
public class LegacyXFormInstance implements XFormInstance {
    private static final Logger LOGGER = Logger.getLogger(LegacyXFormInstance.class.getName());

    private Document document;

    private String id;
    private int activity;

    public LegacyXFormInstance(byte[] bytes) {
        try {
            MimeMultipart multipart = new MimeMultipart(new ByteArrayDataSource(bytes, MediaType.MULTIPART_FORM_DATA));
            InputStream inputStream = multipart.getBodyPart(0).getInputStream();
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = documentBuilder.parse(inputStream);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to parse input", e);
            throw new WebApplicationException(Response.status(BAD_REQUEST).build());
        }

        document.normalizeDocument();

        Element data = document.getDocumentElement();
        Element meta = findElement(data, "meta");
        activity = Integer.parseInt(extractText(findElement(data, "activity")));
        id = extractText(findElement(meta, "instanceID")).replace("-", "");
    }

    @Override
    public String getAuthenticationToken() {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<Element> getFieldContent(ResourceId fieldId) {
        NodeList nodeList = document.getElementsByTagName(fieldTagName(fieldId));

        if (nodeList.getLength() > 0) {
            return Optional.of((Element) nodeList.item(0));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public ResourceId getFormClassId() {
        return activityFormClass(activity);
    }

    private String fieldTagName(ResourceId fieldId) {
        if (field(getFormClassId(), PARTNER_FIELD).equals(fieldId)) {
            return "partner";
        } else if (field(getFormClassId(), PROJECT_FIELD).equals(fieldId)) {
            return "project";
        } else if (field(getFormClassId(), LOCATION_NAME_FIELD).equals(fieldId)) {
            return "locationname";
        } else if (field(getFormClassId(), GPS_FIELD).equals(fieldId)) {
            return "gps";
        } else if (field(getFormClassId(), START_DATE_FIELD).equals(fieldId)) {
            return "date1";
        } else if (field(getFormClassId(), END_DATE_FIELD).equals(fieldId)) {
            return "date2";
        } else if (field(getFormClassId(), COMMENT_FIELD).equals(fieldId)) {
            return "comments";
        } else {
            char domain = fieldId.getDomain();

            if (domain == INDICATOR_DOMAIN) {
                return "I" + getLegacyIdFromCuid(fieldId);
            } else if (domain == ATTRIBUTE_GROUP_FIELD_DOMAIN) {
                return "AG" + getLegacyIdFromCuid(fieldId);
            } else {
                return "WONT'T BE FOUND";   // This is obviously not a valid field name
            }
        }
    }
}
