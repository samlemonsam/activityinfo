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
import org.activityinfo.model.type.time.Month;

import java.io.Serializable;
import java.util.ArrayList;

public class UpdateMonthlyReports implements MutatingCommand<VoidResult> {

    public static class Change implements Serializable {

        public enum Type {
            CREATE,
            EDIT,
            DELETE;
        }

        private Month month;
        private int indicatorId;
        private Double value;
        private Type changeType;

        public Change() {

        }

        public Change(int indicatorId, Month month, Double value) {
            this.indicatorId = indicatorId;
            this.setMonth(month);
            this.setValue(value);
            this.changeType = Type.EDIT;
        }

        public Change(int indicatorId, Month month, Double value, Type changeType) {
            this.indicatorId = indicatorId;
            this.setMonth(month);
            this.setValue(value);
            this.changeType = changeType;
        }

        public void setMonth(Month month) {
            this.month = month;
        }

        public Month getMonth() {
            return month;
        }

        public void setIndicatorId(int indicatorId) {
            this.indicatorId = indicatorId;
        }

        public int getIndicatorId() {
            return indicatorId;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public Double getValue() {
            return value;
        }

        public void setChangeType(Type changeType) {
            this.changeType = changeType;
        }

        public Type getChangeType() {
            return changeType;
        }
    }

    private int siteId;
    private ArrayList<Change> changes;

    public UpdateMonthlyReports() {
    }

    public UpdateMonthlyReports(int siteId, ArrayList<Change> changes) {
        this.siteId = siteId;
        this.changes = changes;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public ArrayList<Change> getChanges() {
        return changes;
    }

    public void setChanges(ArrayList<Change> changes) {
        this.changes = changes;
    }
}
