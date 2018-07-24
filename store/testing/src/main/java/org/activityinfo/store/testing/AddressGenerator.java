package org.activityinfo.store.testing;

import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.io.Resources;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeValue;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;

public class AddressGenerator implements Supplier<FieldValue> {

    private static List<String> STREETS;
    private static String[] SUFFIXES = new String[] { "St.", "Ave", "Lane", "Boulevard" };

    private Random random = new Random(35323L);

    public AddressGenerator() {
        if(STREETS == null) {
            URL streetsUrl = Resources.getResource(AddressGenerator.class, "streets.txt");
            try {
                STREETS = Resources.readLines(streetsUrl, Charsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Could not load streets", e);
            }
        }
    }

    @Override
    public FieldValue get() {
        int houseNumber = 12 + random.nextInt(1530);
        String street = STREETS.get(random.nextInt(STREETS.size()));
        String streetSuffix = SUFFIXES[random.nextInt(SUFFIXES.length)];
        String city = STREETS.get(random.nextInt(STREETS.size()));
        String state = randomLetter() + randomLetter();
        int zipCode = random.nextInt(99999);

        return NarrativeValue.valueOf(String.format("%d %s %s\n%s, %s %5d",
                houseNumber, street, streetSuffix, city, state, zipCode));
    }

    private String randomLetter() {
        int codepoint = 'A' + random.nextInt(25);
        return Character.valueOf((char)codepoint).toString();
    }
}
