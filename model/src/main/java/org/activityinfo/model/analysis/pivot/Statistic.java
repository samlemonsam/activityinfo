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
package org.activityinfo.model.analysis.pivot;

import org.activityinfo.i18n.shared.I18N;

import java.util.ArrayList;
import java.util.List;

/**
 * The way in which a measure is aggregate
 */
public enum Statistic {
    COUNT(I18N.CONSTANTS.count()),
    COUNT_DISTINCT(I18N.CONSTANTS.countDistinct()),
    SUM(I18N.CONSTANTS.sum()),
    AVERAGE(I18N.CONSTANTS.average()),
    MEDIAN(I18N.CONSTANTS.median()),
    MIN(I18N.CONSTANTS.minimum()),
    MAX(I18N.CONSTANTS.maximum());

    private final String label;

    Statistic(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static List<String> labels() {
        List<String> labels = new ArrayList<>();
        for (Statistic statistic : values()) {
            labels.add(statistic.getLabel());
        }
        return labels;
    }
}
