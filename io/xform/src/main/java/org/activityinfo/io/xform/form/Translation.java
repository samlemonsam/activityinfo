package org.activityinfo.io.xform.form;

import javax.xml.bind.annotation.XmlAttribute;


public class Translation {
    private String defaultTranslation;
    private String lang;

    @XmlAttribute(name="default")
    public String getDefaultTranslation() {
        return defaultTranslation;
    }

    public void setDefaultTranslation(String defaultTranslation) {
        this.defaultTranslation = defaultTranslation;
    }

    @XmlAttribute
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public static Translation defaultTranslation() {
        Translation translation = new Translation();
        translation.setDefaultTranslation("true()");
        translation.setLang("default");
        return translation;
    }
}
