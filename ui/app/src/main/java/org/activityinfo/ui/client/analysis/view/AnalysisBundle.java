package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface AnalysisBundle extends ClientBundle {

    AnalysisBundle INSTANCE = GWT.create(AnalysisBundle.class);

    @Source("Analysis.gss")
    Styles getStyles();

    interface Styles extends CssResource {

        String handle();

        String totalCell();

    }

}
