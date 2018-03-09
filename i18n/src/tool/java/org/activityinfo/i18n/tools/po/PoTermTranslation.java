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
