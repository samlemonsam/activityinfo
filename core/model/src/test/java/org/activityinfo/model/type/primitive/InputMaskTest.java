package org.activityinfo.model.type.primitive;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;


public class InputMaskTest {


    private InputMask mask;

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
        this.mask = mask;

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
}