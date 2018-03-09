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
package org.activityinfo.legacy.shared.reports.content;

import org.activityinfo.legacy.shared.reports.model.layers.BubbleMapLayer;

public class BubbleLayerLegend extends MapLayerLegend<BubbleMapLayer> {

    private double minValue = Double.MAX_VALUE;
    private double maxValue = -Double.MAX_VALUE;

    /**
     * @return the indicator value that corresponds to the minimum-sized bubble
     */
    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    /**
     * @return the indicator value that corresponds to the maximum-sized bubble
     */
    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public boolean hasValues() {
        return minValue <= maxValue;
    }
}
