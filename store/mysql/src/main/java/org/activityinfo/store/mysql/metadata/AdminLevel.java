package org.activityinfo.store.mysql.metadata;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.spi.FormNotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;


public class AdminLevel {
    private int id;
    private String name;
    private int parentId;
    private String parentName;
    private int countryId;
    private long version;

    public static AdminLevel fetch(QueryExecutor executor, int levelId) throws SQLException {

        // The shape of the AdminEntity FormClass is determined in part by the parameters
        // set in the adminlevel table
        try (ResultSet rs = executor.query(
                "SELECT " +
                        "L.Name, " +
                        "L.parentId ParentId, " +
                        "P.Name ParentLevelName, " +
                        "L.CountryId, " +
                        "L.Version " +
                        "FROM adminlevel L " +
                        "LEFT JOIN adminlevel P ON (L.parentId = P.AdminLevelId) " +
                        "WHERE L.AdminLevelId = " + levelId)) {

            if (!rs.next()) {
                throw new FormNotFoundException(CuidAdapter.adminLevelFormClass(levelId));
            }
            AdminLevel level = new AdminLevel();
            level.id = levelId;
            level.name = rs.getString(1);
            level.parentId = rs.getInt(2);
            if (rs.wasNull()) {
                level.parentId = 0;
            }
            level.parentName = rs.getString(3);
            level.countryId = rs.getInt(4);
            level.version = rs.getLong(5);
            return level;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public int getCountryId() {
        return countryId;
    }

    public boolean hasParent() {
        return parentId != 0;
    }

    public long getVersion() {
        return version;
    }
}