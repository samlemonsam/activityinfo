package org.activityinfo.store.mysql.cursor;

import com.google.common.collect.Lists;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.Cursor;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.mysql.mapping.PrimaryKeyExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.List;
import java.util.Observer;


class MySqlCursor implements Cursor {

    ResultSet resultSet;
    PrimaryKeyExtractor primaryKey;

    List<Runnable> onNext = Lists.newArrayList();
    List<CursorObserver> onClosed = Lists.newArrayList();

    @Override
    public boolean next() {
        try {
            boolean hasNext = resultSet.next();
            if(hasNext) {
                for(Runnable observer : onNext) {
                    observer.run();
                }
            } else {
                for(CursorObserver observer : onClosed) {
                    observer.onClosed();
                }
            }
            return hasNext;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResourceId getResourceId() {
        return primaryKey.getPrimaryKey(resultSet);
    }
}
