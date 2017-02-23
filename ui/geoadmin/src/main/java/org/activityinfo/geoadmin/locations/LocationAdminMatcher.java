package org.activityinfo.geoadmin.locations;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import org.activityinfo.geoadmin.GeoUtils;
import org.activityinfo.geoadmin.ImportFeature;
import org.activityinfo.geoadmin.ParentGuesser;
import org.activityinfo.geoadmin.PlaceNames;
import org.activityinfo.geoadmin.model.ActivityInfoClient;
import org.activityinfo.geoadmin.model.AdminEntity;
import org.activityinfo.geoadmin.model.AdminLevel;
import org.activityinfo.geoadmin.model.AdminLevelNode;
import org.activityinfo.geoadmin.model.AdminLevelTree;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.vividsolutions.jts.geom.Envelope;

public class LocationAdminMatcher {

	private ActivityInfoClient client;
	private AdminLevelTree tree;
	private Map<AdminLevelNode, Integer> attributeMap = Maps.newHashMap();

	private Multimap<AdminLevelNode, AdminEntity> entities = HashMultimap.create();
	
	public LocationAdminMatcher(ActivityInfoClient client,
			List<AdminLevel> levels) {

		this.client = client;
		this.tree = new AdminLevelTree(levels);
	
	}
	

	public void setLevelAttribute(AdminLevel level, int attributeIndex) {
		AdminLevelNode levelNode = tree.getLevelById(level.getId());
		if(attributeIndex >= 0) {
			attributeMap.put(levelNode, attributeIndex);
		} else {
			attributeMap.remove(levelNode);
		}
	}

	public List<AdminEntity> forFeature(ImportFeature feature) {
		List<AdminEntity> matches = Lists.newArrayList();
		matchChildren(tree.getRootNode(), null, feature, matches);
		return matches;
	}

	private void matchChildren(AdminLevelNode parentLevel, AdminEntity parentEntity, ImportFeature feature, List<AdminEntity> matches) {
		for(AdminLevelNode level : parentLevel.getChildLevels()) {

            Integer attributeIndex = attributeMap.get(level);
            if(attributeIndex != null) {
                if(Strings.isNullOrEmpty(feature.getAttributeStringValue(attributeIndex))) {
                    continue;
                }
            }

			AdminEntity bestMatch = findBestParent(feature, queryEntities(level, parentEntity));
			if(bestMatch != null) {
				matches.add(bestMatch);
				matchChildren(level, bestMatch, feature, matches);
			}
		}
	}

	private Collection<AdminEntity> queryEntities(AdminLevelNode level, AdminEntity parentEntity) {
		
		// download if we don't have it already
		if(entities.get(level).isEmpty()) {
			entities.putAll(level, client.getAdminEntities(level.getId()));
		}
	
		if(parentEntity == null) {
			return entities.get(level);
		} else {
			List<AdminEntity> children = Lists.newArrayList();
			for(AdminEntity entity : entities.get(level)) {
				if(entity.getParentId() == parentEntity.getId()) {
					children.add(entity);
				}
			}
			return children;
		}
		
	}

	public AdminEntity findBestParent(ImportFeature feature,
			Collection<AdminEntity> spatialMatches) {
		double bestScore = 0;
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
    private double scoreParent(ImportFeature feature, AdminEntity parent) {

        // parent should completely contain the child
        // find the proportion contained
        double propContained = scoreGeography(feature, parent);

        // check the name similarity
        double nameSimilarity = scoreName(feature, parent);

        // System.out.println(String.format("%s <> %s %.2f %.2f %.2f",
        // importSource.featureToString(featureIndex),
        // propContained, nameSimilarity, codeScore));

        return propContained + (nameSimilarity * 3d);
        //return nameSimilarity;
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
    public double scoreName(ImportFeature feature, AdminEntity parent) {
    	AdminLevelNode level = tree.getLevelById(parent.getLevelId());
    	Integer attributeIndex = attributeMap.get(level);
    	if(attributeIndex == null) {
    		return 0;
    	} else {
    		return PlaceNames.similarity(parent.getName(), feature.getAttributeStringValue(attributeIndex));
    	}
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
    public double scoreGeography(ImportFeature feature, AdminEntity parent) {
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
}
