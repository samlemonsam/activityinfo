package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Function;
import org.activityinfo.geoadmin.match.HungarianAlgorithm;

import java.util.logging.Logger;


public class AutoMatcher implements Function<FieldMatching, AutoRowMatching> {

    private static final Logger LOGGER = Logger.getLogger(AutoMatcher.class.getName());

    @Override
    public AutoRowMatching apply(FieldMatching fieldMatching) {

        LOGGER.info("Starting row matching...");

        RowDistanceMatrix matrix = new RowDistanceMatrix(fieldMatching);
        HungarianAlgorithm algorithm = new HungarianAlgorithm(matrix);

        int[] assignment = algorithm.execute();

        LOGGER.info("Finished row matching...");

        return new AutoRowMatching(fieldMatching, matrix, assignment);
    }
}
