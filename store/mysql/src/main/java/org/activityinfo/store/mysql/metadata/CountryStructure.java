/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.store.mysql.metadata;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Stores metadata on a Country's admin levels
 */
public class CountryStructure {

    /**
     * Admin levels change quite rarely, so unconditionally retain them at the instance level for up to 10 minutes.
     */
    private static final Cache<Integer, CountryStructure> INSTANCE_CACHE = CacheBuilder
            .newBuilder()
            .expireAfterWrite(10, TimeUnit.HOURS)
            .build();


    private List<Integer> adminLevels = new ArrayList<>();
    private Map<Integer, Integer> parentMap = new HashMap<>();
    
    private CountryStructure() {}
    
    public static void clearCache() {
        INSTANCE_CACHE.invalidateAll();
    }
    
    public static CountryStructure query(QueryExecutor executor, int countryId) throws SQLException {
        
        // Try first to fetch from the instance-level cache
        CountryStructure country = INSTANCE_CACHE.getIfPresent(countryId);
        if(country != null) {
            return country;
        }
        
        // Otherwise we have to hit the database
        country = new CountryStructure();

        String sql =
                "SELECT adminLevelId, parentId " +
                        "FROM adminlevel L " +
                        "WHERE countryid = " + countryId;

        try (ResultSet rs = executor.query(sql)) {
            while(rs.next()) {
                int adminLevelId = rs.getInt(1);
                country.adminLevels.add(adminLevelId);
                
                int parentId = rs.getInt(2);
                if(!rs.wasNull()) {
                    country.parentMap.put(adminLevelId, parentId);
                }
            }
        }
        
        INSTANCE_CACHE.put(countryId, country);
        
        return country;
    }

    public List<Integer> getAdminLevelIds() {
        return adminLevels;
    }
    
    public List<ResourceId> getAdminLevelFormClassIds() {
        List<ResourceId> set = new ArrayList<>();
        for (Integer adminLevel : adminLevels) {
            set.add(CuidAdapter.adminLevelFormClass(adminLevel));
        }
        return set;
    }

    /**
     * 
     * @return a sorted array of admin level ids assocated with this country
     */
    public int[] getAdminLevelIdArray() {
        int array[] = new int[adminLevels.size()];
        int i = 0;
        for (Integer id : adminLevels) {
            array[i++] = id;
        }
        Arrays.sort(array);
        return array;
    }


    /**
     * Given an array of admin level ids, return an array which gives
     * the index of the parent level, or -1 if the level at that index
     * does not have a parent.
     */
    public int[] buildParentIndexMap(int[] idArray) {
        
        int[] parents = new int[idArray.length];
        
        for (int i = 0; i < idArray.length; i++) {
            int levelId = idArray[i];
            Integer parentId = parentMap.get(levelId);
            
            if(parentId == null) {
                parents[i] = -1;
            } else {
                parents[i] = Arrays.binarySearch(idArray, parentId);
            }
        }
        
        return parents;
    }
}
