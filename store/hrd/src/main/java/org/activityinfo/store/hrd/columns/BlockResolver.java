package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.activityinfo.store.hrd.entity.ColumnDescriptor;
import org.activityinfo.store.hrd.entity.FieldDescriptor;
import org.activityinfo.store.hrd.entity.FormEntity;

import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlockResolver {

    private static final Logger LOGGER = Logger.getLogger(BlockResolver.class.getName());


    private static class Fetch {
        private BlockId blockId;
        private long version;

        private boolean cached;
        private Entity block;

        public Fetch(BlockId blockId, long version) {
            this.blockId = blockId;
            this.version = version;
        }

        public String memcacheKey() {
            return blockId.memcacheKey(version);
        }
    }

    private FormEntity formEntity;

    private final MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
    private final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

    private List<Fetch> toFetch = new ArrayList<>();

    public BlockResolver(Transaction transaction, FormEntity formEntity) {
        this.formEntity = formEntity;
    }

    public void fetchRecordIds() {
        if(formEntity.getRecordCount() > 0) {
            int numBlocks = numBlocks(RecordIdBlock.BLOCK_SIZE);

            // Record Ids cannot change, so all blocks except the last one
            // do not change and we use a static version number "0"

            for (int i = 0; i < numBlocks - 1; i++) {
                toFetch.add(new Fetch(blockId(RecordIdBlock.BLOCK_NAME, i), 0));
            }

            // The last, or "tail" block is changing as records are added, so we need
            // to pay attention to the version
            toFetch.add(new Fetch(
                    blockId(RecordIdBlock.BLOCK_NAME, numBlocks - 1),
                    formEntity.getTailIdBlockVersion()));
        }
    }


    public void fetchTombstones() {
        if(formEntity.getDeletedCount() > 0) {
            int numBlocks = numBlocks(TombstoneBlock.BLOCK_SIZE);
            for (int i = 0; i < numBlocks; i++) {
                long version = formEntity.getTombstoneBlockVersion(i);
                if(version > 0) {
                    toFetch.add(new Fetch(blockId(TombstoneBlock.COLUMN_NAME, i), version));
                }
            }
        }
    }

    public void fetchFieldBlock(String fieldBlockId) {
        if(formEntity.getRecordCount() > 0) {
            ColumnDescriptor descriptor = formEntity.getFieldBlock(fieldBlockId);
            int numBlocks = numBlocks(descriptor.getRecordCount());
            for (int i = 0; i < numBlocks; i++) {
                long version = descriptor.getBlockVersion(i);
                if(version > 0) {
                    toFetch.add(new Fetch(blockId(descriptor.getColumnId(), i), version));
                }
            }
        }
    }

    private BlockId blockId(String columnId, int blockIndex) {
        return new BlockId(formEntity.getResourceId(), columnId, blockIndex);
    }

    private int numBlocks(int blockSize) {
        int num = formEntity.getRecordCount() / blockSize;
        if(formEntity.getRecordCount() != blockSize) {
            num++;
        }
        return num;
    }

    public void load() {
        LOGGER.info("Fetching " + toFetch.size() + " blocks");

        Set<String> memcacheKeys = new HashSet<>();
        for (Fetch fetch : toFetch) {
            memcacheKeys.add(fetch.memcacheKey());
        }

        Map<String, Object> cached = memcacheService.getAll(memcacheKeys);


        Set<Key> datastoreKeys = new HashSet<>();

        for (Fetch fetch : toFetch) {
            Entity blockEntity = null;
            try {
                blockEntity = (Entity) cached.get(fetch.memcacheKey());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to deserialize entity " + fetch.blockId, e);
            }
            if(blockEntity != null) {
                fetch.block = blockEntity;
                fetch.cached = true;
            } else {
                datastoreKeys.add(fetch.blockId.key());
            }
        }

        Map<Key, Entity> fetched = datastoreService.get(datastoreKeys);
        for (Fetch fetch : toFetch) {
            if(fetch.block == null) {
                fetch.block = fetched.get(fetch.blockId.key());
            }
        }
    }

    public Iterator<Entity> getTombstoneBlocks() {
        return getBlocks(TombstoneBlock.COLUMN_NAME);
    }

    public Iterator<Entity> getBlocks(String columnName) {
        List<Entity> blocks = new ArrayList<>();
        for (Fetch fetch : toFetch) {
            if(fetch.block != null && fetch.blockId.getColumnId().equals(columnName)) {
                blocks.add(fetch.block);
            }
        }
        return blocks.iterator();
    }


    public Iterator<Entity> getBlocks(FieldDescriptor descriptor) {
        if(!descriptor.hasBlockAssignment()) {
            return Collections.<Entity>emptyList().iterator();
        } else {
            return getBlocks(descriptor.getColumnId());
        }
    }

    public Future<Void> cacheBlocks() {
        AsyncMemcacheService memcache = MemcacheServiceFactory.getAsyncMemcacheService();
        Map<String, Entity> toCache = new HashMap<>();
        for (Fetch fetch : toFetch) {
            if(fetch.block != null && !fetch.cached) {
                toCache.put(fetch.memcacheKey(), fetch.block);
            }
        }
        return memcache.putAll(toCache);
    }

}

