package org.activityinfo.io.xform.form;


import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class IText {
    private final List<Translation> translation = Lists.newArrayList();

    public IText() {
    }

    @XmlElement(name = "translation")
    public List<Translation> getTranslations() {
        return translation;
    }
}
