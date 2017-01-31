package org.activityinfo.ui.client.component.list;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiRenderer;

/**
 * Renders an instance with an icon and a label
 */
public interface ListItemRenderer extends UiRenderer {

    void render(SafeHtmlBuilder sb, String iconClass, String label, String description, String link);

}
