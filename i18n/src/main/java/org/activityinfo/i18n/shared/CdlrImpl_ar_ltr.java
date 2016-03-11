package org.activityinfo.i18n.shared;

import com.google.gwt.i18n.client.impl.CldrImpl;

/**
 * Forces Left-to-Right text.
 * 
 * <p>Exceptionally, we want to maintain an overall <em>document</em> orientation of Left-to-Right as this
 * is the (stated) preference of users.</p>
 */
public class CdlrImpl_ar_ltr extends CldrImpl {

    @Override
    public boolean isRTL() {
        return false;
    }
}
