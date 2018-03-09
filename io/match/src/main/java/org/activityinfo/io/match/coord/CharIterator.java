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
