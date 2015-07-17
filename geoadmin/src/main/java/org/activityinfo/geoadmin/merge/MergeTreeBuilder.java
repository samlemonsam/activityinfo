package org.activityinfo.geoadmin.merge;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.geoadmin.*;
import org.activityinfo.geoadmin.model.ActivityInfoClient;
import org.activityinfo.geoadmin.model.AdminEntity;
import org.activityinfo.geoadmin.model.AdminLevel;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MergeTreeBuilder {

    private static final Logger LOGGER = Logger.getLogger(MergeTreeBuilder.class.getName());

    private ActivityInfoClient client;
    private List<AdminLevel> parentLevels = Lists.newArrayList();
    private List<AdminLevel> levels = Lists.newArrayList();
    private Map<Integer, List<AdminEntity>> levelMap = Maps.newHashMap();

    /**
     * Map from admin level id to the name attribute index
     */
    private Map<Integer, Integer> nameMap = Maps.newHashMap();
    private Map<Integer, Integer> codeMap = Maps.newHashMap();

    private AdminLevel level;
    private ImportSource source;

    public MergeTreeBuilder(ActivityInfoClient client, AdminLevel level, ImportSource source) {
        this.client = client;
        this.level = level;
        this.source = source;
    }

    public MergeNode build() {

        findParentLevels();
        loadEntities();
        findNameAttributes();

        MergeNode root = new MergeNode();
        if (parentLevels.isEmpty()) {
            addLeafNodes(root, null, source.getFeatures());
        } else {
            addChildNodes(root, parentLevels.get(0), source.getFeatures());
        }

        return root;
    }

    private void loadEntities() {
        loadEntities(level);
        for(AdminLevel levelId : parentLevels) {
            loadEntities(levelId);
        }
    }

    private void loadEntities(AdminLevel level) {
        List<AdminEntity> entities = client.getAdminEntities(level);
        levels.add(level);
        levelMap.put(level.getId(), entities);
    }

    private void findNameAttributes() {

        LOGGER.info("Finding name attributes");

        int numLevels = levels.size();
        int numAttributes = source.getAttributeCount();

        ColumnMatchMatrix matrix = new ColumnMatchMatrix(numLevels, numAttributes);

        for (int i = 0; i < levels.size(); i++) {
            for(AdminEntity entity : levelMap.get(levels.get(i).getId())) {
                String entityName = entity.getName();
                String entityCode = entity.getCode();
                for(ImportFeature feature : source.getFeatures()) {
                    for(int j=0;j!=numAttributes;++j) {
                        matrix.addScore(i, j, PlaceNames.similarity(entityName, isString(feature, j)));
                    }
                }
            }
        }

        int[] match = matrix.solve();

        for (int i = 0; i < match.length; i++) {
            int bestMatch = match[i];
            if(bestMatch >= 0) {
                LOGGER.info("Matched name attribute for " + levels.get(i).getName() + " to " + source.getAttributeNames()[bestMatch]);
                nameMap.put(levels.get(i).getId(), bestMatch);
            } else {
                LOGGER.info("Did not find acceptable match for name attribute for level " + level.getName());
            }
        }
    }

    private String isString(ImportFeature feature, int j) {
        Object attributeValue = feature.getAttributeValue(j);
        if(attributeValue instanceof String) {
            return (String)attributeValue;
        }
        return null;
    }

    private void addChildNodes(MergeNode parentNode, AdminLevel adminLevel, List<ImportFeature> features) {

        AdminLevel childLevel = nextParent(adminLevel);


        List<AdminEntity> parents;
        if(parentNode.getEntity() == null) {
           parents = levelMap.get(adminLevel.getId());
        } else {
           parents = getChildEntities(adminLevel, parentNode.getEntity());
        }

        Joiner joiner = new Joiner(parents, features);
        if(nameMap.containsKey(adminLevel.getId())) {
            joiner.setNameAttributeIndex(nameMap.get(adminLevel.getId()));
        }
        List<AdminEntity> featureParents = joiner.joinParents();

        for (AdminEntity parent : parents) {
            MergeNode node = new MergeNode();
            node.setLevel(level);
            node.setEntity(parent);
            node.setParent(parentNode);
            parentNode.add(node);

            List<ImportFeature> children = Lists.newArrayList();
            for (int i = 0; i != features.size(); ++i) {
                if (featureParents.get(i) == parent) {
                    children.add(features.get(i));
                }
            }

            if (childLevel != null) {
                addChildNodes(node, childLevel, children);
            } else {
                addLeafNodes(node, parent, children);
            }
        }
    }

    private void addLeafNodes(MergeNode parentNode, AdminEntity parent, List<ImportFeature> features) {

        List<AdminEntity> entities = getChildEntities(level, parent);

        Joiner joiner = new Joiner(entities, features);

        List<Join> joins = joiner.joinOneToOne();
        for (Join join : joins) {
            MergeNode node = new MergeNode();
            node.setEntity(join.getEntity());
            node.setFeature(join.getFeature());
            node.setLevel(level);
            node.setParent(parentNode);

            if (join.getEntity() != null && join.getFeature() != null) {
                node.setAction(MergeAction.UPDATE);
            } else {
                node.setAction(MergeAction.IGNORE);
            }

            parentNode.add(node);
        }
    }

    private List<AdminEntity> getChildEntities(AdminLevel level, AdminEntity parent) {
        List<AdminEntity> entities = levelMap.get(level.getId());
        List<AdminEntity> children = Lists.newArrayList();
        for (AdminEntity child : entities) {
            if (parent == null || child.getParentId() == parent.getId()) {
                children.add(child);
            }
        }
        return children;
    }

    private AdminLevel nextParent(AdminLevel parentLevel) {
        int index = parentLevels.indexOf(parentLevel);
        if (index + 1 < parentLevels.size()) {
            return parentLevels.get(index + 1);
        } else {
            return null;
        }
    }

    private void findParentLevels() {
        Integer parentId = level.getParentId();
        while (parentId != null) {
            AdminLevel parentLevel = client.getAdminLevel(parentId);
            parentLevels.add(0, parentLevel);
            parentId = parentLevel.getParentId();
        }
    }

}
