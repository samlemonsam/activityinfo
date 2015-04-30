package org.activityinfo.geoadmin.merge.model;

import org.junit.Test;

import static org.junit.Assert.*;

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