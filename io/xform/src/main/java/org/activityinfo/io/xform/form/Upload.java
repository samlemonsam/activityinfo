package org.activityinfo.io.xform.form;

import javax.xml.bind.annotation.XmlAttribute;

public class Upload extends BodyElement {

    private String mediaType;

    @SuppressWarnings("SpellCheckingInspection")
    @XmlAttribute(name = "mediatype")
    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

}
