package org.activityinfo.analysis.pivot;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.*;

public class PivotTableWriterTest {

    private final Writer writer = new StringWriter();

    @Test
    public void illegalCharacterTest() throws Exception {
        String[] data = new String[] { "1", "2", "Address Line 1, Address Line 2", "Double quote \" me and return a new line \n\r"};
        String expectedResult = PivotTableWriter.BYTEORDER_MARK + "1,2,\"Address Line 1, Address Line 2\",\"Double quote \" me and return a new line \n\r\",";
        try(PivotTableWriter pivotWriter = new PivotTableWriter(writer)) {
            for (int i=0; i<data.length; i++) {
                pivotWriter.writeDelimited(data[i]);
            }
        }
        String result = writer.toString();
        assertThat(result, Matchers.equalTo(expectedResult));
    }

}