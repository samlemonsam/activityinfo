package org.activityinfo.store.migrate;

import com.google.appengine.tools.mapreduce.InputReader;
import com.google.appengine.tools.mapreduce.ShardContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class BatchingInputReader<I> extends InputReader<List<I>> {

    private InputReader<I> delegate;
    private int batchSize;

    public BatchingInputReader(InputReader<I> delegate, int batchSize) {
        this.delegate = delegate;
        this.batchSize = batchSize;
    }

    @Override
    public void beginSlice() throws IOException {
        super.beginSlice();
        delegate.beginSlice();
    }

    @Override
    public void beginShard() throws IOException {
        super.beginShard();
        delegate.beginShard();
    }

    @Override
    public List<I> next() throws IOException, NoSuchElementException {

        // Read the first element without catching the NoSuchElementException
        I first = delegate.next();

        List<I> batch = new ArrayList<>(batchSize);
        batch.add(first);

        try {
            while(batch.size() < batchSize) {
                batch.add(delegate.next());
            }
        } catch (NoSuchElementException ignored) {
            // Last batch
        }

        return batch;
    }

    @Override
    public void endSlice() throws IOException {
        super.endSlice();
        delegate.endSlice();
    }

    @Override
    public void endShard() throws IOException {
        super.endShard();
        delegate.endShard();
    }

    @Override
    public long estimateMemoryRequirement() {
        return delegate.estimateMemoryRequirement();
    }

    @Override
    public void setContext(ShardContext context) {
        super.setContext(context);
        delegate.setContext(context);
    }

    @Override
    public Double getProgress() {
        return delegate.getProgress();
    }
}
