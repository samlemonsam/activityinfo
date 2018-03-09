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
package org.activityinfo.geoadmin.match;

import com.google.common.base.Preconditions;

public class MatchRow {
    
    public static final int UNMATCHED = -1;
    
    private int target;
    private int source;

    private boolean deleted = false;

    public MatchRow(int target, int source) {
        this.target = target;
        this.source = source;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }
    
    public boolean isTargetMatched() {
        return target != UNMATCHED;
    }

    public boolean isSourceMatched() {
        return source != UNMATCHED;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    public boolean isMatched() {
        return isSourceMatched() && isTargetMatched();
    }

    public MatchRow split() {
        Preconditions.checkState(isMatched());
        
        MatchRow newRow = new MatchRow(UNMATCHED, source);
        newRow.deleted = deleted;
        source = UNMATCHED;
        
        return newRow;
    }

    /**
     * 
     * @return {@code true} if this {@code MatchRow} "fits" with an other MatchRow 
     */
    public boolean canMatch(MatchRow other) {
        return !isMatched() && 
                isTargetMatched() == !other.isTargetMatched() &&
                isSourceMatched() == !other.isSourceMatched();
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public void setSource(int source) {
        this.source = source;
    }
}
