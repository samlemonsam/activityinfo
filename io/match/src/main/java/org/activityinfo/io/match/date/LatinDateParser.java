package org.activityinfo.io.match.date;


import org.activityinfo.model.type.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Extracts local dates from strings using a series of heuristics
 */
public class LatinDateParser {

    private static final String SEPARATORS = "-/\\, \t.";
    
    public static final int NUM_COMPONENTS = 3;

    private final int[] components = new int[NUM_COMPONENTS];

    private int pivotYear = 50;

    
    /**
     * @return the two-digit pivot year.
     */
    public int getPivotYear() {
        return pivotYear;
    }

    /**
     * Sets the pivot year used to determine whether a two-digit year refers to 1900 or 2000. For example,
     * if the pivot year is set to 50, the default value, then:
     * <ul>
     *     <li>'19 Jan 99' will be parsed as 1999</li>
     *     <li>'15 May 01' will be parsed as 2015</li>
     *     <li>'30 May 51' will be parsed as 1951</li>
     *     <li>'30 May 49' will be parsed as 2049</li>
     * </ul> 
     * @param pivotYear two-digit year between 0 and 100
     */
    public void setPivotYear(int pivotYear) {
        assert pivotYear >= 0 && pivotYear <= 100;
        
        this.pivotYear = pivotYear;
    }

    @Nonnull
    public LocalDate parse(@Nonnull String string) {
        // basically we expect three components, in SOME format
        // that are separated by something normal

        int charIndex = 0;
        int component = 0;

        // if we stumble across the month name,
        // make sure we note this.
        int monthIndex = GregorianMonths.NOT_FOUND;

        while(component < NUM_COMPONENTS) {

            if(charIndex >= string.length()) {
                throw new IllegalArgumentException("Not enough components in '" + string + "', found: " +
                        Arrays.toString(components));
            }

            int start = charIndex;
            char c = string.charAt(start);

            if(Character.isDigit(c)) {

                // read all the digits in
                do {
                    charIndex++;
                }
                while(isDigit(string, charIndex));

                // parse as number
                components[component] = Integer.parseInt(string.substring(start, charIndex));

                // move on the next component
                component++;

            } else {

                // read until we hit a separator or digit
                do {
                    charIndex++;
                } while(isPartOfWord(string, charIndex));

                int monthNameLength = charIndex-start;
                if(monthNameLength > GregorianMonths.MIN_MONTH_NAME_LENGTH) {
                    int month = GregorianMonths.tryParseName(string.substring(start, charIndex));
                    if(month != GregorianMonths.NOT_FOUND) {
                        components[component] = month;
                        monthIndex = component;
                        component++;
                    }
                }
            }

            // advance through any separator chars
            while(isSeparator(string, charIndex)) {
                charIndex++;
            }
        }

        if(monthIndex != -1) {
            return parseUsingKnownMonthPosition(components, monthIndex);

        } else {
            // try to find the obvious year
            int yearIndex = findYearIndex(components, -1);
            if(yearIndex == -1) {
                // if we can't find a 4-digit year, we can only assume that it comes at
                // the end in some completely ambiguous form like 3/4/12
                yearIndex = 2;
            }

            return parseUsingKnownYearPosition(string, components, yearIndex);
        }
    }

    private LocalDate parseUsingKnownYearPosition(String string, int[] components, int yearIndex) {
        if(yearIndex == 0) {
            // usually YYYY-MM-dd
            if(monthAndDayMatch(components, 1, 2)) {
                return toDate(components, yearIndex, 1, 2);

            } else {
                return toDate(components, yearIndex, 2, 1);
            }

        } else if(yearIndex == 1) {
            // date in the middle?? 31-2000-12 ??  i don't think so...
            throw new IllegalArgumentException(string);

        } else {
            // the classic ambiguous 5/3/2007
            if(monthAndDayMatch(components, 1, 0)) {
                return toDate(components, yearIndex, 1, 0);
            } else {
                return toDate(components, yearIndex, 0, 1);
            }
        }
    }

    private boolean monthAndDayMatch(int[] components, int monthIndex, int dayIndex) {
        int month = components[monthIndex];
        if(month > 12) {
            return false;
        }
        if(components[dayIndex] > GregorianMonths.getMaxDaysInMonth(month)) {
            return false;
        }
        return true;
    }

    private LocalDate parseUsingKnownMonthPosition(int components[], int monthIndex) {
        int yearIndex = findYearIndex(components, monthIndex);
        if(yearIndex != -1) {
            int dayIndex = remainingIndex(monthIndex, yearIndex);
            return new LocalDate(components[yearIndex], components[monthIndex], components[dayIndex]);
        } else {
            // best guess
            if(monthIndex == 1) {
                // usually 31st May 12
                return toDate(components, 2, monthIndex, 1);
            } else {
                // who knows...
                return toDate(components, 1, monthIndex, 2);
            }
        }
    }

    private LocalDate toDate(int[] components, int yearIndex, int monthIndex, int dayIndex) {
        int year = components[yearIndex];
        if(isTwoDigits(year)) {
            year += inferCentury(year);
        }
        return new LocalDate(year, components[monthIndex], components[dayIndex]);
    }

    private int inferCentury(int year) {
        if(year < pivotYear) {
            return 2000;
        } else {
            return 1900;
        }
    }

    private boolean isTwoDigits(int year) {
        return year < 1000;
    }

    private int remainingIndex(int monthIndex, int yearIndex) {
        for(int i=0;i!=NUM_COMPONENTS;++i) {
            if(i != monthIndex && i != yearIndex) {
                return i;
            }
        }
        throw new IllegalStateException();
    }

    private int findYearIndex(int components[], int monthIndex) {
        int maxDaysInThisMonth = monthIndex == -1 ? 31 : GregorianMonths.getMaxDaysInMonth(components[monthIndex]);
        for(int i=0;i!=NUM_COMPONENTS;++i) {
            if(i != monthIndex) {
                if(components[i] > maxDaysInThisMonth) {
                    return i;
                }
            }
        }
        return GregorianMonths.NOT_FOUND;
    }

    private boolean isSeparator(String string, int charIndex) {
        if(charIndex < string.length()) {
            return SEPARATORS.indexOf(string.charAt(charIndex)) != -1;
        } else {
            return false;
        }
    }

    private boolean isDigit(String string, int charIndex) {
        if(charIndex < string.length()) {
            return Character.isDigit(string.charAt(charIndex));
        } else {
            return false;
        }
    }

    private boolean isPartOfWord(String string, int charIndex) {
        if(charIndex < string.length()) {
            return Character.isLetter(string.charAt(charIndex));
        } else {
            return false;
        }
    }
}

