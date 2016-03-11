package org.activityinfo.i18n.shared;

import com.google.gwt.i18n.client.impl.cldr.DateTimeFormatInfoImpl_ar;

/**
 * Fixes for the Arabic date time formats
 * 
 * <p>The standard CDLR formatting strings assume an overall RTL document orientation,
 * while we're embedding the arabic version in an LTR document.</p>
 * 
 * <p>To display some of the date formats correctly, this classes removes bidi control characters
 * that were screwing things up in an LTR document orientation..</p>
 * 
 * @see <a href="http://www.iamcal.com/understanding-bidirectional-text/">Understanding Bidirectional 
 * (BIDI) Text in Unicode</a>
 */
public class DateTimeFormatInfoImpl_ar_ltr extends DateTimeFormatInfoImpl_ar {



    @Override
    public String dateFormatFull() {
        return "EEEE\u060c d MMMM\u060c y";
    }

    @Override
    public String dateFormatLong() {
        // <start rtl>d MMMM<arabic comma> y<pop direction>
        return "\u202Bd MMMM\u060c y\u202C";
    }

    @Override
    public String dateFormatMedium() {
        return "dd/MM/yyyy";
    }

    @Override
    public String dateFormatShort() {
        return "d/M/yyyy";
    }

    

}
