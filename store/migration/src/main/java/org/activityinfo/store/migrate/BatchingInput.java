package org.activityinfo.store.migrate;

import com.google.appengine.tools.mapreduce.Input;
import com.google.appengine.tools.mapreduce.InputReader;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BatchingInput<I> extends Input<List<I>> {

    private Input<I> source;
    private int batchSize;

    public BatchingInput(Input<I> source, int batchSize) {
        this.source = source;
        this.batchSize = batchSize;
    }

    @Override
    public List<? extends InputReader<List<I>>> createReaders() throws IOException {
        return source.createReaders()
                .stream()
                .map(r -> (InputReader<List<I>>)new BatchingInputReader(r, batchSize))
                .collect(Collectors.toList());
    }
}
