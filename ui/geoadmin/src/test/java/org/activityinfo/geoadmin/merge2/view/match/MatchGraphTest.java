/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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