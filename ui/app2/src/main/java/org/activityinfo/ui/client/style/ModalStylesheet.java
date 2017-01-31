package org.activityinfo.ui.client.style;

import com.bedatadriven.rebar.style.client.Source;
import com.bedatadriven.rebar.style.client.Stylesheet;
import com.google.gwt.core.client.GWT;

/**
 * Styles for Modal Dialogs
 */
@Source("modals.less")
public interface ModalStylesheet extends Stylesheet {

    public static final ModalStylesheet INSTANCE = GWT.create(ModalStylesheet.class);

}
