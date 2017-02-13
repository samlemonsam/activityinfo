package org.activityinfo.ui.client.formulaDialog;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class FormulaElementCell extends AbstractCell<FormulaElement> {


    interface CellTemplates extends SafeHtmlTemplates {

        @Template("<span class=\"{0}\">{1}</span> {2}")
        SafeHtml cellWithCode(String formulaClass, String code, String label);
    }

    private static final CellTemplates TEMPLATES = GWT.create(CellTemplates.class);


    @Override
    public void render(Context context, FormulaElement formulaElement, SafeHtmlBuilder builder) {

        if(formulaElement.hasCode()) {
            builder.append(TEMPLATES.cellWithCode(FormulaResources.INSTANCE.styles().fieldTreeCode(),
                    formulaElement.getCode(),
                    formulaElement.getLabel()));
        } else {
            builder.appendEscaped(formulaElement.getLabel());
        }
    }
}
