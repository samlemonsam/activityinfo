package org.activityinfo.ui.icons;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiConstructor;


public class Icon implements SafeHtml {
    
    private IconType type;

    @UiConstructor
    public Icon(IconType type) {
        this.type = type;
    }

    @Override
    public String asString() {
        return IconTemplates.INSTANCE.icon(type.name().toLowerCase()).asString();
    }
}
