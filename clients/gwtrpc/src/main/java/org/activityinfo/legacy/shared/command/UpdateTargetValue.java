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
package org.activityinfo.legacy.shared.command;

import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.validation.Required;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

public class UpdateTargetValue implements MutatingCommand<VoidResult> {

    private int targetId;
    private int indicatorId;
    private Map<String, Double> changes;

    public UpdateTargetValue() {
    }

    public UpdateTargetValue(int targetId, int indicatorId, Map<String, Double> changes) {
        this.targetId = targetId;
        this.indicatorId = indicatorId;
        this.changes = changes;
    }

    @Required
    @JsonProperty
    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    @Required
    @JsonProperty
    public int getIndicatorId() {
        return indicatorId;
    }

    public void setIndicatorId(int indicatorId) {
        this.indicatorId = indicatorId;
    }

    @JsonProperty
    public Map<String, Double> getChanges() {
        return changes;
    }

    public void setChanges(Map<String, Double> changes) {
        this.changes = changes;
    }

}
