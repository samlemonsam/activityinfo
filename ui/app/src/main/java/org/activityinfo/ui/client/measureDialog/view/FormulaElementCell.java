package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class FormulaElementCell extends AbstractCell<FormulaElement> {

    @Override
    public void render(Context context, FormulaElement formulaElement, SafeHtmlBuilder builder) {
        if(formulaElement.hasCode()) {
            builder.appendHtmlConstant("<code>");
            builder.appendEscaped(formulaElement.getCode());
            builder.appendHtmlConstant("</code> ");
        }
        builder.appendEscaped(formulaElement.getLabel());
    }
}
