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
package org.activityinfo.server.report.generator.map;

import org.activityinfo.server.report.generator.map.cluster.Cluster;

import java.util.List;

public class FixedRadiiCalculator implements RadiiCalculator {

    private final int radius;

    public FixedRadiiCalculator(int radius) {
        this.radius = radius;
    }

    @Override
    public void calculate(List<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            cluster.setRadius(radius);
        }
    }
}
