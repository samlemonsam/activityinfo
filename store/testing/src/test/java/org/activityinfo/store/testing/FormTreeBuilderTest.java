package org.activityinfo.store.testing;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.formTree.FormMetadataProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class FormTreeBuilderTest {

    public class TestFormMetadataProvider implements FormMetadataProvider {

        private Map<ResourceId,FormMetadata> forms = Maps.newHashMap();

        public TestFormMetadataProvider(List<TestForm> testForms) {
            FormClass formClass;
            for (TestForm form : testForms) {
                 formClass = form.getFormClass();
                 forms.put(formClass.getId(), FormMetadata.of(0, formClass, FormPermissions.readWrite()));
            }
        }

        @Override
        public FormMetadata getFormMetadata(ResourceId formId) {
            return forms.get(formId);
        }
    }


    @Test
    public void buildAndLoadBlankSubform() {
        List<TestForm> testForms = Lists.newArrayList();

        GenericForm generic = new GenericForm();
        BlankSubForm blank = new BlankSubForm(generic);

        testForms.add(generic);
        testForms.add(blank);

        TestFormMetadataProvider provider = new TestFormMetadataProvider(testForms);
        FormTreeBuilder builder = new FormTreeBuilder(provider);

        // Build the form tree, and attempt to fetch the blank sub form
        FormTree tree = builder.queryTree(generic.getFormId());
        tree.getFormClass(blank.getFormId());
    }


}