package org.activityinfo.geoadmin.merge2.view.match;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class MatchGraphTest {

    
    @Test
    public void unambiguousBestMatches() {
        TestScoreMatrix matrix = new TestScoreMatrix(2);
        int mwetsiSource = matrix.addSource("Mwetshi", "North Kivu");
        int kanagaSource = matrix.addSource("Kananga", "Kasai Occidental");

        int mwetshiTarget = matrix.addTarget("Mwetshi", "North Kivu");
        int kanagaTarget = matrix.addTarget("Kananga", "Kasai Occidental");

        MatchGraph graph = new MatchGraph(matrix).build();
        assertThat(graph.getBestMatchForTarget(mwetshiTarget), equalTo(mwetsiSource));
        assertThat(graph.getBestMatchForSource(mwetsiSource), equalTo(mwetshiTarget));
        
        assertThat(graph.getBestMatchForTarget(kanagaTarget), equalTo(kanagaSource));
        assertThat(graph.getBestMatchForSource(kanagaSource), equalTo(kanagaTarget));
    }

    @Test
    public void kamango() throws IOException {

        TestScoreMatrix matrix = new TestScoreMatrix(3);
        // Kamango has no real match in the target, has a low similiarity with "kanaga",
        // which will be picked as "Kamango"'s best match.
        // However, this match should be rejected because "kanaga" in the target
        // is also matched to "kanaga" in the source, and is superior to "kamango" on all
        // dimensions (both name and province name)
        int kamangoSource = matrix.addSource("Kamango", null, "Nord Kivu");
        int kanagaSource =  matrix.addSource("Kananga", null, "Kasai Occidental");
        int mwetsiSource =  matrix.addSource("Mwetshi", null, "Kasai Occidental");
        
        int kanagaTarget =  matrix.addTarget("Kananga", null, "Kasai Occidental");
        
        MatchGraph graph = new MatchGraph(matrix);
        graph.build();

        assertThat(graph.getBestMatchForSource(kanagaSource), equalTo(kanagaTarget));
        assertThat(graph.getBestMatchForSource(kamangoSource), equalTo(-1));
        assertThat(graph.getBestMatchForSource(mwetsiSource), equalTo(-1));

        assertThat(graph.getBestMatchForTarget(kanagaTarget), equalTo(kanagaSource));
    }
}