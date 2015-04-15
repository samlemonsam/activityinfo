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
