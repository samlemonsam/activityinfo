package org.activityinfo.model.query;

import org.junit.Test;

import java.io.*;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class StringArrayColumnViewTest {

    @Test
    public void serializeTest() throws IOException, ClassNotFoundException {
        checkRoundTrip();
        checkRoundTrip("Hello World", "BAHÇELİEVLER MAH. 1");
        checkRoundTrip("Hello World", "BAHÇELİEVLER MAH. 1", null);
        checkRoundTrip(null, null, null);
    }

    private void checkRoundTrip(String... values) throws IOException, ClassNotFoundException {

        StringArrayColumnView view = new StringArrayColumnView(values);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(baos);

        output.writeObject(view);

        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

        StringArrayColumnView review = (StringArrayColumnView) input.readObject();

        assertThat(review.numRows(), equalTo(view.numRows()));

        for (int i = 0; i < values.length; i++) {
            assertTrue("row " + i, Objects.equals(review.getString(i), view.getString(i)));
        }
    }
}