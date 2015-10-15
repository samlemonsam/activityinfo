package org.activityinfo.server.command.handler.pivot;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.AbstractWork;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a mapping from indicator ids to activity ids useful in translating legacy API requests.
 * 
 * <p>The mapping from indicatorId to activityId is immutable and so can be aggressively cached.</p>
 */
@Singleton
public class IndicatorOracle {

    private final Provider<EntityManager> entityManager;


    @Inject
    public IndicatorOracle(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }
    
    /**
     * Fetches uncached activityids from the store.
     */
    public List<ActivityMetadata> fetch(final Filter filter) {
        
        final Map<Integer, ActivityMetadata> activityMap = new HashMap<>();
        
        if(tooBroad(filter)) {
            throw new CommandException("Filter is too broad: must filter on Indicator, Activity, or Database");
        }
        
        HibernateEntityManager entityManager = (HibernateEntityManager) this.entityManager.get();
        entityManager.getSession().doWork(new AbstractWork() {
            @Override
            public void execute(Connection connection) throws SQLException {
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT" +
                        " i.indicatorId, " +        // (1)    
                        " i.aggregation, " +        // (2)    
                        " i.activityId," +          // (3)
                        " a.name, " +               // (4)
                        " a.category, " +            // (5)
                        " a.reportingFrequency," +  // (6)
                        " a.databaseId, " +         // (7)
                        " d.name " +               // (8)
                        "FROM indicator i " +
                        "LEFT JOIN activity a ON (a.activityId=i.activityId) " +
                        "LEFT JOIN userdatabase d ON (a.databaseId=d.databaseId) " +
                        "WHERE i.type = 'QUANTITY' ");
                
                
                appendFilter(filter, DimensionType.Indicator, "i.indicatorId", sql);
                appendFilter(filter, DimensionType.Activity, "a.activityId", sql);
                appendFilter(filter, DimensionType.Database, "a.databaseId", sql);
    
    
                try (Statement s = connection.createStatement()) {
                    try (ResultSet rs = s.executeQuery(sql.toString())) {
                        while (rs.next()) {
                            int activityId = rs.getInt(3);
                            
                            ActivityMetadata activity = activityMap.get(activityId);
                            if(activity == null) {
                                activity = new ActivityMetadata();
                                activity.id = activityId;
                                activity.name = rs.getString(4);
                                activity.categoryName = rs.getString(5);
                                activity.reportingFrequency = rs.getInt(6);
                                activity.databaseId = rs.getInt(7);
                                activity.databaseName = rs.getString(8);
                                activityMap.put(activityId, activity);
                            }
                            
                            IndicatorMetadata indicator = new IndicatorMetadata();
                            indicator.id = rs.getInt(1);
                            indicator.activityId = activityId;
                            indicator.aggregation = rs.getInt(2);
                            
                            activity.indicators.add(indicator);
                        }
                    }
                }
            }
        });
        
        return Lists.newArrayList(activityMap.values());
    }

    private boolean tooBroad(Filter filter) {
        if(filter.isRestricted(DimensionType.Indicator) ||
           filter.isRestricted(DimensionType.Activity) ||
           filter.isRestricted(DimensionType.Database)) {
            return false;
        }
        
        return true;
    }

    private void appendFilter(Filter filter, DimensionType type, String column, StringBuilder sql) {
        if(filter.isRestricted(type)) {
            sql.append(" AND ").append(column).append(" IN (");
            Joiner.on(",").appendTo(sql, filter.getRestrictions(type));
            sql.append(")");
        }
    }
}
