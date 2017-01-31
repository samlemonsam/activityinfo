package org.activityinfo.ui.client.component.importDialog.validation.cells;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
* Created by alex on 2/19/14.
*/
public class ValidationCellTemplatesStub implements ValidationCellTemplates {

    @Override
    public SafeHtml deleted(String text) {
        return SafeHtmlUtils.fromTrustedString("<s>" + text + "</s>");
    }

    @Override
    public SafeHtml inserted(String text) {
        return SafeHtmlUtils.fromTrustedString("<i>" + text + "</i>");
    }

    @Override
    public SafeHtml invalid(String text) {
        return SafeHtmlUtils.fromTrustedString("<s>" + text + "</s>");
    }
}
