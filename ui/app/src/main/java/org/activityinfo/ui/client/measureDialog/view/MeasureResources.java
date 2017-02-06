package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.activityinfo.ui.client.measureDialog.model.MeasureSelectionModel;

public interface MeasureResources extends ClientBundle {

    public static final MeasureResources INSTANCE = GWT.create(MeasureResources.class);

    @Source("Measures.gss")
    Styles styles();

    interface Styles extends CssResource {

        String measureTypes();
    }
}
