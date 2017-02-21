package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.server.command.handler.PermissionOracle;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.impl.Updater;
import org.activityinfo.store.spi.BlobAuthorizer;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorage;

import java.util.Collection;
import java.util.List;


public class ResourceLocatorSyncImpl implements ResourceLocatorSync {

    //private static final Logger LOGGER = Logger.getLogger(ResourceLocatorSyncImpl.class.getName());

    private Provider<FormCatalog> catalog;
    private Provider<AuthenticatedUser> authenticatedUser;
    private PermissionOracle permissionOracle;
    private BlobAuthorizer blobAuthorizer;

    @Inject
    public ResourceLocatorSyncImpl(Provider<FormCatalog> catalog, Provider<AuthenticatedUser> authenticatedUser,
                                   PermissionOracle permissionOracle, BlobAuthorizer blobAuthorizer) {
        this.catalog = catalog;
        this.authenticatedUser = authenticatedUser;
        this.permissionOracle = permissionOracle;
        this.blobAuthorizer = blobAuthorizer;
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return catalog.get().getFormClass(resourceId);
    }

    @Override
    public List<ReferenceChoice> getReferenceChoices(Collection<ResourceId> range) {

        ResourceId formId = Iterables.getOnlyElement(range);
        QueryModel queryModel = new QueryModel(formId);
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
            
            choices.add(new ReferenceChoice(new RecordRef(formId, choiceId), choiceLabel));
        }

        return choices;
    }

    @Override
    public void persist(FormClass formClass) {
        Optional<FormStorage> form = catalog.get().getForm(formClass.getId());
        if(!form.isPresent()) {
            throw new IllegalArgumentException("no such formId:" + formClass.getId());
        }
        form.get().updateFormClass(formClass);
    }

    @Override
    public void persist(FormInstance formInstance) {
        Updater updater = new Updater(catalog.get(), authenticatedUser.get().getUserId(), blobAuthorizer);
        updater.execute(formInstance);
    }
}
