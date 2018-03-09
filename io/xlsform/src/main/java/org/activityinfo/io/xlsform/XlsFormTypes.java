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
package org.activityinfo.io.xlsform;

/**
 * Types used in XLS Form
 */
public class XlsFormTypes {
    

    /**
     * 	Integer (i.e., whole number) input.
     */
    public static final String INTEGER = "integer";


    /**
     * 	Decimal input.
     */
    public static final String DECIMAL = "decimal";


    /**
     * Free text response
     */
    public static final String TEXT = "text";

    /**
     * Multi-line text (ActivityInfo addition, not standard xls)
     */
    public static final String MULTILINE_TEXT = "multiline text";
    
    /**
     * Multiple choice question; only one answer can be selected.
     */
    public static final String SELECT_ONE = "select_one";

    /**
     * Multiple choice question; only one answer can be selected.
     */
    public static final String SELECT_MULTIPLE = "select_multiple";

    /**
     * Display a note on the screen, takes no input.
     */
    public static final String NOTE = "note";

    /**
     * Collect a single GPS coordinates.
     */
    public static final String GEOPOINT = "geopoint";

    /**
     * Record a line of two or more GPS coordinates.
     */
    public static final String GEOTRACE = "geotrace";


    /**
     * Record a polygon of multiple GPS coordinates; the last point is the same as the first point.
     */
    public static final String GEOSHAPE = "geoshape";

    /**
     * Date input
     */
    public static final String DATE = "date";

    /**
     * Time input
     */
    public static final String TIME = "time";

    /**
     * Accepts a date and a time input.
     */
    public static final String DATE_TIME = "dateTime";

    /**
     * Take a picture
     */
    public static final String IMAGE = "image";

    /**
     * Take an audio recording
     */
    public static final String AUDIO = "audio";

    /**
     * Take a video recording
     */
    public static final String VIDEO = "video";

    /**
     * Scan a barcode, requires the barcode scanner app to be installed.
     */
    public static final String BARCODE = "barcode";

    /**
     * Perform a calculation
     */
    public static final String CALCULATE = "calculate";

    /**
     * Acknowledge prompt that sets value to “OK” if selected.
     */
    public static final String ACKNOWLEDGE = "acknowledge";
}
