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

            try (Statement s = connection.createStatement()) {
              fetchIndicators(s, filter, activityMap);
            }
          }
        });
        
        return Lists.newArrayList(activityMap.values());
    }

    private void fetchIndicators(Statement s, Filter filter, Map<Integer, ActivityMetadata> activityMap) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT" +
                " i.indicatorId, " +        // (1)    
                " i.aggregation, " +        // (2)    
                " a.activityId," +          // (3)
                " a.name, " +               // (4)
                " a.category, " +            // (5)
                " a.reportingFrequency," +  // (6)
                " a.databaseId, " +         // (7)
                " d.name, " +               // (8)
                " i.sortOrder, " +          // (9)
                " i.name, " +               // (10)
                " k.sourceIndicatorId, " +       // (11)
                " si.activityId, " +        // (12)
                " sa.reportingFrequency, " + // (13)
                " (si.dateDeleted IS NOT NULL AND " + // (14) 
                  "sa.datedeleted IS NOT NULL)  " + 
                "FROM activity a " +
                "LEFT JOIN indicator i ON (a.activityId=i.activityId) " +
                "LEFT JOIN userdatabase d ON (a.databaseId=d.databaseId) " +
                "LEFT JOIN indicatorlink k ON (i.indicatorId = k.destinationIndicatorId) " +
                "LEFT JOIN indicator si ON (k.sourceIndicatorId = si.indicatorId) " +
                "LEFT JOIN activity sa ON (si.activityId = a.activityId) " +
                "WHERE (i.type = 'QUANTITY' OR i.type IS NULL) ");


        appendFilter(filter, DimensionType.Indicator, "i.indicatorId", sql);
        appendFilter(filter, DimensionType.Activity, "a.activityId", sql);
        appendFilter(filter, DimensionType.Database, "a.databaseId", sql);
        try (ResultSet rs = s.executeQuery(sql.toString())) {

            while (rs.next()) {
                int activityId = rs.getInt(3);
                assert  activityId != 0;

                ActivityMetadata activity = activityMap.get(activityId);
                if (activity == null) {
                    activity = new ActivityMetadata();
                    activity.id = activityId;
                    activity.name = rs.getString(4);
                    activity.categoryName = rs.getString(5);
                    activity.reportingFrequency = rs.getInt(6);
                    activity.databaseId = rs.getInt(7);
                    activity.databaseName = rs.getString(8);
                    activityMap.put(activityId, activity);
                }

                int indicatorId = rs.getInt(1);
                if(!rs.wasNull()) {
                  IndicatorMetadata indicator = activity.indicators.get(indicatorId);
                  if (indicator == null) {
                    indicator = new IndicatorMetadata();
                    indicator.sourceId = indicatorId;
                    indicator.destinationId = indicatorId;
                    indicator.name = rs.getString(10);
                    indicator.aggregation = rs.getInt(2);
                    indicator.sortOrder = rs.getInt(9);
                    activity.indicators.put(indicatorId, indicator);
                  }

                  int linkedIndicatorId = rs.getInt(11);
                  if (!rs.wasNull()) {
                    boolean deleted = rs.getBoolean(14);
                    if (!deleted) {

                      ActivityMetadata linkedActivity;
                      int linkedActivityId = rs.getInt(12);
                      if (linkedActivityId == activityId) {
                        linkedActivity = activity;
                      } else {
                        linkedActivity = activity.linkedActivities.get(linkedActivityId);
                        if (linkedActivity == null) {
                          linkedActivity = new ActivityMetadata();
                          linkedActivity.id = linkedActivityId;
                          linkedActivity.reportingFrequency = rs.getInt(13);
                          // in the context of being linked, the activity is treated as if 
                          // it the same as the destination activity
                          linkedActivity.name = activity.getName();
                          linkedActivity.databaseId = activity.getDatabaseId();
                          linkedActivity.databaseName = activity.getDatabaseName();
                          activity.linkedActivities.put(linkedActivityId, linkedActivity);
                        }
                      }

                      IndicatorMetadata linkedIndicator = new IndicatorMetadata();
                      linkedIndicator.sourceId = linkedIndicatorId;
                      linkedIndicator.destinationId = indicatorId;

                      // in the context of being linked, this indicator is treated as 
                      // if it were the same as the destination indicator
                      linkedIndicator.name = indicator.getName();
                      linkedIndicator.sortOrder = indicator.sortOrder;
                      linkedIndicator.aggregation = indicator.aggregation;
                      linkedActivity.linkedIndicators.add(linkedIndicator);
                    }
                  }
                }
            }
        }
    }

    private boolean tooBroad(Filter filter) {
      return !(
          filter.isRestricted(DimensionType.Indicator) ||
          filter.isRestricted(DimensionType.Activity) ||
          filter.isRestricted(DimensionType.Database));

    }

    private void appendFilter(Filter filter, DimensionType type, String column, StringBuilder sql) {
        if(filter.isRestricted(type)) {
            sql.append(" AND ").append(column).append(" IN (");
            Joiner.on(",").appendTo(sql, filter.getRestrictions(type));
            sql.append(")");
        }
    }
}
