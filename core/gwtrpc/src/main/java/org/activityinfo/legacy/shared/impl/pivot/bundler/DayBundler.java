package org.activityinfo.legacy.shared.impl.pivot.bundler;

import com.bedatadriven.rebar.sql.client.SqlResultSetRow;
import org.activityinfo.legacy.shared.command.result.Bucket;
import org.activityinfo.legacy.shared.reports.content.DayCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;


public class DayBundler implements Bundler {

    private final Dimension dimension;
    private final String dateAlias;

    public DayBundler(Dimension dimension, String dateAlias) {
        this.dimension = dimension;
        this.dateAlias = dateAlias;
    }

    @Override
    public void bundle(SqlResultSetRow row, Bucket bucket) {
        bucket.setCategory(dimension, new DayCategory(row.getDate(dateAlias)));
    }
}
