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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;


public class InputMaskTest {


    @Test
    public void familyRegistration() {

        InputMask mask = new InputMask("\\1-00000000");

        assertThat("1-00000000", matches(mask));
        assertThat("1-23487538", matches(mask));
        assertThat("1-a0000000", not(matches(mask)));

        assertThat("1-234875383", not(matches(mask)));
        assertThat("0-234875383", not(matches(mask)));
        assertThat("-234875383", not(matches(mask)));
    }


    @Test
    public void familyRegistrationUsingRegex() {

        InputMask mask = new InputMask("\\1-00000000");

        assertThat("1-00000000", matchesUsingRegex(mask));
        assertThat("1-23487538", matchesUsingRegex(mask));
        assertThat("1-a0000000", not(matchesUsingRegex(mask)));

        assertThat("1-234875383", not(matchesUsingRegex(mask)));
        assertThat("0-234875383", not(matchesUsingRegex(mask)));
        assertThat("-234875383", not(matchesUsingRegex(mask)));
    }

    @Test
    public void regexWithSpecialCharacters() {

        InputMask phoneMask = new InputMask("(000) 000-0000");

        assertThat("(570) 724-3053", matchesUsingRegex(phoneMask));
    }

    @Test
    public void nullInputMask() {
        InputMask mask = new InputMask(null);

        assertThat("", matches(mask));
        assertThat("foobar", matches(mask));
    }

    @Test
    public void malformedEscapeTest() {
        InputMask mask = new InputMask("000\\");

        assertThat(mask.placeHolderText(), equalTo("000\\"));
        assertThat("000", not(matches(mask)));
        assertThat("123\\", matches(mask));
    }

    @Test
    public void emptyInputMask() {
        InputMask mask = new InputMask("");

        assertThat("", matches(mask));
        assertThat("foobar", matches(mask));
    }

    public Matcher<String> matches(final InputMask mask) {

        return new TypeSafeMatcher<String>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("string matching ").appendValue(mask);
            }

            @Override
            protected boolean matchesSafely(String s) {
                return mask.isValid(s);
            }
        };
    }

    public Matcher<String> matchesUsingRegex(final InputMask mask) {

        final String regex = mask.toXFormRegex();

        return new TypeSafeMatcher<String>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("string matching ").appendValue(mask).appendText(" ").appendValue(regex);
            }

            @Override
            protected boolean matchesSafely(String s) {
                return s.matches(regex);
            }
        };
    }
}