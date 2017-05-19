package org.activityinfo.server.job;

import org.joda.time.format.DateTimeFormat;

/**
 * Generates names for exported files
 */
public class Filenames {


    public static final String timestamp() {
        return DateTimeFormat.forPattern("YYYYmmdd_HHMMss").print(System.currentTimeMillis());
    }
}
