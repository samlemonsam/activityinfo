package org.activityinfo.store.testing;

import com.google.common.base.Optional;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.impl.NullFormScanCache;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorage;

import java.util.*;


public class TestingCatalog implements FormCatalog {

    private Map<ResourceId, TestingFormStorage> formMap = new HashMap<>();


    public TestingCatalog() {

        // Survey Use case
        add(new Survey());

        // Case Tracking use case
        IntakeForm intake = new IntakeForm();
        BioDataForm bioData = new BioDataForm(intake);
        IncidentForm incidentForm = new IncidentForm(bioData);
        ReferralSubForm referralSubForm = new ReferralSubForm(incidentForm);
        add(intake, bioData, incidentForm, referralSubForm);

    }

    private void add(TestForm... testForms) {
        for (TestForm testForm : testForms) {
            assert testForm.getFormClass().getLabel() != null : testForm.getFormId() + " is missing label";
            formMap.put(testForm.getFormId(), new TestingFormStorage(testForm));
        }
    }


    @Override
    public FormClass getFormClass(ResourceId formId) {
        if (!formMap.containsKey(formId)) {
            throw new IllegalArgumentException("No such form " + formId);
        }
        return formMap.get(formId).getFormClass();
    }

    public FormTree getFormTree(ResourceId formId) {
        FormTreeBuilder builder = new FormTreeBuilder(this);
        return builder.queryTree(formId);
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        return null;
    }

    @Override
    public Optional<FormStorage> getForm(ResourceId formId) {
        return Optional.<FormStorage>fromNullable(formMap.get(formId));
    }

    @Override
    public Optional<FormStorage> lookupForm(ResourceId recordId) {
        return Optional.absent();
    }

    @Override
    public List<CatalogEntry> getRootEntries() {
        return Collections.emptyList();
    }

    @Override
    public List<CatalogEntry> getChildren(String parentId, int userId) {
        return Collections.emptyList();
    }

    public ColumnSet query(QueryModel queryModel) {
        ColumnSetBuilder builder = new ColumnSetBuilder(this, new NullFormScanCache());
        return builder.build(queryModel);
    }
}
