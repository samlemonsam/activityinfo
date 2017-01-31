package org.activityinfo.ui.client.component.importDialog.model.type.converter;

class CharIterator  {

    private final String string;
    private int index;
    
    public CharIterator(String string) {
        this.string = string;
    }

    public boolean hasNext() {
        return index < string.length();
    }

    public char next() {
        return string.charAt(index++);
    }

    /**
     * Tries to match the given string at the current position, advancing the iterator
     * and returning true if there is a match.
     */
    public boolean tryMatch(String toMatch) {
        if(hasNext() && string.substring(index).startsWith(toMatch)) {
            index += toMatch.length();
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Tries to match the given character at the current position, advancing the iterator
     * and returning true if there is a match.
     */
    public boolean tryMatch(char toMatch) {
        if(hasNext() && string.charAt(index) == toMatch) {
            index++;
            return true;
        } else {
            return false;
        }
    }

}
