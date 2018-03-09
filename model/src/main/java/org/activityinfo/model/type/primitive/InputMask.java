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
package org.activityinfo.model.type.primitive;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Specifies an input mask that constrains text input.
 *
 * <p>
 *
 * </p>
 */
public class InputMask {


    public static final char OPTIONAL_DIGIT = '9';

    /**
     * User can enter a digit, space, plus or minus sign. If skipped AI enters a blank space.
     */
    public static final char NUMBER_SYMBOL = '#';

    public static final char REQUIRED_LETTER = 'L';

    public static final char OPTIONAL_LETTER = '?';

    public static final char REQUIRED_ALPHANUM = 'A';

    public static final char OPTIONAL_ALPHANUM = 'a';

    /**
     * Character immediately following will be displayed literally
     */
    public static final char ESCAPE = '\\';



    public static final class Input {
        private CharSequence sequence;
        private int offset = 0;

        public Input(CharSequence sequence) {
            this.sequence = sequence;
        }

        public boolean hasNext() {
            return offset < sequence.length();
        }

        public char next() {
            return sequence.charAt(offset++);
        }
    }

    public abstract class Atom {
        public abstract boolean validate(Input input, StringBuilder output);

        public abstract void appendPlaceHolder(StringBuilder sb);

        public abstract void appendRegex(StringBuilder regex);

        public abstract void appendRandom(Random random, StringBuilder sb);
    }

    public class RequiredDigit extends Atom {

        public static final char CHAR = '0';

        @Override
        public boolean validate(Input input, StringBuilder output) {
            if(!input.hasNext()) {
                return false;
            }
            char next = input.next();
            if(!Character.isDigit(next)) {
                return false;
            }
            output.append(next);
            return true;
        }

        @Override
        public void appendPlaceHolder(StringBuilder sb) {
            sb.append(CHAR);
        }

        @Override
        public void appendRegex(StringBuilder regex) {
            regex.append("[0-9]");
        }

        @Override
        public void appendRandom(Random random, StringBuilder sb) {
            sb.append((char)('0' + random.nextInt(10)));
        }
    }

    public class RequiredLetter extends Atom {

        public static final char CHAR = 'L';

        @Override
        public boolean validate(Input input, StringBuilder output) {
            if(!input.hasNext()) {
                return false;
            }
            char next = input.next();
            if(!Character.isLetter(next)) {
                return false;
            }
            output.append(next);
            return true;
        }

        @Override
        public void appendPlaceHolder(StringBuilder sb) {
            sb.append(CHAR);
        }

        @Override
        public void appendRegex(StringBuilder regex) {
            regex.append("[A-Za-z]");
        }

        @Override
        public void appendRandom(Random random, StringBuilder sb) {
            if(random.nextInt(2) == 0) {
                sb.append((char)('A' + random.nextInt(26)));
            } else {
                sb.append((char)('a' + random.nextInt(26)));
            }
        }
    }

    public class RequiredLetterOrDigit extends Atom {

        public static final char CHAR = 'A';

        @Override
        public boolean validate(Input input, StringBuilder output) {
            if(!input.hasNext()) {
                return false;
            }
            char next = input.next();
            if(!(Character.isLetter(next) || Character.isDigit(next)) ) {
                return false;
            }
            output.append(next);
            return true;
        }

        @Override
        public void appendPlaceHolder(StringBuilder sb) {
            sb.append(CHAR);
        }

        @Override
        public void appendRegex(StringBuilder regex) {
            regex.append("[A-Za-z0-9]");
        }

        @Override
        public void appendRandom(Random random, StringBuilder sb) {
            switch (random.nextInt(3)) {
                case 0:
                    sb.append((char)('0' + random.nextInt(10)));
                    break;
                case 1:
                    sb.append((char)('A' + random.nextInt(26)));
                    break;
                case 2:
                    sb.append((char)('a' + random.nextInt(26)));
                    break;
            }
        }
    }

    public class Literal extends Atom {

        private char expectedChar;

        public Literal(char expectedChar) {
            this.expectedChar = expectedChar;
        }

        @Override
        public boolean validate(Input input, StringBuilder output) {
            if(!input.hasNext()) {
                return false;
            }
            char inputChar = input.next();
            if(expectedChar != inputChar) {
                return false;
            }
            output.append(expectedChar);
            return true;
        }

        @Override
        public void appendPlaceHolder(StringBuilder sb) {
            sb.append(expectedChar);
        }

        @Override
        public void appendRegex(StringBuilder regex) {
            if(isRegexCharacter()) {
                regex.append('\\');
            }
            regex.append(expectedChar);
        }

        @Override
        public void appendRandom(Random random, StringBuilder sb) {
            sb.append(expectedChar);
        }

        private boolean isRegexCharacter() {
            switch (expectedChar) {
                case '.':
                case '+':
                case '[':
                case ']':
                case '(':
                case ')':
                case '\\':
                    return true;
                default:
                    return false;
            }
        }
    }

    private final String mask;
    private final List<Atom> atoms = new ArrayList<>();
    private final boolean empty;

    public InputMask(String mask) {
        this.mask = Strings.nullToEmpty(mask);
        this.empty = this.mask.isEmpty();
        if(!this.empty) {
            int i = 0;
            while (i < mask.length()) {
                char c = mask.charAt(i++);
                switch (c) {
                    case RequiredDigit.CHAR:
                        atoms.add(new RequiredDigit());
                        break;

                    case RequiredLetter.CHAR:
                        atoms.add(new RequiredLetter());
                        break;

                    case RequiredLetterOrDigit.CHAR:
                        atoms.add(new RequiredLetterOrDigit());
                        break;

                    case ESCAPE:
                        if (i < mask.length()) {
                            atoms.add(new Literal(mask.charAt(i++)));
                        } else {
                            // be lenient in what we accept
                            atoms.add(new Literal('\\'));
                        }
                        break;

                    default:
                        atoms.add(new Literal(c));
                }
            }
        }
    }

    public boolean isValid(String input) {
        if(empty) {
            return true;
        }
        Input it = new Input(input);
        StringBuilder output = new StringBuilder();
        for (Atom atom : atoms) {
            if(!atom.validate(it, output)) {
                return false;
            }
        }
        if(it.hasNext()) {
            return false;
        }
        return true;
    }


    public String initialInput() {
        StringBuilder sb = new StringBuilder();
        for (Atom atom : atoms) {
            if(atom instanceof Literal) {
                sb.append(((Literal) atom).expectedChar);
            } else {
                break;
            }
        }
        return sb.toString();
    }

    public String placeHolderText() {
        StringBuilder sb = new StringBuilder();
        for (Atom atom : atoms) {
            atom.appendPlaceHolder(sb);
        }
        return sb.toString();
    }

    public List<Atom> getAtoms() {
        return atoms;
    }

    /**
     *
     * @return an XForm (ODK) compatible regular expression to validate this input mask.
     */
    public String toXFormRegex() {
        StringBuilder regex = new StringBuilder();
        for (Atom atom : atoms) {
            atom.appendRegex(regex);
        }
        return regex.toString();
    }


    @Override
    public String toString() {
        return "InputMask{" + mask + '}';
    }
}
