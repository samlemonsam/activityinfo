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
package org.activityinfo.io.match.coord;

import org.activityinfo.i18n.shared.I18N;


public class CoordinateParser {


    /**
     * Notation used to format a geographic coordinate
     * 
     * @see <a href="http://en.wikipedia.org/wiki/Geographic_coordinate_conversion">Wikipedia</a>
     */
    public enum Notation {
        /**
         * Decimal degrees: 40.446° N or +40.446
         */
        DDd,

        /**
         * Degree minutes decimals. For example: 40° 26.767′ N
         */
        DMd,

        /**
         * Degree minutes seconds. For example: 40° 26′ 46″ N
         */
        DMS
    }

    /**
     * Provides number formatting & parsing. Extracted from the class to allow
     * for testing.
     */
    public interface NumberFormatter {

        /*
         * Formats a coordinate as Degree-decimal, with exactly six decimal places and always with a
         * sign prefix. Equivalent the formatting pattern "+0.000000;-0.000000"
         */
        String formatDDd(double value);

        String formatShortFraction(double value);

        String formatInt(double value);

        double parseDouble(String string);

        /**
         * @return the decimal separator for the current locale. In English, this would be "."
         */
        char getDecimalSeparator();

        /**
         * Return true if the given character {@code c} is a digit in the current locale
         */
        boolean isDigit(char c);
    }

    private static final double MINUTES_PER_DEGREE = 60;
    private static final double SECONDS_PER_DEGREE = 3600;
    private static final double MIN_MINUTES = 0;
    private static final double MAX_MINUTES = 60;
    private static final double MIN_SECONDS = 0;
    private static final double MAX_SECONDS = 60;

    private final String posHemiChars;
    private final String negHemiChars;

    private double minValue;
    private double maxValue;

    private final String noNumberErrorMessage = I18N.CONSTANTS.noNumber();
    private final String tooManyNumbersErrorMessage = I18N.CONSTANTS.tooManyNumbers();
    private final String noHemisphereMessage;
    private final String invalidSecondsMessage = I18N.CONSTANTS.invalidMinutes();
    private final String invalidMinutesMessage = I18N.CONSTANTS.invalidMinutes();


    private Notation notation = Notation.DMS;

    private boolean requireSign = true;


    private CoordinateAxis axis;
    private final NumberFormatter numberFormatter;


    public CoordinateParser(CoordinateAxis axis, NumberFormatter numberFormatter) {
        this.axis = axis;
        this.numberFormatter = numberFormatter;
        this.minValue = axis.getMinimumValue();
        this.maxValue = axis.getMaximumValue();
        this.posHemiChars = axis.getPositiveHemisphereCharacters();
        this.negHemiChars = axis.getNegativeHemisphereCharacters();
        switch (axis) {
            case LATITUDE:
                noHemisphereMessage = I18N.CONSTANTS.noHemisphere();
                break;
            default:
                noHemisphereMessage = I18N.CONSTANTS.noHemisphereLng();
                break;
        }
    }


    public void setRequireSign(boolean requireSign) {
        this.requireSign = requireSign;
    }

    /**
     * Parses the given string in either Degree Decimal, Degree Minutes Seconds, or
     * Degree Minutes Decimal notation.
     * 
     * <p>This parser's {@code notation} property is updated to the detected notation
     * after this method returns.</p>
     * 
     * @param value a coordinate value as text
     * @return the coordinate as a floating point number, or {@code null} if value is null or empty.
     * @throws CoordinateFormatException
     */
    public Double parse(String value) throws CoordinateFormatException {

        if (value == null || value.length() == 0) {
            return null;
        }

        StringBuffer[] numbers = new StringBuffer[]{
                new StringBuffer(),
                new StringBuffer(),
                new StringBuffer()};
        int numberIndex = 0;
        int i;

        /*
         * To assure correctness, we're going to insist that the user explicitly
         * enter the hemisphere (+/-/N/S).
         *
         * However, if the bounds dictate that the coordinate is in one
         * hemisphere, then we can assume the sign.
         */
        double sign = maybeInferSignFromBounds();
        
        CharIterator it = new CharIterator(value);

        while(it.hasNext()) {
            
            if(it.tryMatch('+') || it.tryMatch(posHemiChars)) {
                sign = +1;
            } else if(it.tryMatch('-') || it.tryMatch(negHemiChars)) {
                sign = -1;
            } else {
                char c = it.next();
                
                if (isNumberPart(c)) {
                    if (numberIndex > 2) {
                        throw new CoordinateFormatException(tooManyNumbersErrorMessage);
                    }
                    // Normalize decimal point to correct locale-specific decimal point
                    // so that we can parse it correctly
                    // For example, if a user in the French locale enters "32.14", this
                    // should actually be parsed as "32,14"
                    if(isDecimalSeparator(c)) {
                        c = numberFormatter.getDecimalSeparator();
                    }
                    numbers[numberIndex].append(c);

                } else if (numberIndex != 2 && numbers[numberIndex].length() > 0) {
                    // advance to the next token on anything else-- whitespace,
                    // symbols like ' " ° -- we won't insist that they are used
                    // in the right place
                    numberIndex++;
                }
            }
        }

        if (sign == 0) {
            if (requireSign) {
                throw new CoordinateFormatException(noHemisphereMessage);
            } else {
                sign = 1;
            }
        }

        return parseCoordinate(numbers) * sign;
    }

