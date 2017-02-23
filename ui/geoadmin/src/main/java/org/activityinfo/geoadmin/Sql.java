package org.activityinfo.geoadmin;

import org.apache.commons.lang.StringEscapeUtils;

public class Sql {
    public static String quote(String code) {
        if(code == null) {
            return "null";
        } else {
            return "\'" + StringEscapeUtils.escapeSql(code) + "\'";
        }
    }
}
