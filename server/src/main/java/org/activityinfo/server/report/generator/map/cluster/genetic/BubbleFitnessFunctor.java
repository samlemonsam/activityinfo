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
package org.activityinfo.server.report.generator.map.cluster.genetic;

import org.activityinfo.server.report.generator.map.CircleMath;
import org.activityinfo.server.report.generator.map.cluster.Cluster;

import java.util.List;

/**
 * Scores the fitness of a circle clustering solution, awarding points for more
 * bubbles, and penalizing solutions in which bubbles overlap.
 */
public class BubbleFitnessFunctor implements FitnessFunctor {

    @Override
    public double score(List<Cluster> clusters) {

        double score = 0;
        for (int i = 0; i != clusters.size(); ++i) {

            // award a score for the presence of this cluster
            // (all things equal, the more markers the better)
            score += CircleMath.area(clusters.get(i).getRadius());

            // penalize conflicts with other clusters
            for (int j = i + 1; j != clusters.size(); ++j) {

                score -= 4.0 * CircleMath.intersectionArea(clusters.get(i).getPoint(),
                        clusters.get(j).getPoint(),
                        clusters.get(i).getRadius(),
                        clusters.get(j).getRadius());
            }
        }
        return score;
    }
}
