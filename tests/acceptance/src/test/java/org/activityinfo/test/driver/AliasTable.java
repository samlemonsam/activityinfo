package org.activityinfo.test.driver;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import cucumber.runtime.java.guice.ScenarioScoped;

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

    public void mapId(String alias, int newId) {
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
}
