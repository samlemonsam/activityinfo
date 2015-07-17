package org.activityinfo.io.xform.form;

import javax.xml.bind.annotation.XmlAttribute;

public class Input extends BodyElement {

    private String query;
    private String labelPath;
    private String valuePath;

    /**
     * XPath query against an external item set
     *
     * <p><code>instance('counties')/root/item[state=/new_cascading_select/state ]"</code></p>
     */
    @XmlAttribute
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }


}
