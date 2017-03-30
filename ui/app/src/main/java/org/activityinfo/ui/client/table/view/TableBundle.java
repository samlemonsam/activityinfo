package org.activityinfo.ui.client.table.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * CSS and assets for table view
 */
public interface TableBundle extends ClientBundle {

    TableBundle INSTANCE = GWT.create(TableBundle.class);

    @Source("Table.gss")
    Style style();

    interface Style extends CssResource {
        String detailPane();
    }

}
