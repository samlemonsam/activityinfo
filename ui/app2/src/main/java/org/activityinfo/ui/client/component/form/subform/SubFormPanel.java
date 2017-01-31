package org.activityinfo.ui.client.component.form.subform;

import com.google.gwt.user.client.ui.IsWidget;
import org.activityinfo.ui.client.widget.DisplayWidget;
import org.activityinfo.ui.client.widget.LoadingPanel;

/**
 * Created by yuriyz on 8/5/2016.
 */
public interface SubFormPanel extends IsWidget, DisplayWidget<Void> {

    LoadingPanel<Void> getLoadingPanel();
}
