package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.Scheduler;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.formTree.FormMetadataProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FormTree that
 */
public class ObservableFormTree extends Observable<FormTree> {

    private static final Logger LOGGER = Logger.getLogger(ObservableFormTree.class.getName());

    private final ResourceId rootFormId;
    private final Function<ResourceId, Observable<FormMetadata>> provider;
    private final Scheduler scheduler;

    private Map<ResourceId, Observable<FormMetadata>> forms = new HashMap<>();
    private Map<ResourceId, Subscription> subscriptions = new HashMap<>();

    private FormTree tree;

    private boolean crawling = false;
    private boolean crawlPending = false;

    public ObservableFormTree(ResourceId rootFormId, Function<ResourceId, Observable<FormMetadata>> provider,
                              Scheduler scheduler) {
        this.rootFormId = rootFormId;
        this.provider = provider;
        this.scheduler = scheduler;
    }

    public ObservableFormTree(ResourceId rootFormId, Function<ResourceId, Observable<FormMetadata>> provider) {
        this(rootFormId, provider, Scheduler.get());
    }



    @Override
    public boolean isLoading() {
        return tree == null;
    }

    @Override
    public FormTree get() {
        assert tree != null : "Tree is not loaded!";
        return tree;
    }

    @Override
    protected void onConnect() {
        connectTo(rootFormId);
    }

    private void connectTo(ResourceId formId) {
        if(!forms.containsKey(formId)) {

            Observable<FormMetadata> metadata = provider.apply(formId);
            Subscription subscription = metadata.subscribe(this::onFormMetadataChanged);

            forms.put(formId, metadata);
            subscriptions.put(formId, subscription);
        }
    }

    private void disconnectFrom(ResourceId formId) {
        forms.remove(formId);
        Subscription subscription = subscriptions.remove(formId);
        subscription.unsubscribe();
    }

    private void onFormMetadataChanged(Observable<FormMetadata> formClass) {
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

        LOGGER.info("FormTree " + rootFormId + ": Recrawl starting...");

        Set<ResourceId> reachable = new HashSet<>();
        Set<ResourceId> missing = new HashSet<>();
        Set<ResourceId> loading = new HashSet<>();

        crawling = true;
        tree = null;

        try {

            crawl(rootFormId, reachable, missing, loading);

            // First clean up forms that are no longer reachable
            for (ResourceId formId : forms.keySet()) {
                if (!reachable.contains(formId)) {
                    disconnectFrom(formId);
                }
            }

            LOGGER.info("FormTree " + rootFormId + ": reachable = " + reachable +
                    ", missing = " + missing + ", " +
                    ", loading = " + loading);


            // Now add any new forms that are required
            for (ResourceId formId : missing) {
                connectTo(formId);
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
    private void crawl(ResourceId parentId, Set<ResourceId> reachable, Set<ResourceId> missing, Set<ResourceId> loading) {
        boolean seenForFirstTime = reachable.add(parentId);

        if(!seenForFirstTime) {
            return;
        }

        Observable<FormMetadata> metadata = forms.get(parentId);
        if(metadata == null) {
            missing.add(parentId);

        } else if(metadata.isLoading()) {
            loading.add(parentId);

        } else if(metadata.isLoaded()) {
            if(metadata.get().isAccessible()) {
                for (FormField field : metadata.get().getSchema().getFields()) {
                    if (field.getType() instanceof ReferenceType) {
                        ReferenceType type = (ReferenceType) field.getType();
                        for (ResourceId childId : type.getRange()) {
                            crawl(childId, reachable, missing, loading);
                        }
                    } else if (field.getType() instanceof SubFormReferenceType) {
                        SubFormReferenceType type = (SubFormReferenceType) field.getType();
                        crawl(type.getClassId(), reachable, missing, loading);
                    }
                }
            }
        }
    }

    /**
     * After we have a loaded copy of all the form schemas, build the form tree and fire listeners.
     */
    private void rebuildTree() {

        try {
            FormTreeBuilder builder = new FormTreeBuilder(new FormMetadataProvider() {
                @Override
                public FormMetadata getFormMetadata(ResourceId formId) {
                    Observable<FormMetadata> metadata = forms.get(formId);
                    assert metadata != null : "Form " + formId + " is missing!";
                    assert !metadata.isLoading() : "Form " + formId + " is still loading!";

                    return metadata.get();
                }
            });

            this.tree = builder.queryTree(rootFormId);

            fireChange();
        } catch (Error e) {
            LOGGER.log(Level.SEVERE, "Exception rebuilding tree", e);
        }
    }


    @Override
    protected void onDisconnect() {
        this.tree = null;
        for (Subscription subscription : subscriptions.values()) {
            subscription.unsubscribe();
        }
        forms.clear();
        subscriptions.clear();
    }
}
