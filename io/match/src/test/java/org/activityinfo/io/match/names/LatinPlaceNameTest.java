package org.activityinfo.io.match.names;


import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class LatinPlaceNameTest {

    private LatinPlaceName name;

    @Before
    public void setUp() {
        name = new LatinPlaceName();
    }

    @Test
    public void partsAreProperlyProcessed() {
        check("Aïn-Jraïne", asList("AIN", "JRAINE"));
        check("Zouk-El-Hosmieh et Dahr Ayasse", asList("ZOUK", "EL", "HOSMIEH", "ET", "DAHR", "AYASSE"));
        check("Zouk el Moukachérine", asList("ZOUK", "EL", "MOUKACHERINE"));
        check("Mazraat Louzid (Louayziyé)", asList("MAZRAAT", "LOUZID", "LOUAYZIYE"));
    }

    private void check(String input, List<String> expectedParts) {

        name.set(input);

        System.out.println(input + " => " + name);

        assertThat("partCount", name.partCount(), equalTo(expectedParts.size()));
        for(int i=0;i!=name.partCount();i++) {
            assertThat("part " + i, name.part(i), equalTo(expectedParts.get(i)));
        }
    }
}
