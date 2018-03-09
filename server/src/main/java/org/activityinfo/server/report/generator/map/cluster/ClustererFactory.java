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
package org.activityinfo.server.report.generator.map.cluster;

import org.activityinfo.legacy.shared.reports.model.clustering.AdministrativeLevelClustering;
import org.activityinfo.legacy.shared.reports.model.clustering.AutomaticClustering;
import org.activityinfo.legacy.shared.reports.model.clustering.Clustering;
import org.activityinfo.legacy.shared.reports.model.clustering.NoClustering;
import org.activityinfo.server.report.generator.map.RadiiCalculator;
import org.activityinfo.server.report.generator.map.cluster.genetic.MarkerGraph.IntersectionCalculator;

public final class ClustererFactory {

    private ClustererFactory() {
    }

    public static Clusterer fromClustering(Clustering clustering,
                                           RadiiCalculator radiiCalculator,
                                           IntersectionCalculator intersectionCalculator) {
        if (clustering instanceof NoClustering) {
            return new NullClusterer(radiiCalculator);
        } else if (clustering instanceof AutomaticClustering) {
            return new GeneticClusterer(radiiCalculator, intersectionCalculator);
        } else if (clustering instanceof AdministrativeLevelClustering) {
            return new AdminLevelClusterer((AdministrativeLevelClustering) clustering, radiiCalculator);
        }

        return new NullClusterer(radiiCalculator);
    }
}
