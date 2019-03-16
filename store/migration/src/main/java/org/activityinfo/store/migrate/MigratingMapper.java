package org.activityinfo.store.migrate;

import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class MigratingMapper extends MapOnlyMapper<ResultSet, Void> {


    private transient Closeable objectify;

    @Override
    public void beginSlice() {
        objectify = ObjectifyService.begin();
    }

    @Override
    public void map(ResultSet rs) {

        try {
            execute(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    protected abstract void execute(ResultSet rs) throws SQLException;


    @Override
    public void endSlice() {
        if(objectify != null) {
            objectify.close();
        }
    }
}
