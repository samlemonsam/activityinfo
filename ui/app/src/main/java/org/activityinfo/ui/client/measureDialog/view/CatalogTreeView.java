/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.measureDialog.view;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Callback;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.IconProvider;
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
import org.activityinfo.ui.client.icons.IconBundle;
import org.activityinfo.ui.client.store.FormStore;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
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
    private final Optional<String> rootId;
    private final Predicate<CatalogEntry> filter;

    private boolean includeSubForms = true;

    private List<Subscription> subscriptions = new ArrayList<>();

    private final StatefulValue<Optional<ResourceId>> selectedForm = new StatefulValue<>(Optional.<ResourceId>absent());
    private final StatefulValue<Optional<CatalogEntry>> selectedEntry = new StatefulValue<>(Optional.<CatalogEntry>absent());


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
                if(parent.getType() == CatalogEntryType.FORM && ! includeSubForms) {
                    entries = Observable.just(Collections.emptyList());
                } else {
                    entries = formStore.getCatalogChildren(ResourceId.valueOf(parent.getId()));
                }
            }

            entries = entries.transform(new Function<List<CatalogEntry>, List<CatalogEntry>>() {
                @Override
                public List<CatalogEntry> apply(List<CatalogEntry> catalogEntries) {
                    return Lists.newArrayList(Iterables.filter(catalogEntries, filter));
                }
            });

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
            if(parent.getType() == CatalogEntryType.FORM && !includeSubForms) {
                return false;
            }
            return !parent.isLeaf();
        }
    }

    public CatalogTreeView(FormStore formStore) {
        this(formStore, Optional.absent(), Predicates.alwaysTrue());
    }

    public CatalogTreeView(FormStore formStore, Optional<String> rootId, Predicate<CatalogEntry> filter) {
        this.formStore = formStore;
        this.rootId = rootId;
        this.filter = filter;

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
        tree.getStyle().setLeafIcon(IconBundle.INSTANCE.form());
        tree.getStyle().setNodeOpenIcon(IconBundle.INSTANCE.databaseOpen());
        tree.getStyle().setNodeCloseIcon(IconBundle.INSTANCE.databaseClosed());
        tree.setIconProvider(new IconProvider<CatalogEntry>() {
            @Override
            public ImageResource getIcon(CatalogEntry model) {
                switch (model.getType()) {
                    case FOLDER:
                        return IconBundle.INSTANCE.databaseClosed();
                    case ANALYSIS:
                        return IconBundle.INSTANCE.report();
                    default:
                    case FORM:
                        return IconBundle.INSTANCE.form();
                }
            }
        });

        tree.getSelectionModel().addSelectionHandler(event -> {
            selectedEntry.updateIfNotEqual(Optional.of(event.getSelectedItem()));
            if (event.getSelectedItem().getType() == CatalogEntryType.FORM) {
                selectedForm.updateValue(Optional.of(ResourceId.valueOf(event.getSelectedItem().getId())));
            } else {
                selectedForm.updateValue(Optional.absent());
            }
        });
    }

    public StatefulValue<Optional<CatalogEntry>> getSelectedEntry() {
        return selectedEntry;
    }

    @Override
    public Widget asWidget() {
        return tree;
    }

    public Observable<Optional<ResourceId>> getSelectedFormId() {
        return selectedForm;
    }

    public void addSelectionHandler(SelectionHandler<CatalogEntry> handler) {
        tree.getSelectionModel().addSelectionHandler(handler);
    }

    public void setIncludeSubForms(boolean includeSubForms) {
        this.includeSubForms = includeSubForms;
    }
}
