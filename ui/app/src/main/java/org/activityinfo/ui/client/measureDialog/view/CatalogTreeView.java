package org.activityinfo.ui.client.measureDialog.view;

import com.google.common.base.Optional;
import com.google.gwt.core.client.Callback;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.data.shared.loader.ChildTreeStoreBinding;
import com.sencha.gxt.data.shared.loader.DataProxy;
import com.sencha.gxt.data.shared.loader.TreeLoader;
import com.sencha.gxt.widget.core.client.tree.Tree;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.store.FormStore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Presents the user with a selection of Form
 */
public class CatalogTreeView implements IsWidget {

    private static final Logger LOGGER = Logger.getLogger(CatalogTreeView.class.getName());

    private final FormStore formStore;
    private final Tree<CatalogEntry, String> tree;
    private final TreeStore<CatalogEntry> store;
    private final CatalogLoader loader;

    private List<Subscription> subscriptions = new ArrayList<>();

    private final StatefulValue<Optional<ResourceId>> selectedForm = new StatefulValue<>(Optional.<ResourceId>absent());


    private static class EntryValueProvider implements ValueProvider<CatalogEntry, String> {
        @Override
        public String getValue(CatalogEntry object) {
            return object.getLabel();
        }

        @Override
        public void setValue(CatalogEntry object, String value) {

        }

        @Override
        public String getPath() {
            return "label";
        }
    }

    private class CatalogEntryKeyProvider implements ModelKeyProvider<CatalogEntry> {

        @Override
        public String getKey(CatalogEntry item) {
            return item.getId();
        }
    }

    private class CatalogProxy implements DataProxy<CatalogEntry, List<CatalogEntry>> {

        @Override
        public void load(CatalogEntry parent, final Callback<List<CatalogEntry>, Throwable> callback) {

            LOGGER.info("Loading children for " + parent);

            Observable<List<CatalogEntry>> entries;
            if(parent == null) {
                entries = formStore.getCatalogRoots();
            } else {
                entries = formStore.getCatalogChildren(ResourceId.valueOf(parent.getId()));
            }
            Subscription subscription = entries.subscribe(observable -> {
                if (!observable.isLoading()) {
                    callback.onSuccess(observable.get());
                }
            });

            subscriptions.add(subscription);
        }
    }

    private class CatalogLoader extends TreeLoader<CatalogEntry> {

        public CatalogLoader() {
            super(new CatalogProxy());
        }

        @Override
        public boolean hasChildren(CatalogEntry parent) {
            return parent.getType() == CatalogEntryType.FOLDER;
        }
    }

    public CatalogTreeView(FormStore formStore) {
        this.formStore = formStore;

        loader = new CatalogLoader();

        store = new TreeStore<>(new CatalogEntryKeyProvider());
        loader.addLoadHandler(new ChildTreeStoreBinding<>(store));

        tree = new Tree<CatalogEntry, String>(store, new EntryValueProvider()) {
            @Override
            protected void onDoubleClick(Event event) {
                super.onDoubleClick(event);
                TreeNode<CatalogEntry> node = findNode(event.getEventTarget().<Element> cast());
                if(node != null) {
                    CatalogEntry entry = node.getModel();
                    if(entry.getType() == CatalogEntryType.FORM) {

                    }
                }
            }
        };
        tree.setLoader(loader);
        tree.getSelectionModel().addSelectionHandler(event -> {
            if (event.getSelectedItem().getType() == CatalogEntryType.FORM) {
                selectedForm.updateValue(Optional.of(ResourceId.valueOf(event.getSelectedItem().getId())));
            } else {
                selectedForm.updateValue(Optional.absent());
            }
        });
    }


    @Override
    public Widget asWidget() {
        return tree;
    }

    public Observable<Optional<ResourceId>> getSelectedFormId() {
        return selectedForm;
    }

}
