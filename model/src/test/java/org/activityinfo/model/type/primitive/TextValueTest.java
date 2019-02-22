package org.activityinfo.model.type.primitive;

import org.activityinfo.model.type.NarrativeValue;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class TextValueTest {

    @Test
    public void emptyNull() {
        assertThat(TextValue.valueOf(""), nullValue());
        assertThat(TextValue.valueOf("  "), nullValue());
        assertThat(TextValue.valueOf(null), nullValue());
        assertThat(TextValue.valueOf("  foo "), equalTo(TextValue.valueOf("foo")));
    }

    @Test
    public void emptyNullNarrative() {
        assertThat(NarrativeValue.valueOf(""), nullValue());
        assertThat(NarrativeValue.valueOf("  "), nullValue());
        assertThat(NarrativeValue.valueOf(null), nullValue());
        assertThat(NarrativeValue.valueOf("  foo "), equalTo(NarrativeValue.valueOf("foo")));
    }


}