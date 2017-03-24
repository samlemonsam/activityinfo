package org.activityinfo.ui.client.analysis.viewModel;

import java.util.List;

public class Regrouping {

    /**
     * The group membership array for the original grouping based on the full set of dimensions.
     *
     * <p>There will be one element for each element in the value array that gives the index of the full grouping
     * in {@code oldGroups}.</p>
     */
    private int[] oldGroupArray;


    /**
     * The list of old groups.
     *
     * <p>Each list item is a {@code String[]} containing the dimension categories for this group. For a dataset
     * grouped by Gender and Age, for example, you might have the following groups:</p>
     *
     * <ul>
     *     <li>0: [Female, Child]</li>
     *     <li>1: [Female, Adult]</li>
     *     <li>2: [Male, Child]</li>
     *     <li>3: [Male, Adult]</li>
     * </ul>
     *
     */
    private final List<String[]> oldGroups;


    /**
     * The list of new groups, formed by collapsing the old groups by one or more dimensions.
     *
     * <p>For example, if you take an original grouping by Gender and Age, and total by Age, then that leaves
     * the following "new" groups:
     * </p>
     *
     * <ul>
     *     <li>0: [Female]</li>
     *     <li>1: [Male]</li>
     * </ul>
     */
    private List<String[]> newGroups;


    private boolean[] subset;
    /**
     * A map from the original group indices to the new collapsed group indices.
     *
     * <p>For example, if the old groups are aggregated by Gender and Age, and the new groups omit the Age dimension,
     *  then the mapping array would look like:</p>
     *
     *  <ul>
     *     <li>0: 0   [Female, Child] -> [Female]</li>
     *     <li>1: 0   [Female, Adult] -> [Female]</li>
     *     <li>2: 1   [Male, Child]   -> [Male]</li>
     *     <li>3: 1   [Male, Adult]   -> [Male]</li>
     *  </ul>
     *
     */
    private int[] map;


    /**
     * The group membership array for the new grouping.
     *
     */
    private int[] newGroupArray;

    /**
     * @param oldGroupArray the original group membership array
     * @param map an array mapping the original groups to the collapsed groups
     * @param newGroups the new list of collapsed groups
     */
    public Regrouping(int[] oldGroupArray, List<String[]> oldGroups,
                      boolean subset[], int[] map, List<String[]> newGroups) {
        this.oldGroupArray = oldGroupArray;
        this.subset = subset;
        this.map = map;
        this.newGroups = newGroups;
        this.oldGroups = oldGroups;
    }

    public int[] getNewGroupArray() {
        if(newGroupArray == null) {
            newGroupArray = new int[oldGroupArray.length];
            for (int i = 0; i < oldGroupArray.length; i++) {
                int oldGroup = oldGroupArray[i];
                if(oldGroup == -1) {
                    newGroupArray[i] = -1;
                } else {
                    newGroupArray[i] = map[oldGroup];
                }
            }
        }
        return newGroupArray;
    }

    public List<String[]> getOldGroups() {
        return oldGroups;
    }

    public int[] getOldGroupArray() {
        return oldGroupArray;
    }

    public List<String[]> getNewGroups() {
        return newGroups;
    }

    public int getNewGroupCount() {
        return newGroups.size();
    }

    public int[] getMap() {
        return map;
    }

    public String[] getOldGroup(int index) {
        return oldGroups.get(index);
    }

    public boolean isDimensionTotaled(int index) {
        return subset[index];
    }


}
