package org.activityinfo.server.endpoint.odk.xform;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.endpoint.odk.build.XPathBuilder;
import org.activityinfo.server.endpoint.odk.OdkHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.mail.MessagingException;
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

/**
 * After {@link LegacyXFormInstance} is eliminated, this class can be merged with {@link XFormInstance} again
 */
public class XFormInstanceImpl implements XFormInstance {

    private static final Logger LOGGER = Logger.getLogger(XFormInstanceImpl.class.getName());
    private Document document;
    private MimeMultipart mimeMultipart;

    private Element data;
    private Element meta;
    private String instanceId;
    private ResourceId formClassId;
    private String userId;

    public XFormInstanceImpl(byte[] bytes) {
        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(bytes, MediaType.MULTIPART_FORM_DATA);

        parseDocument(byteArrayDataSource);
        parseHeader();
    }

    private void parseDocument(ByteArrayDataSource byteArrayDataSource) {
        try {
            mimeMultipart = new MimeMultipart(byteArrayDataSource);
            InputStream inputStream = mimeMultipart.getBodyPart(0).getInputStream();
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = documentBuilder.parse(inputStream);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to parse input", e);
            throw new WebApplicationException(Response.status(BAD_REQUEST).build());
        }

        document.normalizeDocument();
    }

    private void parseHeader() {
        data = document.getDocumentElement();
        formClassId = ResourceId.valueOf(data.getAttribute("id"));
        meta = findElement(data, "meta");
        instanceId = extractText(findElement(meta, "instanceID")).replace("-", "");
        userId = extractText(findElement(meta, "userID"));
    }

    private String extractText(Element element) {
        return OdkHelper.extractText(element);
    }

    @Override
    public String getAuthenticationToken() {
        return userId;
    }

    @Override
    public String getId() {
        return instanceId;
    }

    @Override
    public Optional<Element> getFieldContent(ResourceId fieldId) {
        String tagName = XPathBuilder.fieldTagName(fieldId);
        NodeList nodeList = document.getElementsByTagName(tagName);

        if (nodeList.getLength() > 0) {
            return Optional.of((Element) nodeList.item(0));
        } else {
            return Optional.absent();
        }
    }

    public javax.mail.BodyPart findBodyPartByFilename(String filename) throws MessagingException {
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            javax.mail.BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (filename.equals(bodyPart.getFileName())) {
                return bodyPart;
            }
        }
        LOGGER.log(Level.SEVERE, "Could not find the specified filename");
        throw new WebApplicationException(Response.status(BAD_REQUEST).build());
    }

    static Element findElement(Element parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for(int i=0;i!=children.getLength();++i) {
            Node child = children.item(i);
            if(child instanceof Element) {
                if(((Element) child).getTagName().equals(tagName)) {
                    return (Element) child;
                }
            }
        }
        throw new IllegalStateException("Cannot find element " + tagName);
    }

    @Override
    public ResourceId getFormClassId() {
        return formClassId;
    }
}
