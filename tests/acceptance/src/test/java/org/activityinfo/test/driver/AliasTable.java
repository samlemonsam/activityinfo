package org.activityinfo.test.driver;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import cucumber.api.DataTable;
import cucumber.runtime.java.guice.ScenarioScoped;
import gherkin.formatter.model.DataTableRow;

import java.util.*;

/**
 * Maintains a mapping between human-readable "test handles" and their unique aliases
 * that are actually used in interacting with the system. This helps isolate tests 
 * running on the same server from each other.
 * 
 * For example, two different tests might refer to "NFI Distribution", or we might run the 
 * same test on several browsers against the same server. If we actually used the name
 * "NFI Distribution", tests run against the UI would not be reliable. 
 * 
 * To avoid this, we decorate each human-readable "test handle" with a random hex string, so
 * that when we see "NFI Distribution_ADc323434" we know that is the object that we've 
 * created in this specific run of the test.
 */
@ScenarioScoped
public class AliasTable {

   
    /**
     * Maps test/user-friendly names to unique names that we really use
     */
    private BiMap<String, String> testHandleToAlias = HashBiMap.create();

    private Map<String, Integer> testHandleToId = Maps.newHashMap();
    

    private Random random = new Random();

    /**
     * Gets a randomized name for a domain object given
     * the friendly test-facing name
     */
    public String createAlias(String testHandle) {
        Preconditions.checkNotNull(testHandle, "testHandle");
        Preconditions.checkState(!testHandleToAlias.containsKey(testHandle), 
                "The test handle has already been assigned the alias '%s'", testHandle);

        String alias = testHandle + "_" + Long.toHexString(random.nextLong());
        testHandleToAlias.put(testHandle, alias);
        return alias;
    }

    public List<String> getTestHandles() {
        Set<String> set = new HashSet<>();
        set.addAll(testHandleToAlias.keySet());
        set.addAll(testHandleToId.keySet());
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return list;
    }

    public int getId(String testHandle) {
        if(!testHandleToId.containsKey(testHandle)) {
            throw missingHandle("Test handle '%s' has not been bound to an id.", testHandle);
        }
        return testHandleToId.get(testHandle);
    }

    private IllegalStateException missingHandle(String message, Object... arguments) {
        StringBuilder s = new StringBuilder();
        s.append(String.format(message, arguments));
        s.append("\n");
        s.append("Test handles:\n");
        for (String handle : getTestHandles()) {
            s.append(String.format("  %s [%s] = %d\n", handle, testHandleToAlias.get(handle), testHandleToId.get(handle)));
        }
        return new IllegalStateException(s.toString());
    }

    public int generateIdFor(String testHandle) {
        Preconditions.checkNotNull(testHandle, "testHandle");

        int id = generateId();
        testHandleToId.put(testHandle, id);
        return id;
    }

    public int generateId() {
        return random.nextInt(Integer.MAX_VALUE-1);
    }

    /**
     * Maps an alias to its server-generated ID
     */
    public void bindAliasToId(String alias, int id) {
        Preconditions.checkNotNull(alias, "alias");
        testHandleToId.put(getTestHandleForAlias(alias), id);
    }

    /**
     * Maps a test handle to its server-generated ID
     */
    public void bindTestHandleToId(String handle, int newId) {
        Preconditions.checkNotNull(handle, "handle");

        Integer existingId = testHandleToId.get(handle);
        if(existingId != null && existingId != newId) {
            throw new IllegalStateException(String.format(
            "Cannot bind test handle %s to id %d: it was previously bound to %d", handle, 
                    existingId,
                    testHandleToId.get(handle)));
        }
        testHandleToId.put(handle, newId);
    }
    
    /**
     * 
     * @param alias the uniquely decorated name
     * @return the human-readable test alias
     */
    public String getTestHandleForAlias(String alias) {
        String testHandle = testHandleToAlias.inverse().get(alias);
        if(testHandle == null) {
            throw missingHandle("Cannot find a test handle for the alias '%s'", alias);
        }
        return testHandle;
    }
    
    public String getAlias(String testHandle) {
        return testHandleToAlias.get(testHandle);
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
                if(testHandleToAlias.inverse().containsKey(cell)) {
                    String alias = testHandleToAlias.inverse().get(cell);
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
        return testHandleToAlias.containsValue(text);
    }
    
    public String alias(String name) {
        return testHandleToAlias.inverse().get(name);
    }


    /**
     * @return a random 32-bit integer key
     */
    public int generateInt() {
        return random.nextInt(Integer.MAX_VALUE);
    }
    
    public int getOrGenerateId(String alias) {
        if(testHandleToId.containsKey(alias)) {
            return testHandleToId.get(alias);
        } else {
            int newId = generateId();
            testHandleToId.put(alias, newId);
            return newId;
        }
    }

}
