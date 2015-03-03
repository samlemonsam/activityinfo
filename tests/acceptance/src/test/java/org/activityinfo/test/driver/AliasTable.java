package org.activityinfo.test.driver;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import cucumber.api.DataTable;
import cucumber.runtime.java.guice.ScenarioScoped;
import gherkin.formatter.model.DataTableRow;

import java.util.List;
import java.util.Random;

@ScenarioScoped
public class AliasTable {

   
    /**
     * Maps test/user-friendly names to unique names that we really use
     */
    private BiMap<String, String> nameMap = HashBiMap.create();

    private BiMap<String, Integer> idMap = HashBiMap.create();



    private Random random = new Random();

    /**
     * Gets a randomized name for a domain object given
     * the friendly test-facing name
     */
    public String create(String alias) {
        Preconditions.checkState(!nameMap.containsKey(alias), "There is already a test object with alias '%s'", alias);

        String name = alias + "_" + Long.toHexString(random.nextLong());
        nameMap.put(alias, name);
        return name;
    }

    public void bindId(String alias, int newId) {
        idMap.put(alias, newId);   
    }

    public int getId(String alias) {
        Preconditions.checkState(idMap.containsKey(alias), "Unknown alias '%s'", alias);
        return idMap.get(alias);
    }
    
    public int generateIdFor(String alias) {
        int id = generateId();
        idMap.put(alias, id);
        return id;
    }

    public int generateId() {
        return random.nextInt(Integer.MAX_VALUE-1);
    }

    /**
     * Maps a "real" name to its server-generated ID
     */
    public void mapNameToId(String name, int id) {
        String alias = nameMap.inverse().get(name);
        idMap.put(alias, id);
        
    }

    public String getName(String alias) {
        return nameMap.get(alias);
    }

    /**
     * Replaces strongly named objects in the given table with their 
     * test-facing aliases. 
     * 
     * <p>For example, if your test has a partner called "NRC", the the
     * test driver will actually create a partner called something like
     * "NRC_5e9823d88d563417" to ensure that any existing state does not
     * interfere with the current test run.
     * 
     * <p>This method will replace "NRC_5e9823d88d563417" with "NRC" so that
     * the output can be presented back to the user.</p>
     */
    public DataTable alias(DataTable table) {
        List<List<String>> rows = Lists.newArrayList();
        for (DataTableRow row : table.getGherkinRows()) {
            List<String> cells = Lists.newArrayList();
            for (String cell : row.getCells()) {
                if(nameMap.inverse().containsKey(cell)) {
                    String alias = nameMap.inverse().get(cell);
                    cells.add(alias);
                } else {
                    cells.add(cell.trim());
                }
            }
            rows.add(cells);
        }
        return DataTable.create(rows);
    }

    public boolean isName(String text) {
        return nameMap.containsValue(text);
    }
    
    public String alias(String name) {
        return nameMap.inverse().get(name);
    }


    /**
     * @return a random 32-bit integer key
     */
    public int generateInt() {
        return random.nextInt(Integer.MAX_VALUE);
    }
    
    public int getOrGenerateId(String alias) {
        if(idMap.containsKey(alias)) {
            return idMap.get(alias);
        } else {
            int newId = generateId();
            idMap.put(alias, newId);
            return newId;
        }
    }
}
