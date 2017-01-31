package org.activityinfo.ui.client.component.report.view;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 * Drill down row, which is a combination of site and reporting period
 */
@SuppressWarnings("GwtInconsistentSerializableClass")
public class DrillDownRow extends BaseModelData {
    private final int siteId;

    public DrillDownRow(int siteId) {
        this.siteId = siteId;
    }
}
