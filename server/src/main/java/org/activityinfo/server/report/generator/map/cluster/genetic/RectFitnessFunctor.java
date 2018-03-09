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

import org.activityinfo.legacy.shared.reports.model.Rectangle;
import org.activityinfo.server.report.generator.map.cluster.Cluster;

import java.util.List;

public class RectFitnessFunctor implements FitnessFunctor {

    private int area(Rectangle r) {
        return r.getWidth() * r.getHeight();
    }

    @Override
    public double score(List<Cluster> clusters) {
        double score = 0;
        for (int i = 0; i != clusters.size(); ++i) {

            // award a score for the presence of this cluster
            // (all things equal, the more markers the better)
            Rectangle iRect = clusters.get(i).getRectangle();
            score += area(iRect);

            // penalize conflicts with other clusters
            for (int j = i + 1; j != clusters.size(); ++j) {

                Rectangle jRect = clusters.get(j).getRectangle();

                if (iRect.intersects(jRect)) {
                    score -= 4.0 * area(iRect.intersection(clusters.get(j).getRectangle()));
                }
            }
        }
        return score;
    }
}
