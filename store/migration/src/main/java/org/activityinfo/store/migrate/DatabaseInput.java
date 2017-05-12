package org.activityinfo.store.migrate;

import com.google.appengine.tools.mapreduce.Input;
import com.google.appengine.tools.mapreduce.InputReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Set of all ids in the user database table.
 */
public class DatabaseInput extends Input<Integer> {
    @Override
    public List<? extends InputReader<Integer>> createReaders() throws IOException {
        List<InputReader<Integer>> readers = new ArrayList<>();
        readers.add(new DatabaseReader());
        return readers;
    }

}
