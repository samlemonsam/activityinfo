package org.activityinfo.i18n.ar;

import com.google.gwt.i18n.client.impl.CldrImpl;

/**
 * Forces Left-to-Right text
 */
public class CldrImplLtr extends CldrImpl {

    @Override
    public boolean isRTL() {
        return false;
    }
}
