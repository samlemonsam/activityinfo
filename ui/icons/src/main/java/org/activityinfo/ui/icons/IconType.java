package org.activityinfo.ui.icons;

/**
 * Icon
 */
public enum IconType {
    BARS,
    HOME,
    LIST,
    EDIT,
    SEARCH,
    WRENCH,
    ANALYSIS;
    
    public String getIconStyle() {
        return "icon icon_" + name().toLowerCase();
    }
}
