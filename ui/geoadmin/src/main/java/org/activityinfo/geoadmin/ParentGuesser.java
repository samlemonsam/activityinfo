package org.activityinfo.geoadmin;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;
import org.activityinfo.geoadmin.model.AdminEntity;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Given a new set of administrative entities from a shapefile, guess their
 * parents within the existing administrative entity based on name, code, and
 * geography
 * 
 */
public class ParentGuesser {

    private static final double MIN_SCORE = 0.75;

    private ImportSource importSource;
    private STRtree index;
    
    public enum Quality {
        OK,
        WARNING,
        SEVERE
    }

    /**
     * 
     * @param importSource
     *            the imported features/entities
     * @param parents
     *            prospective parents of the source entities
     */
    public ParentGuesser(ImportSource importSource, List<AdminEntity> parents) {
        super();
        this.importSource = importSource;
        if(!parents.isEmpty()) {
            this.index = new STRtree(parents.size());

            // create a spatial index to help narrow down the search
            for (AdminEntity entity : parents) {
                Envelope mbr = GeoUtils.toEnvelope(entity.getBounds());
                mbr.expandBy(mbr.getWidth() * 0.10, mbr.getHeight() * 0.10);
                index.insert(mbr, entity);
            }
        }
    }

    public AdminEntity[] run() throws IOException {
        AdminEntity[] matches = new AdminEntity[importSource.getFeatureCount()];
        for (int i = 0; i != matches.length; ++i) {
            matches[i] = findBestMatch(importSource.getFeatures().get(i));
        }
        return matches;

    }

    /**
     * Finds the best matching parent for a given feature
     * 
     * @param featureIndex
     *            the index of the feature in the import source
     * @return the best matching admin entity
     */
    private AdminEntity findBestMatch(ImportFeature feature) {
    	
    	List<AdminEntity> spatialMatches = index.query(feature.getEnvelope());
    	
        return findBestParent(feature, spatialMatches);
    }

	public static AdminEntity findBestParent(ImportFeature feature,
			Collection<AdminEntity> spatialMatches) {
		double bestScore = MIN_SCORE;
        AdminEntity bestParent = null;
        for (AdminEntity parent : spatialMatches) {
            double score = scoreParent(feature, parent);
            if (score > bestScore) {
                bestScore = score;
                bestParent = parent;
            }
        }
        return bestParent;
	}

    /**
     * Scores a prospective parent based on geography, name and code
     * 
     * @param feature
     * @param parent
     * @return a score describe how will the parent entity matches as a parent
     *         of the feature at feature index. 0 = poor match.
     */
    private static double scoreParent(ImportFeature feature, AdminEntity parent) {

        // parent should completely contain the child
        // find the proportion contained
        double propContained = scoreGeography(feature, parent);

        // check the name similarity
        double nameSimilarity = scoreName(feature, parent);

        // check for the presence of the code
        double codeScore = scoreCodeMatch(feature, parent);

        // System.out.println(String.format("%s <> %s %.2f %.2f %.2f",
        // importSource.featureToString(featureIndex),
        // propContained, nameSimilarity, codeScore));

        return propContained + (nameSimilarity * 3d) + codeScore;
    }

    /**
     * Scores the prospective parent based on name similarity. 1=high, meaning
     * that the feature contains an exact match of the parent's name in one of
     * its columns.
     * 
     * @param feature
     * @param parent
     *            the prospective parent to evaluate
     * @return a score from 0=poor match, 1=perfect match
     */
    public static double scoreName(ImportFeature feature, AdminEntity parent) {
        return feature.similarity(parent.getName());
    }

    /**
     * Scores the prospective parent based on the presence of the parent code in
     * the feature's column.
     * 
     * @param feature
     *            the index of the feature
     * @param parent
     *            the prospective parent to evaluate
     * @return a score from 0=poor match, 1=perfect match
     */
    public static double scoreCodeMatch(ImportFeature feature, AdminEntity parent) {
        if (parent.getCode() != null) {
            if (Codes.hasCode(feature.getAttributeValues(), parent.getCode())) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Scores the prospective parent based on geography. A perfectly matched
     * parent will entirely contain the child entity. (we only use MBRs here)
     * 
     * @param feature
     * @param parent
     *            the prospective parent to evaluate
     * @return a score from 0=poor match, no intersection, 1=perfect match,
     *         competely contained
     */
    public static double scoreGeography(ImportFeature feature, AdminEntity parent) {
        Envelope parentEnvelope = GeoUtils.toEnvelope(parent.getBounds());
        Envelope childEnvelope = feature.getEnvelope();
        
        if(childEnvelope.getArea() > 0) {
	        
	        double propContained = parentEnvelope.intersection(childEnvelope).getArea() /
	            childEnvelope.getArea();
	        return propContained;
        
        } else {
        	// we have only a point representation
        	return parentEnvelope.contains(childEnvelope) ? 1 : 0;
        	
        }
    }

    /**
     * Evaluates the quality of the match between an imported feature and and a
     * prospective parent.
     * 
     * @param feature
     * @param parent
     *            the propsective parent
     * 
     * @return a qualitative evaluation of the match
     */
    public Quality quality(ImportFeature feature, AdminEntity parent) {

        double geoScore = scoreGeography(feature, parent);
        if (geoScore < 0.97) {
            return Quality.WARNING;
        }
        if (geoScore < 0.90) {
            return Quality.SEVERE;
        }

        double nameScore = scoreName(feature, parent);
        if (nameScore < 0.80) {
            return Quality.SEVERE;
        }

        if (nameScore < 1) {
            return Quality.WARNING;
        }

        return Quality.OK;

    }
}
