package org.activityinfo.store.mysql.update;

import com.google.common.base.Preconditions;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Updates the indicator value table
 */
public class IndicatorValueTableUpdater {


    private final int siteId;
    private int reportingPeriodId;

    private Date date1;
    private Date date2;


    
    private static class IndicatorUpdate {
        private int indicatorId;
        private FieldValue value;

        public IndicatorUpdate(int indicatorId, FieldValue value) {
            this.indicatorId = indicatorId;
            this.value = value;
        }
    }

    private final List<IndicatorUpdate> updates = new ArrayList<>();


    public IndicatorValueTableUpdater(ResourceId siteResourceId) {
        Preconditions.checkArgument(siteResourceId.getDomain() == CuidAdapter.SITE_DOMAIN);
        siteId = CuidAdapter.getLegacyIdFromCuid(siteResourceId);
    }
    
    public void update(ResourceId fieldId, FieldValue value) {
        Preconditions.checkArgument(fieldId.getDomain() == CuidAdapter.INDICATOR_DOMAIN);
        
        updates.add(new IndicatorUpdate(CuidAdapter.getLegacyIdFromCuid(fieldId), value));
    }


    public void setDate1(FieldValue value) {
        this.date1 = ((LocalDate)value).atMidnightInMyTimezone();
    }

    public void setDate2(FieldValue value) {
        this.date2 = ((LocalDate)value).atMidnightInMyTimezone();
    }
    
    public void execute(QueryExecutor executor) throws SQLException {
        if(!updates.isEmpty()) {
            reportingPeriodId = queryReportingPeriod(executor);

            for (IndicatorUpdate update : updates) {
                executeUpdate(executor, update);
            }
        }
    }
    
    public void insert(QueryExecutor executor) {
        insertReportingPeriod(executor);
        
        for (IndicatorUpdate update : updates) {
            executeUpdate(executor, update);
        }   
    }
    
    public void insertReportingPeriod(QueryExecutor executor) {
        reportingPeriodId = new KeyGenerator().generateInt();
        executor.update("INSERT INTO reportingperiod (siteId, reportingPeriodId, date1, date2, dateCreated, dateEdited) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                Arrays.asList(siteId, reportingPeriodId, date1, date2, new Date(), new Date()));
    }
    
    private void executeUpdate(QueryExecutor executor, IndicatorUpdate update) {
        if(update.value == null) {
            clearValue(executor, update);
        } else if(update.value instanceof Quantity) {
            executeQuantityUpdate(executor, update);
        } else if(update.value instanceof TextValue) {
            executeTextUpdate(executor, update);
        }
    }

    private void executeQuantityUpdate(QueryExecutor executor, IndicatorUpdate update) {
        Quantity quantity = (Quantity) update.value;
        executor.update("REPLACE INTO indicatorvalue (reportingPeriodId, indicatorId, Value) VALUES (?, ?, ?)",
                Arrays.asList(reportingPeriodId, update.indicatorId, quantity.getValue()));
    }
    
    private void executeTextUpdate(QueryExecutor executor, IndicatorUpdate update) {
        TextValue textValue = (TextValue) update.value;
        executor.update("REPLACE INTO indicatorvalue (reportingPeriodId, indicatorId, TextValue) VALUES (?, ?, ?)",
                Arrays.asList(reportingPeriodId, update.indicatorId, textValue.asString()));
    }

    private void clearValue(QueryExecutor executor,  IndicatorUpdate update) {
        executor.update("UPDATE indicatorvalue SET value = NULL, textValue = NULL " +
                "WHERE reportingPeriodId = ? AND indicatorId = ?", Arrays.asList(reportingPeriodId, update.indicatorId));
    }

    private int queryReportingPeriod(QueryExecutor executor) throws SQLException {
        
        try(ResultSet rs = executor.query("SELECT ReportingPeriodId FROM reportingperiod WHERE SiteId = " + siteId)) {
            if(!rs.next()) {
                throw new IllegalStateException("ReportingPeriod does not exist for site " + siteId);
            }
            int reportingPeriodId = rs.getInt(1);
            if(rs.next()) {
                throw new IllegalStateException("Site " + siteId + " has multiple reporting periods, expected exactly one");
            }
            return reportingPeriodId;
        }
        
    }
}
