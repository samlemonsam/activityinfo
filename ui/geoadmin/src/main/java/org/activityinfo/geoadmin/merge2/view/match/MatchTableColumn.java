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
import org.activityinfo.geoadmin.match.MatchLevel;

/**
 * Single column in the merge table
 */
public abstract class MatchTableColumn {

    public abstract String getHeader();

    public abstract String getValue(int rowIndex);

    public abstract Optional<MatchSide> getSide();
    
    public Optional<MatchLevel> getMatchConfidence(int rowIndex) {
        return Optional.absent();
    }

}
