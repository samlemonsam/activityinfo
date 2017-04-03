package org.activityinfo.ui.client.component.importDialog.model.strategy;
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

import org.activityinfo.model.resource.ResourceId;

import java.util.List;

/**
 * @author yuriyz on 5/20/14.
 */
public class InstanceScoreSource {

    private List<ColumnAccessor> sources;
    private boolean[] roots;
    private List<ResourceId> referenceInstanceIds;
    private List<String[]> referenceValues;

    public InstanceScoreSource(List<ColumnAccessor> sources, boolean[] roots, List<ResourceId> referenceInstanceIds, List<String[]> referenceValues) {
        this.sources = sources;
        this.roots = roots;
        this.referenceInstanceIds = referenceInstanceIds;
        this.referenceValues = referenceValues;
    }

    public List<ColumnAccessor> getSources() {
        return sources;
    }

    public void setSources(List<ColumnAccessor> sources) {
        this.sources = sources;
    }

    public List<ResourceId> getReferenceInstanceIds() {
        return referenceInstanceIds;
    }

    public void setReferenceInstanceIds(List<ResourceId> referenceInstanceIds) {
        this.referenceInstanceIds = referenceInstanceIds;
    }

    public List<String[]> getReferenceValues() {
        return referenceValues;
    }

    public void setReferenceValues(List<String[]> referenceValues) {
        this.referenceValues = referenceValues;
    }

    public boolean isRoot(int i) {
        return roots[i];
    }

}
