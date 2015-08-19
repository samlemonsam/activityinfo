package org.activityinfo.i18n.tools.po;

import com.google.common.base.Strings;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

public class PoDefinition {
    private String form;

    @JsonSerialize(include= JsonSerialize.Inclusion.NON_DEFAULT)
    private double fuzzy;
    
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private DateTime updated;

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public double getFuzzy() {
        return fuzzy;
    }

    public void setFuzzy(double fuzzy) {
        this.fuzzy = fuzzy;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }
    
    @JsonSetter
    public void setUpdated(String updated) {
        if(Strings.isNullOrEmpty(updated)) {
            this.updated = null;
        } else {
            this.updated = PoTerm.DATE_TIME_FORMAT.parseDateTime(updated);
        }
    }
}
