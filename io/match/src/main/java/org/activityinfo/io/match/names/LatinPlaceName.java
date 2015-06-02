package org.activityinfo.io.match.names;

/**
 * Contains a fully normalized and parsed latin names name as
 * an array of characters with pointers to the beginnings of parts.
 */
class LatinPlaceName {

    public static final int MAX_PARTS = 15;

    public static final int MAX_LENGTH = 100;

    private final int LETTER_CLASS = 0;
    private final int DIGIT_CLASS = 1;
    private final int OTHER = -1;

    public static final String APOSTROPHE = "'";


    /**
     * Normalized characters, without any separating spaces
     */
    char[] chars = new char[MAX_LENGTH];

    /**
     * The offsets of the parts
     */
    private int[] partOffsets = new int[MAX_PARTS+1];

    /**
     * The number of chars in the normalized string
     */
    private int numChars;

    /**
     * The number of parts found
     */
    private int numParts;


    private final LatinCharacterNormalizer characterNormalizer;

    LatinPlaceName() {
        this.characterNormalizer = LatinCharacterNormalizer.get();
    }

    public void set(String input) {
        numParts = 0;
        numChars = 0;

        if(input != null) {

            int currentClass = OTHER;

            for (int i = 0; i != input.length(); ++i) {
                String ch = characterNormalizer.normalizeCharacter(input.substring(i, i + 1));

                // For words like N'Goutjina we just drop the apostrophe
                if (ch.equals(APOSTROPHE) && currentClass == LETTER_CLASS) {
                    continue;
                }

                int characterClass = classify(ch);
                if (isBreak(currentClass, characterClass)) {
                    partOffsets[numParts] = numChars;
                    numParts++;
                }

                currentClass = characterClass;

                if (currentClass != OTHER) {
                    for (int j = 0; j != ch.length(); ++j) {
                        chars[numChars++] = ch.charAt(j);
                    }
                }
            }
        }

        // add a final offset for convenience
        partOffsets[numParts] = numChars;
    }

    public int partCount() {
        return numParts;
    }

    public boolean isEmpty() {
        return numChars == 0;
    }

    /**
     * @return the number of characters in part {@code partIndex}
     */
    public int charCount(int partIndex) {
        return partOffsets[partIndex+1] - partOffsets[partIndex];
    }

    /**
     * @return the total number of alphanumeric characters in this names name following normalization
     */
    public int charCount() {
        return numChars;
    }


    /**
     * @return the starting index of part {@code partIndex}
     */
    public int partStart(int partIndex) {
        return partOffsets[partIndex];
    }

    /**
     *
     * @return true if the transition from character class {@code a} to character
     * class {@code b} should be considered a break between components
     */
    private boolean isBreak(int fromClass, int toClass) {

        if(fromClass == toClass) {
            return false;
        }

        // OTHER => [LETTER, DIGIT] is always a break
        // (don't count [LETTER, DIGIT] -> OTHER as this will double
        //  count parts)
        if(fromClass == OTHER) {
            return true;
        }

        // We consider LETTER => DIGIT a break, for example:
        // "Commune2" should be understood as [commune, 2]
        if(fromClass == LETTER_CLASS && toClass == DIGIT_CLASS) {
            return true;
        }

        // But going from DIGIT => LETTER is probably not because
        // it could be a suffix like "2b" or an ordinal indicator
        // like "1st" or "2eme"
        return false;
    }

    /**
     * Classifies a character as {@code LETTER}, {@code DIGIT}, or {@code OTHER}
     */
    private int classify(String input) {
        char ch = input.charAt(0);
        if(isDigit(ch)) {
            return DIGIT_CLASS;
        } else if(ch >= 'A' && ch <= 'Z') {
            return LETTER_CLASS;
        } else {
            return OTHER;
        }
    }

    private boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    public String part(int index) {
        return new String(chars, partOffsets[index], charCount(index));
    }

    public char charAt(int partIndex, int charIndex) {
        return chars[ partOffsets[partIndex] + charIndex ];
    }

    public boolean isPartNumeric(int partIndex) {
        return Character.isDigit(chars[partStart(partIndex)]);
    }

    public int parsePartAsInteger(int index) {
        int numberStart = partStart(index);
        int numberEnd = numberStart;
        int partEnd = partStart(index+1);
        while(numberEnd < partEnd && isDigit(chars[numberEnd])) {
            numberEnd++;
        }
        return Integer.parseInt(new String(chars, numberStart, numberEnd - numberStart));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int partIndex = 0; partIndex!=numParts;++partIndex) {
            if(partIndex > 0) {
                sb.append(", ");
            }
            sb.append(part(partIndex));
        }
        sb.append("]");
        return sb.toString();
    }

    public int tryParsePartAsRomanNumeral(int index) {
        return RomanNumerals.tryDecodeRomanNumeral(chars, partOffsets[index], partOffsets[index+1]);
    }
}
