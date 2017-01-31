package org.activityinfo.ui.client.page.config.design.importer.wrapper;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

/**
 * @author yuriyz on 9/18/14.
 */
public class IndicatorKey implements WrapperKey {

    private String activityLabel;
    private String indicatorLabel;
    private String category;

    public IndicatorKey(String activityLabel, String indicatorLabel, String category) {
        this.activityLabel = activityLabel;
        this.indicatorLabel = indicatorLabel;
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndicatorKey that = (IndicatorKey) o;

        if (activityLabel != null ? !activityLabel.equals(that.activityLabel) : that.activityLabel != null)
            return false;
        if (category != null ? !category.equals(that.category) : that.category != null) return false;
        if (indicatorLabel != null ? !indicatorLabel.equals(that.indicatorLabel) : that.indicatorLabel != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = activityLabel != null ? activityLabel.hashCode() : 0;
        result = 31 * result + (indicatorLabel != null ? indicatorLabel.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IndicatorKey{" +
                "activityLabel='" + activityLabel + '\'' +
                ", indicatorLabel='" + indicatorLabel + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
