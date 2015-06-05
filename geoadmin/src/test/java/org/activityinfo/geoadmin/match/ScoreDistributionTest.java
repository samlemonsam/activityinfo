package org.activityinfo.geoadmin.match;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ScoreDistributionTest {

    @Test
    public void binning() {
        assertThat(ScoreDistribution.binIndexOf(0), equalTo(0));
        assertThat(ScoreDistribution.binIndexOf(1), equalTo(ScoreDistribution.BIN_COUNT - 1));
    }
    
    @Test
    public void percentile() {
        ScoreDistribution.Builder builder = new ScoreDistribution.Builder();
        builder.add(0.43);
        builder.add(0.43);
        builder.add(0.43);
        builder.add(1.00);
        builder.add(1.00);
        ScoreDistribution distribution = builder.build();
        
        assertThat(distribution.percentile(0.0), equalTo(0.00));
        assertThat(distribution.percentile(0.3), equalTo(0.00));
        assertThat(distribution.percentile(0.5), equalTo(0.60));
        assertThat(distribution.percentile(1.0), equalTo(0.60));
    }
    
    @Test
    public void frequentHighScoresAreWeightedLow() {
        ScoreDistribution.Builder builder = new ScoreDistribution.Builder();
        builder.add(1.00);
        builder.add(1.00);
        builder.add(1.00);
        builder.add(1.00);
        builder.add(1.00);
        ScoreDistribution distribution = builder.build();

        assertThat(distribution.percentile(1.00), equalTo(0.00));
        
    }
}