    /**
     * 
     * @return true if the hemisphere of this coordinate can be inferred from its bounds. 
     */
    private double maybeInferSignFromBounds() {
        double sign = 0;
        if (maxValue < 0) {
            sign = -1;
        } else if (minValue > 0) {
            sign = +1;
        }
        return sign;
    }

    private boolean isNumberPart(char c) {
        return isDecimalSeparator(c) || numberFormatter.isDigit(c);
    }

    private boolean isDecimalSeparator(char c) {
        // Be generous in what we accept while parsing. 
        // A thousands seperator will never be valid in a coordinate string,
        // so we can safely asusme that a comma is a european decimal separator for example
        return c == '.' || c == ',' || c == numberFormatter.getDecimalSeparator();
    }

    private double parseCoordinate(StringBuffer[] tokens)
            throws CoordinateFormatException {
        if (tokens[0].length() == 0) {
            throw new CoordinateFormatException(noNumberErrorMessage);
        }

        double coordinate = numberFormatter.parseDouble(tokens[0].toString());
        if(coordinate < axis.getMinimumValue() || coordinate > axis.getMaximumValue()) {
            throw new CoordinateFormatException(axis.getDegreesOutOfBoundsMessage());
        }

        notation = Notation.DDd;

        if (tokens[1].length() != 0) {
            double minutes = numberFormatter.parseDouble(tokens[1].toString());
            if (minutes < MIN_MINUTES || minutes > MAX_MINUTES) {
                throw new CoordinateFormatException(invalidMinutesMessage);
            }
            coordinate += minutes / MINUTES_PER_DEGREE;
            notation = Notation.DMd;

        }
        if (tokens[2].length() != 0) {
            double seconds = numberFormatter.parseDouble(tokens[2].toString());
            if (seconds < MIN_SECONDS || seconds > MAX_SECONDS) {
                throw new CoordinateFormatException(invalidSecondsMessage);
            }
            notation = Notation.DMS;
            coordinate += seconds / SECONDS_PER_DEGREE;
        }
        return coordinate;
    }



    /**
     * Formats the given coordinate {@code value} using Degree-Minute-decimal notation. 
     * For example: 40° 26.767′ N
     */
    public String formatAsDMd(double value) {

        double degrees = Math.floor(Math.abs(value));
        double minutes = (Math.abs(value) - degrees);

        StringBuilder sb = new StringBuilder();
        sb.append(numberFormatter.formatInt(Math.abs(degrees))).append("° ");
        sb.append(numberFormatter.formatShortFraction(minutes * MINUTES_PER_DEGREE)).append("' ");
        sb.append(hemisphereLabel(value));

        return sb.toString();
    }


    /**
     * Formats the given coordinate {@code value} using the decimal degree notation. 
     * For example: +40.767333
     */
    public String formatAsDDd(double coordinate) {
        return numberFormatter.formatDDd(coordinate);
    }

    /**
     * Formats the given coordinate {@code value} using Degree-Minute-Second notation.
     * For example: 40° 26′ 46″ N
     */
    public String formatAsDMS(double value) {
        double absv = Math.abs(value);

        double degrees = Math.floor(absv);
        double minutes = ((absv - degrees) * 60.0);
        double seconds = ((minutes - Math.floor(minutes)) * 60.0);
        minutes = Math.floor(minutes);

        StringBuilder sb = new StringBuilder();
        sb.append(numberFormatter.formatInt(Math.abs(degrees))).append("° ");
        sb.append(numberFormatter.formatInt(minutes)).append("' ");
        sb.append(numberFormatter.formatShortFraction(seconds)).append("\" ");
        sb.append(hemisphereLabel(value));

        return sb.toString();
    }

    /**
     * Formats the given coordinate {@code value} using this parser's current notation. 
     */
    public String format(double coordinate) {
        return format(notation, coordinate);
    }

    /**
     * Formats the given coordinate {@code value} using the given {@code notation}.
     */
    public String format(Notation notation, double coordinate) {
        switch (notation) {
            case DDd:
                return formatAsDDd(coordinate);
            case DMd:
                return formatAsDMd(coordinate);
            default:
            case DMS:
                return formatAsDMS(coordinate);
        }
    }

    private String hemisphereLabel(double value) {
        if (Math.signum(value) < 0) {
            return negHemiChars;
        } else {
            return posHemiChars;
        }
    }

    /**
     * 
     * @return the detected notation of the last coordinate parsed.
     */
    public Notation getNotation() {
        return notation;
    }

    /**
     * Sets the notation to be used for formatting. 
     * 
     * @param notation the notation to be used for formatting
     */
    public void setNotation(Notation notation) {
        this.notation = notation;
    }

    public double getMinValue() {
        return minValue;
    }

    /**
     * 
     * Sets the minimum coordinate value expected. This is used only for inferring a coordinate's sign while parsing.
     *
     */
    public void setMinValue(double minValue) {
        assert minValue >= axis.getMinimumValue();
        
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    /**
     *
     * Sets the maximum coordinate value expected. This is used only for inferring a coordinate's sign while parsing.
     *
     */
    public void setMaxValue(double maxValue) {
        assert maxValue <= axis.getMaximumValue();

        this.maxValue = maxValue;
    }
}
