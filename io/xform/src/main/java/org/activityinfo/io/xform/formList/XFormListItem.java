package org.activityinfo.io.xform.formList;




import org.activityinfo.io.xform.Namespaces;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;

public class XFormListItem {
    private String formId;
    private String name;
    private String version;
    private String hash;
    private String descriptionText;
    private String downloadUrl;
    private String manifestUrl;

    /**
     * Either the id attribute of the top-level group within the default model instance or the
     * xmlns namespace attribute (explicitly defined -- not inherited from the surrounding form) of that group.
     * The id attribute value takes precedence if present.
     * Openrosa- compliant forms are expected to have defined at least one of these.
     *
     * @return the form id
     */
    @XmlElement(name = "formID")
    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    /**
     * The user-friendly display name of the form. The server may localize this name
     * (translate it) based upon the Accept-Language: header on the incoming request.
     * Devices SHOULD send this header and servers MAY return different name and description
     * text based upon its value. The default behavior is to return the text within the <title> element of the Xform.
     */
    @XmlElement(namespace = Namespaces.XFORM_LIST)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Is the value of the string-valued version attribute on the top-level group within the default model instance.
     * The device MAY use this to determine if its xform definition is out of sync with the server (over time,
     * the server may roll the current version forward or backward).
     *
     */
    @XmlElement
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Contains the hash value of the form definition file available for download.
     * The only hash values currently supported are MD5 hashes of the file contents;
     * they are prefixed by md5:. If the hash value identified in the form list differs
     * from the hash value for a previously-downloaded form definition file,
     * then the file should be re-fetched from the server.
     *
     * <p>Note: not currently used by ODK</p>
     *
     */
    @Nullable
    @XmlElement
    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @XmlElement
    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    /**
     * s a fully formed URL for downloading the form to the device. It may be a valid http or
     * https URL of any structure; the server may require authentication; the server may
     * require a secure (https) channel, etc.
     */
    @XmlElement
    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(URI downloadUrl) {
        this.downloadUrl = downloadUrl.toString();
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * If present, it defines a URL from which the device can obtain a
     * manifest defining additional supporting objects and files.
     */
    @Nullable
    @XmlElement
    public String getManifestUrl() {
        return manifestUrl;
    }


    public void setManifestUrl(URI manifestUrl) {
        this.manifestUrl = manifestUrl.toString();
    }

    public void setManifestUrl(String manifestUrl) {
        this.manifestUrl = manifestUrl;
    }
}
