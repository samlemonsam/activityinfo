package org.activityinfo.io.match.names;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


public class LatinPlaceNameScorerTest {

    public static final double MINIMUM_SCORE = 0.25;

    @Test
    public void permutations() {

        String y[] = new String[] { "COMMUNE", "DE", "KAYES" };

        List<String> permutations = Lists.newArrayList();

        int a[] = PartialPermutations.identity(y.length);

        do {
            String permuted = y[a[0]] + " " + y[a[1]];
            System.out.println(permuted);
            permutations.add(permuted);
        }
        while(PartialPermutations.next(a, y.length, 2));

        assertThat(permutations, contains(
                "COMMUNE DE",
                "COMMUNE KAYES",
                "DE COMMUNE",
                "DE KAYES",
                "KAYES COMMUNE",
                "KAYES DE"));
    }

    @Test
    public void bestPermutations() {
        LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();
        scorer.init("COMMUNE DE KAYES", "COMMUNE KAYES");

        double bestScore = scorer.findBestPermutationScore();

        // the score ~ number of characters matched
        double numerator = "COMMUNEKAYES".length();
        double denominator = "COMMUNEDEKAYES".length();

        assertThat(bestScore, closeTo(numerator / denominator, 0.01));
    }

    @Test
    public void denominatorIncludesUnmatchedParts() {
        LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();
        double score = scorer.score("FINKOLO SIKASSO", "SIKASSO COMMUNE");

        double numerator = "SIKASSO".length();
        double denominator = "SIKASSO".length() + "FINKOLO".length() + "COMMUNE".length();
        assertThat(score, closeTo(numerator / denominator, 0.01));
    }

    @Test
    public void denominatorIncludesExtraParts() {
        LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();
        double score = scorer.score("BENKADI-FOUNIA", "BENKADI");

        double numerator = "BENKADI".length();
        double denominator = "BENKADI".length() + "FOUNIA".length();

        assertThat(score, closeTo(numerator / denominator, 0.01));
    }

    @Test
    public void somalia() {
        LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();

        double score = scorer.score("Jubbada Hoose (Lower Juba)", "Lower Juba");
        System.out.println(score);
        assertThat(score, greaterThan(MINIMUM_SCORE));
    }
//
//    @Test
//    public void profileNames() throws IOException {
//        final LatinPlaceName name = new LatinPlaceName();
//        final StringBuilder nucleus = new StringBuilder();
//
//        final PrintWriter nuclei = new PrintWriter("/home/alex/data/names/nuclei.txt");
//        final PrintWriter normalized = new PrintWriter("/home/alex/data/names/normalized.txt");
//
//
//        Files.readLines(new File("/home/alex/data/names.txt"), Charsets.UTF_8, new LineProcessor<Object>() {
//            @Override
//            public boolean processLine(String s) throws IOException {
//                name.set(s);
//
//                normalized.println(name.toString());
//
//                for(int i=0;i!=name.partCount();++i) {
//                    int start = name.partStart(i);
//                    int end = name.partStart(i+1);
//                    for(int j=start;j<end;j++) {
//
//                        if(isVowelChar(name.chars[j])) {
//                            nucleus.setLength(0);
//                            nucleus.append(name.chars[j]);
//                            while(j < end && isVowelChar(name.chars[j])) {
//                                nucleus.append(name.chars[j]);
//                                j++;
//                            }
//                            nuclei.println(nucleus.toString());
//                        }
//                    }
//                }
//                return true;
//
//            }
//
//            @Override
//            public Object getResult() {
//                return null;
//            }
//        });
//    }

}
