package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.impl.SchedulerImpl;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
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
class ObservableFormTree extends Observable<FormTree> {

    private static final Logger LOGGER = Logger.getLogger(ObservableFormTree.class.getName());

    private ResourceId rootFormId;
    private Function<ResourceId, Observable<FormClass>> provider;

    private Map<ResourceId, Observable<FormClass>> forms = new HashMap<>();
    private Map<ResourceId, Subscription> subscriptions = new HashMap<>();

    private FormTree tree;

    private boolean crawling = false;
    private boolean crawlPending = false;

    public ObservableFormTree(ResourceId rootFormId, Function<ResourceId, Observable<FormClass>> provider) {
        this.rootFormId = rootFormId;
        this.provider = provider;
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

            Observable<FormClass> formClass = provider.apply(formId);
            Subscription subscription = formClass.subscribe(this::onFormClassChanged);

            forms.put(formId, formClass);
            subscriptions.put(formId, subscription);
        }
    }

    private void disconnectFrom(ResourceId formId) {
        forms.remove(formId);
        Subscription subscription = subscriptions.remove(formId);
        subscription.unsubscribe();
    }

    private void onFormClassChanged(Observable<FormClass> formClass) {
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
                SchedulerImpl.INSTANCE.scheduleDeferred(this::recrawl);
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

        Observable<FormClass> form = forms.get(parentId);
        if(form == null) {
            missing.add(parentId);
        } else if(form.isLoading()) {
            loading.add(parentId);
        } else {
            for (FormField field : form.get().getFields()) {
                if(field.getType() instanceof ReferenceType) {
                    ReferenceType type = (ReferenceType) field.getType();
                    for (ResourceId childId : type.getRange()) {
                        crawl(childId, reachable, missing, loading);
                    }
                } else if(field.getType() instanceof SubFormReferenceType) {
                    SubFormReferenceType type = (SubFormReferenceType) field.getType();
                    crawl(type.getClassId(), reachable, missing, loading);
                }
            }
        }
    }

    /**
     * After we have a loaded copy of all the form schemas, build the form tree and fire listeners.
     */
    private void rebuildTree() {
        try {
            FormTreeBuilder builder = new FormTreeBuilder(formId -> {
                Observable<FormClass> formClass = forms.get(formId);
                assert formClass != null : "Form " + formId + " is missing!";
                assert formClass.isLoaded() : "Form " + formId + " is still loading!";

                return formClass.get();
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
