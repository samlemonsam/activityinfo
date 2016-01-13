package org.activityinfo.i18n.tools.po;


import com.google.common.base.Strings;
import org.activityinfo.i18n.tools.model.ResourceClassTerm;
import org.activityinfo.i18n.tools.model.TranslatedTerm;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class PoTermTranslation extends PoTerm implements TranslatedTerm {

    private PoDefinition definition;

    public PoTermTranslation() {
    }

    public PoTermTranslation(ResourceClassTerm term) {
        super(term);
        if(!Strings.isNullOrEmpty(term.getDefaultTranslation())) {
            this.definition = new PoDefinition();
            this.definition.setForm(term.getDefaultTranslation());
        }
    }

    public PoDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(PoDefinition definition) {
        this.definition = definition;
    }
    
    @JsonIgnore
    public boolean isTranslated() {
        return definition != null && !Strings.isNullOrEmpty(definition.getForm());
        
    }

    @JsonIgnore
    @Override
    public String getTranslation() {
        if(isTranslated()) {
            return definition.getForm();
        } else {
            return null;
        }
    }
}
