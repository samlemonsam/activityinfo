package org.activityinfo.io.match.names;


import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.readLines;

public class NameMatchingIntegrationTest {

    public static final int COLUMN_WIDTH = 30;
    private LatinPlaceNameScorer scorer;
    private List<String> a;
    private List<String> b;

    private int falsePositives = 0;
    private int noMatch = 0;
    private int matches = 0;

    @Before
    public void setUp() {
        scorer = new LatinPlaceNameScorer();
    }

    @Test
    @Ignore
    public void test() throws IOException {
        test("lebanon.txt");
        test("philipines.txt");
        test("mali.txt");

        System.out.println("MATCHED:         " + matches);
        System.out.println("FALSE POSITIVES: " + falsePositives);
        System.out.println("NO MATCH:        " + noMatch);
    }

    private void test(String resource) throws IOException {
        loadTestSet(resource);
        printReport();

    }

    private void printReport() {

        for(int i=0;i!= a.size();++i) {
            String x = a.get(i);

            int bestMatch = findBestMatch(x, b);

            String correctMatch = b.get(i);
            double correctScore = scorer.score(x, b.get(i));

            if(bestMatch == -1) {
                noMatch++;

                System.out.println(Strings.padEnd(x, COLUMN_WIDTH, ' ' ) +
                        " FAILED TO MATCH " + formatScore(correctScore) + correctMatch);

            } else if(bestMatch == i || b.get(bestMatch).equals(b.get(i))) {
                matches++;

            } else {
                String matchedName = b.get(bestMatch);
                double matchedScore = scorer.score(x, b.get(bestMatch));

                System.out.println(Strings.padEnd(x, COLUMN_WIDTH, ' ' ) +
                        "         MATCHED " + formatScore(matchedScore) +
                        Strings.padEnd(matchedName, COLUMN_WIDTH, ' ') +
                        " INSTEAD OF " + formatScore(correctScore) + correctMatch);
                falsePositives++;
            }
        }
    }

    private void loadTestSet(String resourceName) throws IOException {
        // Each line in our test sets contain a pair of names that
        // refer to the same entity, but differ by spelling, transliteration method
        // or other messiness

        a = Lists.newArrayList();
        b = Lists.newArrayList();

        List<String> testSet = readLines(getResource(LatinPlaceNameScorer.class, resourceName), Charsets.UTF_8);
        for(String pair : testSet) {
            String[] columns = pair.split("\\|");
            if(columns.length != 2) {
                throw new IOException("Bad format for line: " + pair);
            }
            a.add(columns[0]);
            b.add(columns[1]);
        }
    }

    private String formatScore(double score) {
        return " (" + Strings.padStart(Integer.toString((int) (score*100d)), 3, ' ') + ") ";
    }

    private int findBestMatch(String x, List<String> b) {

        double bestScore = 0;
        int bestMatch = -1;

        for(int i=0;i!=b.size();++i) {
            String y = b.get(i);
            double score = scorer.score(x, y);

            if(score > bestScore) {
                bestMatch = i;
                bestScore = score;
            }
        }
        return bestMatch;
    }

    @Test
    public void nonMatches() {
        // These should not match at all
        // They are a separate set of entities
        // and it's obvious to the naked eye that they
        // are nowhere near close

        // it's important that our scorer
        // is able to see this

        List<String> regions = Lists.<String>newArrayList(
                "Region de Bruxelles-Capitale / Brussels Hoofdstede",
                "Vlaams Gewest",
                "Region wallonne");

        assertNoMatch("West-Vla", regions);
        assertNoMatch("Oost-Vla", regions);
        assertNoMatch("Antwerpen", regions);
        assertNoMatch("Brussel", regions);
        assertNoMatch("Limburg", regions);
        assertNoMatch("Vla-Bra", regions);
    }

    private void assertNoMatch(String province, List<String> regions) {
        LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();
        for(String region : regions) {
            double score = scorer.score(province, region);
            if(score > 0) {
                throw new AssertionError(province + " matched " + region + formatScore(score));
            }
        }
    }

}
