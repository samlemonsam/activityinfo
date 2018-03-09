package org.activityinfo.ui.client.component.importDialog.model.match;

import org.junit.Test;

import static org.activityinfo.ui.client.component.importDialog.model.match.Levenshtein.getLevenshteinDistance;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class LevenshteinTest {

    @Test
    public void testLevenshteinDistance() {
        assertThat(getLevenshteinDistance("a", "b"), equalTo(1));
        assertThat(getLevenshteinDistance("ab", "bb"), equalTo(1));
        assertThat(getLevenshteinDistance("ab ", " bb"), equalTo(2));
    }
}