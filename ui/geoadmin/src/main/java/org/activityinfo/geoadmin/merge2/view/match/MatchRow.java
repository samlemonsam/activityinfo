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
package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Optional;
import org.activityinfo.geoadmin.merge2.model.InstanceMatch;
import org.activityinfo.model.resource.ResourceId;

public class MatchRow {

    public static final int UNMATCHED = -1;

    private boolean inputRequired = false;
    private boolean resolved = false;
    
    private final KeyFieldPairSet keyFields;
    private int sourceRow = UNMATCHED;
    private int targetRow = UNMATCHED;

    public MatchRow(KeyFieldPairSet keyFields) {
        this.keyFields = keyFields;
    }

    public void setSourceRow(int sourceRow) {
        this.sourceRow = sourceRow;
    }

    public void setTargetRow(int targetRow) {
        this.targetRow = targetRow;
    }


    public int getRow(MatchSide side) {
        if(side == MatchSide.SOURCE) {
            return getSourceRow();
        } else {
            return getTargetRow();
        }
    }
    
    public Optional<ResourceId> getSourceId() {
        if(sourceRow == UNMATCHED) {
            return Optional.absent();
        } 
        return Optional.of(keyFields.getSource().getRowId((sourceRow)));
    }
    
    public Optional<ResourceId> getTargetId() {
        if(targetRow == UNMATCHED) {
            return Optional.absent();
        }
        return Optional.of(keyFields.getTarget().getRowId(targetRow));
    }

    public int getSourceRow() {
        return sourceRow;
    }
    
    public int getTargetRow() {
        return targetRow;
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean isInputRequired() {
        return inputRequired;
    }

    public void setInputRequired(boolean inputRequired) {
        this.inputRequired = inputRequired;
    }

    public boolean isMatched() {
        return sourceRow != UNMATCHED && targetRow != UNMATCHED;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public InstanceMatch asInstanceMatch() {
        return new InstanceMatch(getSourceId(), getTargetId());
    }

    public boolean isMatched(MatchSide matchSide) {
        if(matchSide == MatchSide.SOURCE) {
            return sourceRow != UNMATCHED;
        } else {
            return targetRow != UNMATCHED;
        }
    }

}
