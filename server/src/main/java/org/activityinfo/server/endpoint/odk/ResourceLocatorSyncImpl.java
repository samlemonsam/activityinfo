package org.activityinfo.server.endpoint.odk;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.lookup.ReferenceChoice;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.impl.Updater;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;


public class ResourceLocatorSyncImpl implements ResourceLocatorSync {

    private static final Logger LOGGER = Logger.getLogger(ResourceLocatorSyncImpl.class.getName());

    private Provider<CollectionCatalog> catalog;

    @Inject
    public ResourceLocatorSyncImpl(Provider<CollectionCatalog> catalog) {
        this.catalog = catalog;
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return catalog.get().getFormClass(resourceId);
    }

    @Override
    public List<ReferenceChoice> getReferenceChoices(Set<ResourceId> range) {

        QueryModel queryModel = new QueryModel(Iterables.getOnlyElement(range));
        queryModel.selectResourceId().as("id");
        queryModel.selectExpr("label").as("label");
        
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog.get());
        ColumnSet columnSet = builder.build(queryModel);

        ColumnView id = columnSet.getColumnView("id");
        ColumnView label = columnSet.getColumnView("label");

        List<ReferenceChoice> choices = Lists.newArrayList();
        for (int i = 0; i < columnSet.getNumRows(); i++) {
            ResourceId choiceId = ResourceId.valueOf(id.getString(i));
            String choiceLabel = label.getString(i);
            
            choices.add(new ReferenceChoice(choiceId, choiceLabel));            
        }

        return choices;
    }

    @Override
    public void persist(FormInstance formInstance) {
        Updater updater = new Updater(catalog.get());
        updater.execute(formInstance);
    }
}
