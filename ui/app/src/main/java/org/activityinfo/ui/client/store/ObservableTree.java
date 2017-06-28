package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.Scheduler;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FormTree that
 */
public class ObservableTree<KeyT, NodeT, TreeT> extends Observable<TreeT> {

    private static final Logger LOGGER = Logger.getLogger(ObservableTree.class.getName());


    public interface TreeLoader<KeyT, NodeT, TreeT> {

        KeyT getRootKey();

        Observable<NodeT> get(KeyT nodeKey);

        Iterable<KeyT> getChildren(NodeT nodeT);

        TreeT build(Map<KeyT, Observable<NodeT>> nodes);
    }


    private final TreeLoader<KeyT, NodeT, TreeT> loader;
    private final Scheduler scheduler;

    private Map<KeyT, Observable<NodeT>> nodes = new HashMap<>();
    private Map<KeyT, Subscription> subscriptions = new HashMap<>();

    private TreeT value;

    private boolean crawling = false;
    private boolean crawlPending = false;

    public ObservableTree(TreeLoader<KeyT, NodeT, TreeT> loader, Scheduler scheduler) {
        this.loader = loader;
        this.scheduler = scheduler;
    }

    @Override
    public boolean isLoading() {
        return value == null;
    }

    @Override
    public TreeT get() {
        assert value != null : "Tree is not loaded!";
        return value;
    }

    @Override
    protected void onConnect() {
        connectTo(loader.getRootKey());
    }

    @Override
    protected void onDisconnect() {
        this.value = null;
        for (Subscription subscription : subscriptions.values()) {
            subscription.unsubscribe();
        }
        nodes.clear();
        subscriptions.clear();
    }

    private void connectTo(KeyT nodeKey) {
        if(!nodes.containsKey(nodeKey)) {

            Observable<NodeT> metadata = loader.get(nodeKey);
            Subscription subscription = metadata.subscribe(this::onNodeChanged);

            nodes.put(nodeKey, metadata);
            subscriptions.put(nodeKey, subscription);
        }
    }

    private void disconnectFrom(KeyT nodeKey) {
        nodes.remove(nodeKey);
        Subscription subscription = subscriptions.remove(nodeKey);
        subscription.unsubscribe();
    }

    private void onNodeChanged(Observable<NodeT> formClass) {
        if(formClass.isLoaded()) {
            if(crawling) {
                crawlPending = true;
            } else {
                recrawl();
            }
        }
    }

    /**
     * Crawl from the root tree to all the leaves to find the set
     * of forms we need to query,
     */
    private void recrawl() {

        LOGGER.info("Tree " + loader + ": Recrawl starting...");

        Set<KeyT> reachable = new HashSet<>();
        Set<KeyT> missing = new HashSet<>();
        Set<KeyT> loading = new HashSet<>();

        crawling = true;
        value = null;

        try {

            crawl(loader.getRootKey(), reachable, missing, loading);

            // First clean up forms that are no longer reachable
            List<KeyT> connectedForms = new ArrayList<>(nodes.keySet());
            for (KeyT nodeKey : connectedForms) {
                if (!reachable.contains(nodeKey)) {
                    disconnectFrom(nodeKey);
                }
            }

            LOGGER.info("Tree " + loader + ": reachable = " + reachable +
                    ", missing = " + missing + ", " +
                    ", loading = " + loading);


            // Start listening to any new nodes that we have just discovered.
            for (KeyT nodeKey : missing) {
                connectTo(nodeKey);
            }

            // Otherwise if we've got everything, we can build the tree
            if (missing.isEmpty() && loading.isEmpty()) {
                rebuildTree();

            } else if(crawlPending) {
                scheduler.scheduleDeferred(this::recrawl);
                crawlPending = false;
            }

        } finally {
            crawling = false;
        }
    }

    /**
     * Recursively search the tree of forms for those that are reachable, missing, and still loading.
     */
    private void crawl(KeyT parentKey, Set<KeyT> reachable, Set<KeyT> missing, Set<KeyT> loading) {
        boolean seenForFirstTime = reachable.add(parentKey);

        if(!seenForFirstTime) {
            return;
        }

        Observable<NodeT> node = nodes.get(parentKey);
        if(node == null) {
            missing.add(parentKey);

        } else if(node.isLoading()) {
            loading.add(parentKey);

        } else if(node.isLoaded()) {

            for (KeyT childKey : loader.getChildren(node.get())) {
                crawl(childKey, reachable, missing, loading);
            }
        }
    }

    /**
     * After we have a loaded copy of all the form schemas, build the form tree and fire listeners.
     */
    private void rebuildTree() {

        try {
            this.value = loader.build(nodes);

            fireChange();
        } catch (Error e) {
            LOGGER.log(Level.SEVERE, "Exception rebuilding tree", e);
        }
    }

}
