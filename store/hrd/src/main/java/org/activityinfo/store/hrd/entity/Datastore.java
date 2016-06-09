package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.*;
import com.google.common.base.Optional;
import org.activityinfo.store.hrd.op.Operation;

import java.util.*;

/**
 * Simple wrapper over {@link DatastoreService}
 */
public class Datastore {
    private final DatastoreService datastoreService;
    private final Transaction tx;


    public Datastore() {
        this.datastoreService = DatastoreServiceFactory.getDatastoreService();
        this.tx = null;
    }
    
    private Datastore(DatastoreService datastoreService, Transaction tx) {
        this.datastoreService = datastoreService;
        this.tx = tx;
    }

    public Datastore(DatastoreService datastoreService) {
        this.datastoreService = datastoreService;
        this.tx = null;
    }


    public void execute(Operation operation) {
        int retries = 3;
        while (true) {
            Transaction tx = datastoreService.beginTransaction();
            Datastore wrapper = new Datastore(datastoreService, tx);
            try {
                operation.execute(wrapper);
                tx.commit();
                break;
            } catch (ConcurrentModificationException e) {
                if (retries == 0) {
                    throw e;
                }
                // Allow retry to occur
                --retries;
            } catch (RuntimeException | Error e) {
                throw e;

            } catch (Exception e) {
                throw new RuntimeException(e);
                
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
            }
        }
    }
    
    public <T> T load(TypedKey<T> key) throws EntityNotFoundException {
        Entity entity = datastoreService.get(key.raw());
        return key.typeEntity(entity);
    }
    
    public <T> Optional<T> loadIfPresent(TypedKey<T> key) {
        Entity entity;
        try {
            entity = datastoreService.get(key.raw());
        } catch (EntityNotFoundException e) {
            return Optional.absent();
        }
        
        return Optional.of(key.typeEntity(entity));
    }

    public void put(TypedEntity entity) {
        datastoreService.put(tx, entity.raw());
    }

    public void put(TypedEntity entity, TypedEntity... moreEntities) {
        List<Entity> toPut = new ArrayList<>(1 + moreEntities.length);
        toPut.add(entity.raw());
        for (TypedEntity moreEntity : moreEntities) {
            toPut.add(moreEntity.raw());
        }
        datastoreService.put(tx, toPut);
    }
    
    public <K extends TypedKey<E>, E> Map<K, E> get(Iterable<K> keys) {
        
        List<Key> rawKeys = new ArrayList<>();
        for (K key : keys) {
            rawKeys.add(key.raw());
        }

        Map<Key, Entity> map = datastoreService.get(tx, rawKeys);
        Map<K, E> typedMap = new HashMap<>();

        for (K key : keys) {
            Entity entity = map.get(key.raw());
            if(entity != null) {
                typedMap.put(key, key.typeEntity(entity));
            }
        }
        return typedMap;
    }
    
    public DatastoreService unwrap() {
        return datastoreService;
    }

}
