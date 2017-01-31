package org.activityinfo.ui.client.component.importDialog.validation.cells;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
* HTML templates used for Validation Grid cells
*/
public interface ValidationCellTemplates extends SafeHtmlTemplates {

    ValidationCellTemplates INSTANCE = GWT.create(ValidationCellTemplates.class);

    @Template("<del>{0}</del>")
    public SafeHtml deleted(String text);

    @Template("<ins>{0}</ins>")
    public SafeHtml inserted(String text);

    @SafeHtmlTemplates.Template("<s>{0}</s>")
    public SafeHtml invalid(String text);
}
