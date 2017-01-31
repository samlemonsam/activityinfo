package org.activityinfo.ui.client.style.table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.DataGrid;

/**
 * Our own Application resources for the GWT data grid
 */
public class DataGridResources implements DataGrid.Resources {

    public static final DataGridResources INSTANCE = new DataGridResources();

    private static final DataGrid.Resources BASE_RESOURCES = GWT.create(DataGrid.Resources.class);

    private static final DataGridStylesheet STYLE_SHEET = GWT.create(DataGridStylesheet.class);


    @Override
    public ImageResource dataGridLoading() {
        return BASE_RESOURCES.dataGridLoading();
    }

    @Override
    public ImageResource dataGridSortAscending() {
        return BASE_RESOURCES.dataGridSortDescending();
    }

    @Override
    public ImageResource dataGridSortDescending() {
        return BASE_RESOURCES.dataGridSortDescending();
    }

    @Override
    public DataGrid.Style dataGridStyle() {
        return STYLE_SHEET;
    }
}
