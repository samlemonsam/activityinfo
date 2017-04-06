package org.activityinfo.model.type.primitive;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

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
                            throw new InvalidInputMaskException(mask, i - 1);
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


    @Override
    public String toString() {
        return "InputMask{" + mask + '}';
    }
}
