package org.activityinfo.server.command.handler;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides a mapping from indicator ids to activity ids useful in translating legacy API requests.
 * 
 * <p>The mapping from indicatorId to activityId is immutable and so can be aggressively cached.</p>
 */
@Singleton
public class IndicatorOracle {

    private final Provider<EntityManager> entityManager;
    private final Cache<Integer, ResourceId> indicatorCache;


    @Inject
    public IndicatorOracle(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
        indicatorCache = CacheBuilder.newBuilder().maximumSize(10000).build();
    }
    
    public Map<Integer, ResourceId> indicatorToForm(Iterable<Integer> indicatorIds) {
        Set<Integer> toFetch = new HashSet<>();
        Map<Integer, ResourceId> mapping = new HashMap<>();

        for (Integer indicatorId : indicatorIds) {
            ResourceId formClassId = indicatorCache.getIfPresent(indicatorId);
            if(formClassId != null) {
                mapping.put(indicatorId, formClassId);
            } else {
                toFetch.add(indicatorId);
            }
        }
        
        if(!toFetch.isEmpty()) {
            fetch(mapping, toFetch);
        }
        
        return mapping;
    }

    /**
     * Fetches uncached activityids from the store.
     */
    private void fetch(final Map<Integer, ResourceId> mapping, final Set<Integer> toFetch) {
        HibernateEntityManager entityManager = (HibernateEntityManager) this.entityManager.get();
        entityManager.getSession().doWork(new AbstractWork() {
            @Override
            public void execute(Connection connection) throws SQLException {
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT i.indicatorId, i.activityId, a.reportingFrequency " +
                        "FROM indicator i " +
                        "LEFT JOIN activity a ON (a.activityId=i.activityId) " +
                        "WHERE indicatorId IN (");
                Joiner.on(",").appendTo(sql, toFetch);
                sql.append(")");
                
                try(Statement s = connection.createStatement()) {
                    try(ResultSet rs = s.executeQuery(sql.toString())) {
                        while(rs.next()) {
                            int indicatorId = rs.getInt(1);
                            int activityId = rs.getInt(2);
                            int reportingFrequency = rs.getInt(3);
                            ResourceId formClassId;
                            if(reportingFrequency == ActivityFormDTO.REPORT_MONTHLY) {
                                formClassId = CuidAdapter.reportingPeriodFormClass(activityId);
                            } else {
                                formClassId = CuidAdapter.activityFormClass(activityId);
                            }
                            mapping.put(indicatorId, formClassId);
                            indicatorCache.put(indicatorId, formClassId);
                        }
                    }
                }
            }
        });
    }
}
