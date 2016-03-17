package org.activityinfo.io.match.names;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LatinWordDistanceTest {

    private LatinWordDistance comparator;

    @Before
    public void setUp() throws Exception {
        comparator = new LatinWordDistance();
    }

    @Test
    public void vowelGroups() {
        double distance = comparator.distance("OUA", "OUE");
        assertThat(distance, equalTo(0.25));
    }

    @Test
    public void consonantSubstitution() {
        assertThat(comparator.distance("M", "N"), greaterThan(0d));
        assertThat(comparator.distance("T", "X"), infinite());
    }

    @Test
    public void doubleConsonant() {
        assertThat(comparator.distance("AMMA", "AMA"), lessThan(1d));
    }

    @Test
    public void yah() {
        assertThat(comparator.distance("SIYAA", "SEA"), not(infinite()));
    }

    @Test
    public void yat() {
        double distance = comparator.distance("AAYTIT", "YAT");
        System.out.println(distance);
    }


    @Test
    public void trailingVowel() {
        assertMatches("BOUAREJ", "BOUERIJE");
    }

    @Test
    public void hazerta() {
        assertMatches("HAZERTA", "HIZZERTA");
    }

    @Test
    @Ignore("not sure if this is valid")
    public void mtayriye() {
        assertMatches("MTAYRIYE", "MATARITE");
    }

    @Test
    public void ommittedVowel() {
        assertMatches("JBAB", "DJEBAB");
    }

    @Test
    public void msaitbe() {
        assertMatches("MSAITBE", "MOUSSAYTBEH");
    }

    @Test
    public void youmine() {

        // YOU-   MINE
        // YOU-  [N]INE
        // Y[A]- M[OU]NE

        double x = comparator.similarity("YOUMINE", "YAMOUNE");
        double y = comparator.similarity("YOUMINE", "YOUNINE");

        assertThat(y, greaterThan(x));
    }

    @Test
    public void louayze() {
        assertMatches("LOUAYZE", "LOUAIZE");
    }

    @Test
    public void yAsVowel() {
        assertThat(comparator.distance("AAYTANIT", "AITANIT"), not(infinite()));
    }

    void assertMatches(String x, String y) {
        double score = comparator.distance(x, y);
        double similarity = comparator.similarity(x, y);
        System.out.println(x + " <> " + y + ": d=" + score + ", s=" + similarity);
        assertThat(score, not(infinite()));
    }


    private Matcher<? super Double> infinite() {
        return new TypeSafeMatcher<Double>() {
            @Override
            protected boolean matchesSafely(Double item) {
                return Double.isInfinite(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("infinite");
            }
        };
    }


}
