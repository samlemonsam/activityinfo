/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.component.formdesigner.drop;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerConstants;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerStyles;

/**
 * @author yuriyz on 7/8/14.
 */
public class Positioner implements IsWidget {

    private final HTML widget = new HTML();
    private boolean forbidded = false;

    public Positioner() {
        widget.addStyleName(FormDesignerStyles.INSTANCE.spacer());
        widget.setHeight(FormDesignerConstants.SOURCE_CONTROL_HEIGHT_PX + "px");
    }

    private void setStyle() {

        widget.removeStyleName(FormDesignerStyles.INSTANCE.spacerNormal());
        widget.removeStyleName(FormDesignerStyles.INSTANCE.spacerForbidden());

        if (forbidded) {
            widget.addStyleName(FormDesignerStyles.INSTANCE.spacerForbidden());
        } else {
            widget.addStyleName(FormDesignerStyles.INSTANCE.spacerNormal());
        }
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    public void setForbidded(boolean forbidded) {
        this.forbidded = forbidded;
        setStyle();
    }
}
