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
package org.activityinfo.io.match.names;

/**
 * Parses roman numerals
 *
 * @author <a href="http://rosettacode.org/wiki/Roman_numerals/Decode#Java_2">Rosetta Code</a>
 */
public class RomanNumerals {



    private static int decodeSingle(char letter) {
        switch(letter) {
            case 'M': return 1000;
            case 'D': return 500;
            case 'C': return 100;
            case 'L': return 50;
            case 'X': return 10;
            case 'V': return 5;
            case 'I': return 1;
            default: return 0;
        }
    }

    /**
     * Tries to parse a roman numeral from a string
     * @return the value of the roman numeral or -1 if the string is not a valid roman numeral
     */
    public static int tryDecodeRomanNumeral(char[] chars, int start, int end) {
        int result = 0;
        for(int i = start;i < end - 1;i++) {//loop over all but the last character
            //if this character has a lower value than the next character
            int digitValue = decodeSingle(chars[i]);
            if(digitValue == 0) {
                return -1;
            }
            if ( digitValue < decodeSingle(chars[i+1])) {
                //subtract it
                result -=  digitValue;
            } else {
                //add it
                result +=  digitValue;
            }
        }
        //decode the last character, which is always added
        int digitValue = decodeSingle(chars[end - 1]);
        if(digitValue == 0) {
            return -1;
        }
        result += digitValue;
        return result;
    }
}
