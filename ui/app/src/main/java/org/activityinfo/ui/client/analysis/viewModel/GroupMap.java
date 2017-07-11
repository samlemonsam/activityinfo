package org.activityinfo.ui.client.analysis.viewModel;


import java.util.*;


public class GroupMap {

    private final List<EffectiveMapping> dimensions;
    private int[] groupArray;
    private final List<String[]> groups;

    public GroupMap(List<EffectiveMapping> dimensions, List<String[]> groups, int[] groupArray) {
        this.groups = groups;
        this.dimensions = dimensions;
        this.groupArray = groupArray;
    }

    public int getGroupCount() {
        return groups.size();
    }

    public String[] getGroup(int groupId) {
        return groups.get(groupId);
    }

    public List<String[]> getGroups() {
        return groups;
    }

    /**
     * Merges an existing group array to produce a group array for totals of a single dimension
     */
    public Regrouping regrouping(TotalSubset totalDimensions) {

        int map[] = new int[getGroupCount()];

        List<String[]> newGroups = new ArrayList<>();
        Map<String, Integer> newGroupMap = new HashMap<>();

        // Merge the existing groups into superset by omitting the
        // total dimension
        for (int i = 0; i < groups.size(); i++) {
            String[] group = groups.get(i);
            String regroupedKey = rekey(group, totalDimensions);

            Integer regroupedIndex = newGroupMap.get(regroupedKey);
            if(regroupedIndex == null) {
                String regroup[] = regroup(group, totalDimensions);

                regroupedIndex = newGroups.size();
                newGroups.add(regroup);
                newGroupMap.put(regroupedKey, regroupedIndex);
            }
            map[i] = regroupedIndex;
        }

        return new Regrouping(groupArray, groups, totalDimensions, map, newGroups);
    }

    public GroupMap regroup(TotalSubset totalDimensions) {
        Regrouping regrouping = regrouping(totalDimensions);
        return new GroupMap(dimensions, regrouping.getNewGroups(), regrouping.getNewGroupArray());
    }


    /**
     * Creates a new 'totals' key by omitting totalled dimensions
     *
     * @param group an array of dimension categories
     * @param totalSubset an array indicating which dimensions should be totaled and so omitted from the key.
     */

    private String rekey(String[] group, TotalSubset totalSubset) {
        assert group.length == dimensions.size();
        assert group.length == totalSubset.getDimCount();

        StringBuilder key = new StringBuilder();
        for (int i = 0; i < group.length; i++) {
            if(!totalSubset.isDimensionCollapsed(i)) {
                key.append('\0');
                key.append(group[i]);
            }
        }
        return key.toString();
    }

    private String[] regroup(String[] group, TotalSubset totalSubset) {
        assert group.length == dimensions.size();

        String[] regrouped = new String[group.length];
        for (int i = 0; i < group.length; i++) {
            if(totalSubset.isDimensionCollapsed(i)) {
                regrouped[i] = Point.TOTAL;
            } else {
                regrouped[i] = group[i];
            }
        }
        return regrouped;
    }


    public int[] copyOfGroupArray() {
        return Arrays.copyOf(groupArray, groupArray.length);
    }
}
