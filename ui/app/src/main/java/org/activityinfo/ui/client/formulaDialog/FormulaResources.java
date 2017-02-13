package org.activityinfo.ui.client.formulaDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface FormulaResources extends ClientBundle {

    FormulaResources INSTANCE = GWT.create(FormulaResources.class);

    @Source("Formula.gss")
    Styles styles();

    interface Styles extends CssResource {

        String formulaDragHandle();

        String fieldTreeCode();
    }


}
