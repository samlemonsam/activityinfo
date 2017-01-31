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
public class AttributeGroupKey implements WrapperKey {

    private String activityLabel;
    private String attributeGroupLabel;

    public AttributeGroupKey(String activityLabel, String attributeGroupLabel) {
        this.activityLabel = activityLabel;
        this.attributeGroupLabel = attributeGroupLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttributeGroupKey that = (AttributeGroupKey) o;

        if (activityLabel != null ? !activityLabel.equals(that.activityLabel) : that.activityLabel != null)
            return false;
        if (attributeGroupLabel != null ? !attributeGroupLabel.equals(that.attributeGroupLabel) : that.attributeGroupLabel != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = activityLabel != null ? activityLabel.hashCode() : 0;
        result = 31 * result + (attributeGroupLabel != null ? attributeGroupLabel.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AttributeGroupKey{" +
                "activityLabel='" + activityLabel + '\'' +
                ", attributeGroupLabel='" + attributeGroupLabel + '\'' +
                '}';
    }
}

