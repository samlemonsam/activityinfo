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
package org.activityinfo.geoadmin.merge.model;

import org.activityinfo.geoadmin.match.MatchRow;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchTest {

    @Test
    public void matchable() {

        MatchRow xx = new MatchRow(1, 1);
        MatchRow xo = new MatchRow(1, MatchRow.UNMATCHED);
        MatchRow ox = new MatchRow(MatchRow.UNMATCHED, 1);
        MatchRow yy = new MatchRow(2, 2);
        MatchRow yo = new MatchRow(2, MatchRow.UNMATCHED);
        MatchRow oy = new MatchRow(MatchRow.UNMATCHED, 2);

        assertTrue(xo.canMatch(oy));
        assertTrue(yo.canMatch(ox));
        
        assertFalse(xx.canMatch(yy));
        assertFalse(xo.canMatch(yo));
        assertFalse(ox.canMatch(oy));
        assertFalse(ox.canMatch(ox));



    }
    
}